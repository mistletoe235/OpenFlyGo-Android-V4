package edu.playground.djivln.reconstruction

import edu.playground.djivln.mini2.BuildConfig

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class V86HttpClient(endpoint: String, private val accessToken: String) {
    private val baseUrl = normalizeEndpoint(endpoint)
    private val baseAddress = URL(baseUrl)

    fun createSession(config: V86SessionConfig) =
        V86SessionState.decode(requestJson("POST", "/api/sessions", config.toJson()))
    fun getSession(id: String) =
        V86SessionState.decode(requestJson("GET", "/api/sessions/${safeSessionId(id)}"))
    fun finalize(id: String) = V86SessionState.decode(
        requestJson("POST", "/api/sessions/${safeSessionId(id)}/finalize", JSONObject()))
    fun retry(id: String) = V86SessionState.decode(
        requestJson("POST", "/api/sessions/${safeSessionId(id)}/retry", JSONObject()))
    fun cancel(id: String) = V86SessionState.decode(
        requestJson("POST", "/api/sessions/${safeSessionId(id)}/cancel", JSONObject()))
    fun result(id: String) = V86Result.decode(
        requestJson("GET", "/api/sessions/${safeSessionId(id)}/result"))

    fun uploadImage(
        sessionId: String, sequence: Int, file: File, filename: String, mimeType: String,
        latitude: Double?, longitude: Double?, absoluteAltitudeMeters: Double?,
        altitudeSource: String?, timestamp: String?, captureView: String,
    ): JSONObject {
        require(file.isFile && file.length() >= 128L) { "Image is missing or empty" }
        val connection = open("PUT", "/api/sessions/${safeSessionId(sessionId)}/images/$sequence")
        try {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", mimeType)
            connection.setRequestProperty("X-Filename", filename)
            latitude?.let { connection.setRequestProperty("X-Latitude", it.toString()) }
            longitude?.let { connection.setRequestProperty("X-Longitude", it.toString()) }
            absoluteAltitudeMeters?.let { connection.setRequestProperty("X-Altitude", it.toString()) }
            altitudeSource?.takeIf(String::isNotBlank)?.let {
                connection.setRequestProperty("X-Altitude-Source", it)
            }
            timestamp?.takeIf(String::isNotBlank)?.let { connection.setRequestProperty("X-Timestamp", it) }
            connection.setRequestProperty("X-Capture-View", captureView)
            connection.setFixedLengthStreamingMode(file.length())
            file.inputStream().buffered().use { input ->
                connection.outputStream.buffered().use { output -> input.copyTo(output) }
            }
            return JSONObject(readResponse(connection, MAX_JSON_RESPONSE_BYTES))
        } finally {
            connection.disconnect()
        }
    }

    fun download(pathOrUrl: String): ByteArray =
        readResponseBytes(open("GET", pathOrUrl), MAX_TEXT_ARTIFACT_BYTES)

    fun downloadToFile(pathOrUrl: String, target: File,
                       maxBytes: Long = MAX_POINT_CLOUD_BYTES,
                       onProgress: (Long, Long?) -> Unit): File {
        require(maxBytes > 0L) { "Download limit must be positive" }
        val connection = open("GET", pathOrUrl)
        val part = File(target.parentFile, "${target.name}.part")
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw V86HttpException(code,
                connection.errorStream?.use { readBounded(it, MAX_ERROR_RESPONSE_BYTES) }
                    ?.toString(StandardCharsets.UTF_8)?.take(500).orEmpty())
            target.parentFile?.mkdirs()
            val total = connection.contentLengthLong.takeIf { it > 0L }
            if (total != null && total > maxBytes) throw V86HttpBodyLimitException(
                "Download exceeds ${maxBytes} byte limit")
            var downloaded = 0L
            connection.inputStream.buffered().use { input ->
                FileOutputStream(part).buffered().use { output ->
                    val buffer = ByteArray(128 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (downloaded + count > maxBytes) throw V86HttpBodyLimitException(
                            "Download exceeds ${maxBytes} byte limit")
                        output.write(buffer, 0, count)
                        downloaded += count
                        onProgress(downloaded, total)
                    }
                }
            }
            require(downloaded > 0L) { "Downloaded file is empty" }
            require(total == null || downloaded == total) {
                "Downloaded file is incomplete: $downloaded / $total"
            }
            if (!part.renameTo(target)) {
                part.copyTo(target, overwrite = true)
                part.delete()
            }
            return target
        } finally {
            connection.disconnect()
            if (part.isFile) part.delete()
        }
    }

    private fun requestJson(method: String, path: String, body: JSONObject? = null): JSONObject {
        val connection = open(method, path)
        try {
            if (body != null) {
                val bytes = body.toString().toByteArray(StandardCharsets.UTF_8)
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }
            }
            return JSONObject(readResponse(connection, MAX_JSON_RESPONSE_BYTES))
        } finally {
            connection.disconnect()
        }
    }

    private fun open(method: String, pathOrUrl: String): HttpURLConnection {
        val url = if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            URL(pathOrUrl)
        } else URL(baseUrl + if (pathOrUrl.startsWith('/')) pathOrUrl else "/$pathOrUrl")
        require(url.protocol == "http" || url.protocol == "https") { "HTTP/HTTPS only" }
        require(sameOrigin(baseAddress, url)) { "Refusing to send access code cross-origin" }
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 180_000
            useCaches = false
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json, application/octet-stream")
            setRequestProperty("Authorization", "Bearer $accessToken")
        }
    }

    private fun readResponse(connection: HttpURLConnection, maxBytes: Int) =
        readResponseBytes(connection, maxBytes).toString(StandardCharsets.UTF_8)

    private fun readResponseBytes(connection: HttpURLConnection, successLimit: Int): ByteArray {
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val limit = if (code in 200..299) successLimit else MAX_ERROR_RESPONSE_BYTES
            val declared = connection.contentLengthLong
            if (declared > limit.toLong()) throw V86HttpBodyLimitException(
                "HTTP response exceeds $limit byte limit")
            val bytes = stream?.use { readBounded(it, limit) } ?: ByteArray(0)
            if (code !in 200..299) throw V86HttpException(code,
                bytes.toString(StandardCharsets.UTF_8).trim().take(500).ifBlank {
                    connection.responseMessage ?: "HTTP $code"
                })
            return bytes
        } finally { connection.disconnect() }
    }

    private fun readBounded(input: InputStream, limit: Int): ByteArray =
        ByteArrayOutputStream(minOf(limit, 64 * 1024)).use { output ->
            val buffer = ByteArray(16 * 1024)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (total + count > limit) throw V86HttpBodyLimitException(
                    "HTTP response exceeds $limit byte limit")
                output.write(buffer, 0, count)
                total += count
            }
            output.toByteArray()
        }

    private fun safeSessionId(value: String): String {
        require(value.matches(Regex("[a-z0-9][a-z0-9_-]{5,63}"))) { "Invalid session ID" }
        return value
    }

    companion object {
        val DEFAULT_ENDPOINT: String = BuildConfig.V86_DEFAULT_ENDPOINT
        const val MAX_JSON_RESPONSE_BYTES = 2 * 1024 * 1024
        const val MAX_TEXT_ARTIFACT_BYTES = 16 * 1024 * 1024
        const val MAX_ERROR_RESPONSE_BYTES = 64 * 1024
        const val MAX_POINT_CLOUD_BYTES = 512L * 1024L * 1024L
        fun normalizeEndpoint(value: String): String {
            val trimmed = value.trim().trimEnd('/')
            val url = URL(trimmed)
            require(url.protocol == "http" || url.protocol == "https") { "HTTP/HTTPS only" }
            require(url.host.isNotBlank()) { "Service address is missing a host" }
            require(url.path.isBlank() || url.path == "/") { "Service address must not contain API path" }
            require(url.userInfo == null && url.query == null && url.ref == null) {
                "Service address must not contain credentials, query, or fragment"
            }
            return trimmed
        }

        private fun sameOrigin(a: URL, b: URL) =
            a.protocol.equals(b.protocol, true) && a.host.equals(b.host, true) &&
                (if (a.port >= 0) a.port else a.defaultPort) ==
                (if (b.port >= 0) b.port else b.defaultPort)
    }
}

class V86HttpException(val statusCode: Int, message: String) : Exception("HTTP $statusCode: $message")
class V86HttpBodyLimitException(message: String) : Exception(message)
