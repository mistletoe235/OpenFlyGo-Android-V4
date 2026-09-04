package edu.playground.djivln.reconstruction

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.BufferedInputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V86StreamingControllerInstrumentedTest {
    data class Request(val method: String, val path: String, val headers: Map<String, String>)

    @Test fun sessionQueueUploadsOfflineImageAndDrains() {
        val server = ServerSocket(0, 4, InetAddress.getByName("127.0.0.1"))
        val requests = CopyOnWriteArrayList<Request>()
        val serverDone = CountDownLatch(1)
        val finalizeArrived = CountDownLatch(1)
        val releaseFinalize = CountDownLatch(1)
        Thread {
            try {
                repeat(3) {
                    server.accept().use { socket ->
                        val input = BufferedInputStream(socket.getInputStream())
                        val first = readLine(input).split(' ')
                        val headers = linkedMapOf<String, String>()
                        while (true) {
                            val line = readLine(input)
                            if (line.isEmpty()) break
                            val separator = line.indexOf(':')
                            if (separator > 0) headers[line.substring(0, separator)] = line.substring(separator + 1).trim()
                        }
                        val length = headers.entries.firstOrNull { row ->
                            row.key.equals("Content-Length", true)
                        }?.value?.toIntOrNull() ?: 0
                        var remaining = length
                        val buffer = ByteArray(4096)
                        while (remaining > 0) {
                            val count = input.read(buffer, 0, minOf(buffer.size, remaining))
                            if (count < 0) break
                            remaining -= count
                        }
                        requests += Request(first[0], first[1], headers)
                        val response = if (first[1].endsWith("/finalize")) {
                            finalizeArrived.countDown()
                            check(releaseFinalize.await(5, TimeUnit.SECONDS))
                            """{"id":"s20260830-test","phase":"processing","sealed":true,"image_count":1}"""
                        } else if (first[0] == "POST") {
                            """{"id":"s20260830-test","phase":"receiving","message":"ready","image_count":0}"""
                        } else """{"duplicate":false,"image_count":1}"""
                        val bytes = response.toByteArray()
                        socket.getOutputStream().apply {
                            write("HTTP/1.1 201 Created\r\nContent-Type: application/json\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n".toByteArray())
                            write(bytes); flush()
                        }
                    }
                }
            } finally { serverDone.countDown() }
        }.apply { isDaemon = true; start() }

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("v86_streaming_v4", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        java.io.File(context.filesDir, "v86-streaming").deleteRecursively()
        val uploaded = CountDownLatch(1)
        val controller = V86StreamingController(context) { snapshot ->
            if (snapshot.uploadedCount == 1 && snapshot.pendingCount == 0 && !snapshot.uploading) {
                uploaded.countDown()
            }
        }
        try {
            assertNull(controller.saveConnectionError(
                "http://127.0.0.1:${server.localPort}", "instrumented-secret"))
            controller.createSession(V86SessionConfig("test", 82.0, 10.0, "DJI"))
            val deadline = System.currentTimeMillis() + 5_000
            while (controller.current().sessionId == null && System.currentTimeMillis() < deadline) {
                Thread.sleep(20)
            }
            assertEquals("s20260830-test", controller.current().sessionId)
            val inspection = V86OfflineImageCompressor.Inspection(
                31.1, 121.2, 42.0, "2026-08-30T12:00:00.000Z", "DJI", "M30T", "NADIR")
            assertNull(controller.enqueueOfflineImageError(
                V86OfflineImageCompressor.PreparedImage("DJI_0001.jpg", ByteArray(256) { 7 },
                    inspection, false)))
            assertTrue(uploaded.await(8, TimeUnit.SECONDS))
            assertEquals(2, requests.size)
            val upload = requests.single { it.method == "PUT" }
            assertEquals("/api/sessions/s20260830-test/images/0", upload.path)
            assertEquals("Bearer instrumented-secret", upload.headers.header("Authorization"))
            assertNull(upload.headers.header("X-Latitude"))
            assertNull(upload.headers.header("X-Altitude"))
            assertFalse(controller.current().uploading)
            val originalEndpoint = controller.current().endpoint
            assertTrue(controller.saveConnection(
                "http://127.0.0.1:${server.localPort + 1}", "replacement-secret").isFailure)
            assertEquals(originalEndpoint, controller.current().endpoint)
            assertEquals("instrumented-secret", controller.accessCode())
            val finalized = CountDownLatch(1)
            controller.finalizeSession { finalized.countDown() }
            assertTrue(finalizeArrived.await(3, TimeUnit.SECONDS))
            val lateFrame = controller.enqueueOfflineImage(
                V86OfflineImageCompressor.PreparedImage("late.jpg", ByteArray(256) { 8 }, inspection, false))
            assertTrue(lateFrame.exceptionOrNull()?.message.orEmpty().contains("finalizing"))
            var duplicateError: Throwable? = null
            controller.finalizeSession { duplicateError = it.exceptionOrNull() }
            assertTrue(duplicateError?.message.orEmpty().contains("already finalizing"))
            releaseFinalize.countDown()
            assertTrue(finalized.await(3, TimeUnit.SECONDS))
            assertTrue(serverDone.await(2, TimeUnit.SECONDS))
            assertTrue(controller.current().session?.sealed == true)
            assertEquals(3, requests.size)
        } finally {
            releaseFinalize.countDown()
            controller.close(); server.close()
        }
    }

    @Test fun pendingQueueResumesAfterControllerRestart() {
        val server = ServerSocket(0, 4, InetAddress.getByName("127.0.0.1"))
        val done = CountDownLatch(1)
        val methods = CopyOnWriteArrayList<String>()
        Thread {
            try {
                repeat(3) {
                    server.accept().use { socket ->
                        val input = BufferedInputStream(socket.getInputStream())
                        val request = readLine(input).split(' ')
                        methods += request[0]
                        val headers = linkedMapOf<String, String>()
                        while (true) {
                            val line = readLine(input)
                            if (line.isEmpty()) break
                            val separator = line.indexOf(':')
                            if (separator > 0) headers[line.substring(0, separator)] = line.substring(separator + 1).trim()
                        }
                        var remaining = headers.header("Content-Length")?.toIntOrNull() ?: 0
                        val buffer = ByteArray(4096)
                        while (remaining > 0) {
                            val count = input.read(buffer, 0, minOf(buffer.size, remaining))
                            if (count < 0) break
                            remaining -= count
                        }
                        val response = if (request[0] == "GET") {
                            """{"id":"s20260830-resume","phase":"receiving","message":"ready","image_count":0}"""
                        } else """{"duplicate":false,"image_count":1}"""
                        val bytes = response.toByteArray()
                        socket.getOutputStream().apply {
                            write("HTTP/1.1 200 OK\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n".toByteArray())
                            write(bytes); flush()
                        }
                    }
                }
            } finally { done.countDown() }
        }.apply { isDaemon = true; start() }

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val preferences = context.getSharedPreferences("v86_streaming_v4", android.content.Context.MODE_PRIVATE)
        preferences.edit().clear()
            .putString("endpoint", "http://127.0.0.1:${server.localPort}")
            .putString("session_id", "s20260830-resume")
            .putInt("next_sequence", 1).putInt("captured", 1).putInt("uploaded", 0)
            .putLong("takeoff_asl_bits", 10.0.toBits()).commit()
        V86SecureTokenStore(context).save("resume-secret")
        val queue = java.io.File(context.filesDir, "v86-streaming/pending").apply {
            deleteRecursively(); mkdirs()
        }
        java.io.File(queue, "00000000.jpg").writeBytes(ByteArray(256) { 9 })
        java.io.File(queue, "00000000.json").writeText("""
            {"sequence":0,"latitude":null,"longitude":null,"upload_absolute_altitude_m":null,
             "altitude_source":"image_exif","upload_timestamp":"2026-08-30T12:00:00.000Z",
             "capture_view":"NADIR"}
        """.trimIndent())
        val drained = CountDownLatch(1)
        val controller = V86StreamingController(context) { state ->
            if (state.uploadedCount == 1 && state.pendingCount == 0 && !state.uploading) drained.countDown()
        }
        val secondController = V86StreamingController(context) { state ->
            if (state.uploadedCount == 1 && state.pendingCount == 0 && !state.uploading) drained.countDown()
        }
        try {
            assertTrue(drained.await(8, TimeUnit.SECONDS))
            assertTrue(done.await(2, TimeUnit.SECONDS))
            assertEquals(1, methods.count { it == "PUT" })
        } finally { controller.close(); secondController.close(); server.close() }
    }

    @Test fun corruptQueueBlocksFinalizeAndRepairsNextSequence() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val preferences = context.getSharedPreferences(
            "v86_streaming_v4", android.content.Context.MODE_PRIVATE)
        preferences.edit().clear().putString("session_id", "s20260830-corrupt")
            .putInt("next_sequence", 1).putInt("captured", 1).commit()
        V86SecureTokenStore(context).clear()
        val queue = java.io.File(context.filesDir, "v86-streaming/pending").apply {
            deleteRecursively(); mkdirs()
        }
        java.io.File(queue, "00000007.jpg").writeBytes(ByteArray(256) { 3 })
        val controller = V86StreamingController(context) {}
        try {
            assertEquals(1, controller.current().pendingCount)
            assertTrue(controller.current().error.orEmpty().contains("damaged"))
            assertEquals(8, preferences.getInt("next_sequence", -1))
            assertEquals(8, controller.current().capturedCount)
            var finalizeError: Throwable? = null
            controller.finalizeSession { finalizeError = it.exceptionOrNull() }
            assertTrue(finalizeError is V86QueueIntegrityException)
        } finally {
            controller.close()
            preferences.edit().clear().commit()
            queue.deleteRecursively()
        }
    }

    @Test fun sealedSessionRejectsImagesAndEmptySessionCannotFinalize() {
        val server = ServerSocket(0, 2, InetAddress.getByName("127.0.0.1"))
        val accepted = CountDownLatch(1)
        Thread {
            runCatching {
                server.accept().use { socket ->
                    val input = BufferedInputStream(socket.getInputStream())
                    val first = readLine(input)
                    val headers = linkedMapOf<String, String>()
                    while (true) {
                        val line = readLine(input)
                        if (line.isEmpty()) break
                        val separator = line.indexOf(':')
                        if (separator > 0) headers[line.substring(0, separator)] =
                            line.substring(separator + 1).trim()
                    }
                    var remaining = headers.header("Content-Length")?.toIntOrNull() ?: 0
                    while (remaining-- > 0) input.read()
                    assertTrue(first.startsWith("POST /api/sessions "))
                    val body = """{"id":"s20260830-sealed","phase":"complete","sealed":true,"completed":true,"image_count":0}"""
                        .toByteArray()
                    socket.getOutputStream().apply {
                        write("HTTP/1.1 201 Created\r\nContent-Length: ${body.size}\r\nConnection: close\r\n\r\n".toByteArray())
                        write(body); flush()
                    }
                }
            }.also { accepted.countDown() }
        }.apply { isDaemon = true; start() }
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val preferences = context.getSharedPreferences(
            "v86_streaming_v4", android.content.Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        java.io.File(context.filesDir, "v86-streaming").deleteRecursively()
        val controller = V86StreamingController(context) {}
        try {
            assertNull(controller.saveConnectionError(
                "http://127.0.0.1:${server.localPort}", "sealed-secret"))
            val created = CountDownLatch(1)
            controller.createSession(V86SessionConfig("sealed", 82.0, 10.0, "DJI")) {
                created.countDown()
            }
            assertTrue(created.await(5, TimeUnit.SECONDS))
            assertTrue(accepted.await(2, TimeUnit.SECONDS))
            val inspection = V86OfflineImageCompressor.Inspection(
                31.1, 121.2, 42.0, null, "DJI", "M30T", "NADIR")
            val enqueue = controller.enqueueOfflineImage(
                V86OfflineImageCompressor.PreparedImage(
                    "DJI_0001.jpg", ByteArray(256) { 2 }, inspection, false))
            assertTrue(enqueue.isFailure)
            val wrongGeneration = controller.enqueueOfflineImage(
                V86OfflineImageCompressor.PreparedImage(
                    "DJI_0002.jpg", ByteArray(256) { 3 }, inspection, false),
                "s20260830-another",
            )
            assertTrue(wrongGeneration.exceptionOrNull()?.message.orEmpty().contains("session changed"))
            var finalizeError: Throwable? = null
            controller.finalizeSession { finalizeError = it.exceptionOrNull() }
            assertTrue(finalizeError?.message.orEmpty().contains("At least one image") ||
                finalizeError?.message.orEmpty().contains("already finalized"))
        } finally {
            controller.close(); server.close()
            preferences.edit().clear().commit()
            java.io.File(context.filesDir, "v86-streaming").deleteRecursively()
        }
    }

    private fun readLine(input: BufferedInputStream): String {
        val bytes = mutableListOf<Byte>()
        while (true) {
            val value = input.read()
            if (value < 0 || value == '\n'.code) break
            if (value != '\r'.code) bytes += value.toByte()
        }
        return bytes.toByteArray().toString(Charsets.US_ASCII)
    }
    private fun Map<String, String>.header(name: String) =
        entries.firstOrNull { it.key.equals(name, true) }?.value
}
