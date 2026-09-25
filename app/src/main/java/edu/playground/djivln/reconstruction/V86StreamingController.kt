package edu.playground.djivln.reconstruction

import android.content.Context
import android.os.Handler
import android.os.Looper
import edu.playground.djivln.camera.TriggerFrameMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import org.json.JSONObject

class V86StreamingController(
    context: Context,
    private val listener: Listener,
) : AutoCloseable {
    fun interface Listener { fun onChanged(snapshot: Snapshot) }
    fun interface TextCallback { fun onResult(value: String?, error: String?) }
    fun interface ProgressListener { fun onProgress(downloaded: Long, total: Long?) }
    fun interface FileCallback { fun onResult(file: File?, error: String?) }
    fun interface SessionCallback { fun onResult(session: V86SessionState?, error: String?) }
    data class Snapshot(
        val endpoint: String,
        val sessionId: String?,
        val session: V86SessionState?,
        val result: V86Result?,
        val capturedCount: Int,
        val uploadedCount: Int,
        val pendingCount: Int,
        val rejectedCount: Int,
        val uploading: Boolean,
        val retryAtEpochMillis: Long?,
        val message: String,
        val error: String?,
    )

    private data class Pending(
        val sequence: Int,
        val file: File,
        val metadataFile: File,
        val latitude: Double?,
        val longitude: Double?,
        val altitude: Double?,
        val altitudeSource: String?,
        val timestamp: String?,
        val captureView: String,
    )

    private data class QueueScan(
        val pending: List<Pending>,
        val issues: List<String>,
        val maximumSequence: Int?,
    ) {
        val blockingCount: Int get() = pending.size + issues.size
        val integrityError: String? get() = issues.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "Persistent V86 queue is damaged: ", limit = 3)
    }

    private val app = context.applicationContext
    private val preferences = app.getSharedPreferences("v86_streaming_v4", Context.MODE_PRIVATE)
    private val tokenStore = V86SecureTokenStore(app)
    private val queueRoot = File(app.filesDir, "v86-streaming/pending").apply { mkdirs() }
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val pumping = AtomicBoolean(false)
    private val refreshing = AtomicBoolean(false)
    private val lock = PROCESS_QUEUE_LOCK
    @Volatile private var closed = false
    private var retryAttempt = 0
    private val terminating = AtomicBoolean(false)
    private val creating = AtomicBoolean(false)
    private val finalizing = AtomicBoolean(false)
    private val pollRunnable = Runnable { refresh() }
    private val startupQueue = scanQueue()
    @Volatile private var restoredSessionNeedsRefresh = preferences.getString("session_id", null) != null
    private var state = Snapshot(
        endpoint = preferences.getString("endpoint", V86HttpClient.DEFAULT_ENDPOINT)
            ?: V86HttpClient.DEFAULT_ENDPOINT,
        sessionId = preferences.getString("session_id", null),
        session = null,
        result = null,
        capturedCount = maxOf(preferences.getInt("captured", 0),
            startupQueue.maximumSequence?.plus(1) ?: 0),
        uploadedCount = preferences.getInt("uploaded", 0),
        pendingCount = startupQueue.blockingCount,
        rejectedCount = preferences.getInt("rejected", 0),
        uploading = false,
        retryAtEpochMillis = null,
        message = if (startupQueue.integrityError == null) "V86 ready" else "V86 queue requires repair",
        error = startupQueue.integrityError,
    )

    init {
        val recoveredNext = maxOf(preferences.getInt("next_sequence", 0),
            startupQueue.maximumSequence?.plus(1) ?: 0)
        preferences.edit().putInt("next_sequence", recoveredNext)
            .putInt("captured", state.capturedCount).commit()
        publish()
        if (state.sessionId != null && tokenStore.load() != null) {
            refresh()
            pump()
        }
    }

    fun current(): Snapshot = synchronized(lock) { state }
    fun accessCode(): String = tokenStore.load().orEmpty()

    fun saveConnection(endpoint: String, accessCode: String): Result<Unit> = runCatching {
        val normalized = V86HttpClient.normalizeEndpoint(endpoint)
        require(accessCode.isNotBlank()) { "Access code is required" }
        synchronized(lock) {
            check(!closed) { "V86 controller is closed" }
            check(normalized == state.endpoint ||
                (state.sessionId == null && !creating.get() && scanQueue().blockingCount == 0)) {
                "Finish or terminate the current session before changing the V86 service address"
            }
            tokenStore.save(accessCode)
            preferences.edit().putString("endpoint", normalized).apply()
            state = state.copy(endpoint = normalized, message = "V86 connection saved", error = null)
        }
        publish()
        pump()
    }

    fun saveConnectionError(endpoint: String, accessCode: String): String? =
        saveConnection(endpoint, accessCode).exceptionOrNull()?.message

    @JvmOverloads
    fun createSession(config: V86SessionConfig, callback: (Result<V86SessionState>) -> Unit = {}) {
        synchronized(lock) {
            if (closed || !creating.compareAndSet(false, true)) {
                main.post { callback(Result.failure(IllegalStateException("V86 session creation is unavailable"))) }
                return
            }
        }
        worker.execute {
            val result = runCatching {
                check(current().sessionId == null) { "A V86 session is already active" }
                val queue = scanQueue()
                check(queue.blockingCount == 0) {
                    queue.integrityError ?: "Persistent V86 queue is not empty"
                }
                client().createSession(config)
            }
            result.onSuccess { session ->
                synchronized(lock) {
                    if (closed) return@onSuccess
                    clearPendingLocked()
                    preferences.edit()
                        .putString("session_id", session.id)
                        .putInt("next_sequence", 0).putInt("captured", 0)
                        .putInt("uploaded", 0).putInt("rejected", 0)
                        .putLong("takeoff_asl_bits", config.takeoffAbsoluteAltitudeMeters.toBits())
                        .apply()
                    state = state.copy(sessionId = session.id, session = session, result = null,
                        capturedCount = 0, uploadedCount = 0, pendingCount = 0,
                        rejectedCount = 0, message = session.message, error = null)
                }
                restoredSessionNeedsRefresh = false
                publish()
            }.onFailure { fail("Create V86 session failed", it) }
            creating.set(false)
            main.post { callback(result) }
        }
    }

    fun createSessionJava(config: V86SessionConfig, callback: SessionCallback) {
        createSession(config) { result ->
            callback.onResult(result.getOrNull(), result.exceptionOrNull()?.message)
        }
    }

    fun enqueueCapture(jpeg: ByteArray, metadata: TriggerFrameMetadata): Result<Int> = runCatching {
        require(jpeg.size >= 128) { "Trigger frame is empty" }
        val sessionId = current().sessionId ?: error("No active V86 session")
        ensureSessionAcceptsImages()
        val age = metadata.capturedAtEpochMillis - metadata.telemetryUpdatedAtEpochMillis
        require(age in 0..MAX_TELEMETRY_AGE_MILLIS) { "Aircraft GPS is stale: ${age}ms" }
        require(metadata.hasFreshAircraftGps) { "Aircraft GPS is unavailable or invalid" }
        val takeoff = Double.fromBits(preferences.getLong("takeoff_asl_bits", Double.NaN.toBits()))
        val altitude = V86CaptureAltitudePolicy.resolve(
            metadata.aslMeters.takeIf(Double::isFinite),
            metadata.altitudeMeters.takeIf(Double::isFinite),
            takeoff.takeIf(Double::isFinite),
            "aircraft_asl",
        ) ?: error("Absolute aircraft altitude is unavailable")
        val sequence: Int
        synchronized(lock) {
            check(state.sessionId == sessionId) { "V86 session changed" }
            ensureSessionAcceptsImages()
            sequence = preferences.getInt("next_sequence", 0)
            val stem = "%08d".format(Locale.US, sequence)
            val image = File(queueRoot, "$stem.jpg")
            val json = File(queueRoot, "$stem.json")
            writeAtomic(image, jpeg)
            val payload = metadata.toJson().put("sequence", sequence)
                .put("altitude_source", altitude.source)
                .put("upload_absolute_altitude_m", altitude.absoluteAltitudeMeters)
            writeAtomic(json, payload.toString().toByteArray())
            check(preferences.edit().putInt("next_sequence", sequence + 1)
                .putInt("captured", state.capturedCount + 1).commit()) {
                "Cannot persist V86 queue counters"
            }
            state = state.copy(capturedCount = state.capturedCount + 1,
                pendingCount = scanQueue().blockingCount, message = "Frame queued", error = null)
        }
        publish()
        pump()
        sequence
    }.onFailure { reportCaptureRejected(it.message ?: "Capture rejected") }

    fun enqueueCaptureError(jpeg: ByteArray, metadata: TriggerFrameMetadata): String? =
        enqueueCapture(jpeg, metadata).exceptionOrNull()?.message

    fun enqueueOfflineImage(image: V86OfflineImageCompressor.PreparedImage): Result<Int> =
        enqueueOfflineImage(image, current().sessionId)

    fun enqueueOfflineImage(
        image: V86OfflineImageCompressor.PreparedImage,
        expectedSessionId: String?,
    ): Result<Int> = runCatching {
        require(image.bytes.size >= 128) { "Offline image is empty" }
        check(expectedSessionId != null && current().sessionId == expectedSessionId) {
            "V86 session changed during offline preflight"
        }
        ensureSessionAcceptsImages()
        val sequence: Int
        synchronized(lock) {
            check(state.sessionId == expectedSessionId) {
                "V86 session changed during offline preflight"
            }
            ensureSessionAcceptsImages()
            sequence = preferences.getInt("next_sequence", 0)
            val stem = "%08d".format(Locale.US, sequence)
            val file = File(queueRoot, "$stem.jpg")
            val json = File(queueRoot, "$stem.json")
            writeAtomic(file, image.bytes)
            val payload = JSONObject()
                .put("schema", "openfly.v86.offline.v1")
                .put("sequence", sequence)
                .put("filename", image.displayName)
                .put("latitude", JSONObject.NULL).put("longitude", JSONObject.NULL)
                .put("upload_absolute_altitude_m", JSONObject.NULL)
                .put("altitude_source", "image_exif")
                .put("upload_timestamp", image.inspection.timestamp ?: JSONObject.NULL)
                .put("capture_view", image.inspection.captureView)
            writeAtomic(json, payload.toString().toByteArray())
            check(preferences.edit().putInt("next_sequence", sequence + 1)
                .putInt("captured", state.capturedCount + 1).commit()) {
                "Cannot persist V86 queue counters"
            }
            state = state.copy(capturedCount = state.capturedCount + 1,
                pendingCount = scanQueue().blockingCount, message = "Offline image queued", error = null)
        }
        publish(); pump(); sequence
    }

    fun enqueueOfflineImageError(image: V86OfflineImageCompressor.PreparedImage): String? =
        enqueueOfflineImage(image).exceptionOrNull()?.message

    fun enqueueOfflineImageError(
        image: V86OfflineImageCompressor.PreparedImage,
        expectedSessionId: String?,
    ): String? = enqueueOfflineImage(image, expectedSessionId).exceptionOrNull()?.message

    fun reportCaptureRejected(reason: String) {
        synchronized(lock) {
            val queue = scanQueue()
            preferences.edit().putInt("rejected", state.rejectedCount + 1).apply()
            state = state.copy(rejectedCount = state.rejectedCount + 1,
                pendingCount = queue.blockingCount, message = "Capture not queued",
                error = queue.integrityError ?: reason)
        }
        publish()
    }

    fun retryUploads() { retryAttempt = 0; update { it.copy(retryAtEpochMillis = null) }; pump() }

    @JvmOverloads fun refresh(callback: (Result<V86SessionState>) -> Unit = {}) {
        val id = current().sessionId ?: return
        if (!refreshing.compareAndSet(false, true)) return
        worker.execute {
            try {
                val result = runCatching { client().getSession(id) }
                result.onSuccess { session -> update {
                    restoredSessionNeedsRefresh = false
                    it.copy(session = session, message = session.message, error = session.error)
                }}.onFailure { fail("Refresh V86 failed", it) }
                main.post { callback(result) }
            } finally { refreshing.set(false) }
        }
    }

    @JvmOverloads fun finalizeSession(callback: (Result<V86SessionState>) -> Unit = {}) {
        val reservation = synchronized(lock) {
            runCatching {
                check(!closed && !terminating.get()) {
                    "V86 session is not ready to finalize"
                }
                check(finalizing.compareAndSet(false, true)) { "V86 session is already finalizing" }
            }
        }
        if (reservation.isFailure) return callback(Result.failure(reservation.exceptionOrNull()!!))
        fun reject(error: Throwable) {
            finalizing.set(false)
            callback(Result.failure(error))
        }
        val snapshot = current()
        val queue = scanQueue()
        queue.integrityError?.let { error ->
            return reject(V86QueueIntegrityException(error))
        }
        if (snapshot.pendingCount > 0 || queue.pending.isNotEmpty() || snapshot.uploading)
            return reject(IllegalStateException("Pending uploads must reach zero"))
        if (snapshot.capturedCount <= 0)
            return reject(IllegalStateException("At least one image is required"))
        if (snapshot.session?.sealed == true || snapshot.session?.completed == true ||
            snapshot.result?.completed == true)
            return reject(IllegalStateException("V86 session is already finalized"))
        val id = snapshot.sessionId ?: return reject(IllegalStateException("No session"))
        if (restoredSessionNeedsRefresh) return reject(IllegalStateException("Restored V86 session must refresh before finalize"))
        worker.execute {
            val result = runCatching { client().finalize(id) }
            result.onSuccess { session -> update {
                it.copy(session = session, message = session.message, error = session.error)
            }}.onFailure { fail("Finalize V86 failed", it) }
            finalizing.set(false)
            main.post { callback(result) }
        }
    }

    @JvmOverloads fun retryProcessing(callback: (Result<V86SessionState>) -> Unit = {}) {
        val id = current().sessionId ?: return callback(Result.failure(IllegalStateException("No session")))
        worker.execute {
            val result = runCatching { client().retry(id) }
            result.onSuccess { session -> update {
                it.copy(session = session, message = session.message, error = session.error)
            }}.onFailure { fail("Retry V86 processing failed", it) }
            main.post { callback(result) }
        }
    }

    fun discardEmptySessionError(): String? = runCatching {
        val snapshot = current()
        require(snapshot.capturedCount == 0 && snapshot.pendingCount == 0 && !snapshot.uploading) {
            "Only an empty V86 session can be discarded"
        }
        terminateCurrentSession()
    }.exceptionOrNull()?.message

    fun terminateCurrentSession() {
        val id = current().sessionId ?: return
        if (!terminating.compareAndSet(false, true)) return
        update { it.copy(message = "Terminating V86 session") }
        worker.execute {
            runCatching { client().cancel(id) }
                .onSuccess { resetLocalSession("V86 session terminated") }
                .onFailure { fail("Terminate V86 session failed", it) }
            terminating.set(false)
        }
    }

    @JvmOverloads fun fetchResult(callback: (Result<V86Result>) -> Unit = {}) {
        val id = current().sessionId ?: return callback(Result.failure(IllegalStateException("No session")))
        worker.execute {
            val result = runCatching { client().result(id) }
            result.onSuccess { value -> update {
                it.copy(result = value, message = value.message, error = value.error ?: value.missionError)
            }}.onFailure { fail("Fetch V86 result failed", it) }
            main.post { callback(result) }
        }
    }

    fun downloadPointCloudTo(target: File, onProgress: (Long, Long?) -> Unit,
                             callback: (Result<File>) -> Unit) {
        val url = current().result?.pointCloudUrl
            ?: return callback(Result.failure(IllegalStateException("Point cloud is unavailable")))
        worker.execute {
            val result = runCatching { client().downloadToFile(url, target, onProgress = onProgress) }
            main.post { callback(result) }
        }
    }

    fun downloadPointCloudToJava(
        target: File,
        progress: ProgressListener,
        callback: FileCallback,
    ) {
        downloadPointCloudTo(target, { downloaded, total -> progress.onProgress(downloaded, total) }) {
            callback.onResult(it.getOrNull(), it.exceptionOrNull()?.message)
        }
    }

    fun downloadMission(callback: (Result<String>) -> Unit) = downloadText(
        current().result?.missionUrl, "Mission is unavailable", callback)
    fun downloadViewerData(callback: (Result<String>) -> Unit) = downloadText(
        current().result?.viewerDataUrl, "Viewer data is unavailable", callback)

    fun downloadMissionJava(callback: TextCallback) {
        downloadMission { result -> callback.onResult(result.getOrNull(), result.exceptionOrNull()?.message) }
    }

    fun downloadViewerDataJava(callback: TextCallback) {
        downloadViewerData { result -> callback.onResult(result.getOrNull(), result.exceptionOrNull()?.message) }
    }

    private fun downloadText(path: String?, missing: String, callback: (Result<String>) -> Unit) {
        if (path == null) return callback(Result.failure(IllegalStateException(missing)))
        worker.execute {
            val result = runCatching { client().download(path).toString(Charsets.UTF_8) }
            main.post { callback(result) }
        }
    }

    private fun pump() {
        if (closed || current().sessionId == null || tokenStore.load() == null ||
            !pumping.compareAndSet(false, true)) return
        worker.execute {
            var ownsProcessPump = false
            try {
                ownsProcessPump = PROCESS_PUMP_LOCK.tryLock()
                if (!ownsProcessPump) {
                    main.postDelayed({ if (!closed) pump() }, PROCESS_PUMP_RETRY_MILLIS)
                    return@execute
                }
                update { it.copy(uploading = true, retryAtEpochMillis = null) }
                while (!closed) {
                    val queue = scanQueue()
                    val integrityError = queue.integrityError
                    if (integrityError != null) {
                        fail("Upload blocked by queue integrity error",
                            V86QueueIntegrityException(integrityError))
                        break
                    }
                    val item = queue.pending.firstOrNull() ?: break
                    val id = current().sessionId ?: break
                    try {
                        client().uploadImage(id, item.sequence, item.file, item.file.name,
                            "image/jpeg", item.latitude, item.longitude, item.altitude,
                            item.altitudeSource, item.timestamp, item.captureView)
                        synchronized(lock) {
                            if (!item.metadataFile.delete()) throw V86QueueIntegrityException(
                                "Cannot remove uploaded metadata ${item.metadataFile.name}")
                            if (!item.file.delete()) throw V86QueueIntegrityException(
                                "Cannot remove uploaded image ${item.file.name}")
                            retryAttempt = 0
                            check(preferences.edit()
                                .putInt("uploaded", state.uploadedCount + 1).commit()) {
                                "Cannot persist V86 upload counter"
                            }
                            state = state.copy(uploadedCount = state.uploadedCount + 1,
                                pendingCount = scanQueue().blockingCount,
                                message = "Frame uploaded", error = null)
                        }
                        publish()
                    } catch (error: Throwable) {
                        if (!V86UploadRetryPolicy.isAutomaticallyRetryable(error)) {
                            fail("Upload requires user action", error)
                            break
                        }
                        val delay = V86UploadRetryPolicy.delayMillis(++retryAttempt)
                        val retryAt = System.currentTimeMillis() + delay
                        update { it.copy(retryAtEpochMillis = retryAt,
                            message = "Upload paused; automatic retry scheduled", error = error.message) }
                        main.postDelayed({ pumping.set(false); pump() }, delay)
                        return@execute
                    }
                }
            } finally {
                if (ownsProcessPump) PROCESS_PUMP_LOCK.unlock()
                if (pumping.getAndSet(false)) update { it.copy(uploading = false,
                    pendingCount = scanQueue().blockingCount) }
            }
        }
    }

    private fun scanQueue(): QueueScan = synchronized(lock) {
        val files = queueRoot.listFiles().orEmpty()
        val issues = mutableListOf<String>()
        val sequences = sortedSetOf<Int>()
        files.forEach { file ->
            QUEUE_SEQUENCE_PREFIX.find(file.name)?.groupValues?.get(1)
                ?.toIntOrNull()?.let(sequences::add)
            val match = QUEUE_FILE_PATTERN.matchEntire(file.name)
            if (match == null) issues += "unexpected file ${file.name}"
            else match.groupValues[1].toIntOrNull()?.let(sequences::add)
                ?: run { issues += "invalid sequence ${file.name}" }
        }
        val pending = sequences.mapNotNull { sequence ->
            val stem = "%08d".format(Locale.US, sequence)
            val image = File(queueRoot, "$stem.jpg")
            val metadata = File(queueRoot, "$stem.json")
            if (!image.isFile || !metadata.isFile) {
                issues += "incomplete pair $stem"
                return@mapNotNull null
            }
            if (image.length() < 128L) {
                issues += "empty image $stem"
                return@mapNotNull null
            }
            if (metadata.length() !in 2..MAX_QUEUE_METADATA_BYTES) {
                issues += "invalid metadata size $stem"
                return@mapNotNull null
            }
            runCatching {
                val value = JSONObject(metadata.readText())
                require(value.getInt("sequence") == sequence) { "sequence mismatch" }
                Pending(sequence, image, metadata,
                    value.optDoubleOrNull("latitude"), value.optDoubleOrNull("longitude"),
                    value.optDoubleOrNull("upload_absolute_altitude_m"),
                    value.optString("altitude_source").takeIf(String::isNotBlank),
                    value.optString("upload_timestamp").takeIf(String::isNotBlank)
                        ?: isoTimestamp(value.optLong("captured_at_epoch_ms", 0L)),
                    value.optString("capture_view", "NADIR"))
            }.getOrElse {
                issues += "invalid metadata $stem: ${it.message ?: it.javaClass.simpleName}"
                null
            }
        }
        QueueScan(pending, issues, sequences.maxOrNull())
    }

    fun canAcceptCaptureFrames(): Boolean = current().sessionId != null &&
        runCatching { ensureSessionAcceptsImages() }.isSuccess

    private fun ensureSessionAcceptsImages() {
        val snapshot = current()
        check(!closed) { "V86 controller is closed" }
        check(!finalizing.get()) { "V86 session is finalizing" }
        check(!terminating.get()) { "V86 session is terminating" }
        check(!restoredSessionNeedsRefresh) { "Restored V86 session must refresh before capture" }
        val session = snapshot.session ?: error("V86 session state is unavailable")
        check(!session.sealed && !session.completed && snapshot.result?.completed != true) {
            "V86 session no longer accepts images"
        }
    }

    private fun client() = V86HttpClient(current().endpoint,
        tokenStore.load() ?: error("V86 access code is unavailable"))

    private fun update(transform: (Snapshot) -> Snapshot) {
        synchronized(lock) { state = transform(state) }
        publish()
    }
    private fun fail(prefix: String, error: Throwable) = update {
        it.copy(message = prefix, error = error.message ?: error.javaClass.simpleName)
    }
    private fun publish() {
        val value = current()
        main.post {
            if (!closed) {
                listener.onChanged(value)
                schedulePoll(value)
            }
        }
    }

    private fun schedulePoll(snapshot: Snapshot) {
        main.removeCallbacks(pollRunnable)
        if (snapshot.sessionId == null || tokenStore.load() == null) return
        val interval = when {
            snapshot.result?.completed == true || snapshot.session?.completed == true -> 60_000L
            snapshot.session?.sealed == true || snapshot.session?.running == true -> 5_000L
            else -> 15_000L
        }
        main.postDelayed(pollRunnable, interval)
    }

    private fun writeAtomic(target: File, bytes: ByteArray) {
        target.parentFile?.mkdirs()
        val temporary = File(target.parentFile, "${target.name}.tmp")
        temporary.writeBytes(bytes)
        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true); temporary.delete()
        }
    }
    private fun clearPendingLocked() { queueRoot.listFiles()?.forEach(File::delete) }
    private fun resetLocalSession(message: String) {
        synchronized(lock) {
            clearPendingLocked()
            preferences.edit().remove("session_id").remove("next_sequence")
                .remove("captured").remove("uploaded").remove("rejected")
                .remove("takeoff_asl_bits").apply()
            state = state.copy(sessionId = null, session = null, result = null,
                capturedCount = 0, uploadedCount = 0, pendingCount = 0,
                rejectedCount = 0, uploading = false, retryAtEpochMillis = null,
                message = message, error = null)
        }
        publish()
    }
    private fun isoTimestamp(epoch: Long): String? = epoch.takeIf { it > 0L }?.let {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(it))
    }
    private fun JSONObject.optDoubleOrNull(name: String): Double? =
        if (isNull(name)) null else optDouble(name).takeIf(Double::isFinite)

    override fun close() { closed = true; main.removeCallbacksAndMessages(null); worker.shutdownNow() }

    private companion object {
        const val MAX_TELEMETRY_AGE_MILLIS = 2_000L
        const val MAX_QUEUE_METADATA_BYTES = 256L * 1024L
        val QUEUE_FILE_PATTERN = Regex("^(\\d{8})\\.(jpg|json)$")
        val QUEUE_SEQUENCE_PREFIX = Regex("^(\\d{8})")
        val PROCESS_QUEUE_LOCK = Any()
        val PROCESS_PUMP_LOCK = ReentrantLock()
        const val PROCESS_PUMP_RETRY_MILLIS = 250L
    }
}
