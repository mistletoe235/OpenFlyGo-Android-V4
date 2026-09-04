package edu.playground.djivln.reconstruction

import java.io.BufferedInputStream
import java.net.InetAddress
import java.net.ServerSocket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V86HttpSafetyTest {
    @Test fun `endpoint rejects path credentials query and cross origin artifact`() {
        assertTrue(runCatching { V86HttpClient.normalizeEndpoint("http://user:pass@example.com") }.isFailure)
        assertTrue(runCatching { V86HttpClient.normalizeEndpoint("http://example.com/api") }.isFailure)
        assertTrue(runCatching { V86HttpClient.normalizeEndpoint("http://example.com?q=1") }.isFailure)
        val client = V86HttpClient("http://127.0.0.1:54321", "secret")
        assertTrue(runCatching { client.download("http://example.com/cloud.ply") }
            .exceptionOrNull()?.message.orEmpty().contains("cross-origin"))
    }

    @Test fun `truncated download keeps valid cache and removes part`() {
        val server = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))
        val thread = Thread {
            runCatching {
                server.accept().use { socket ->
                    val input = BufferedInputStream(socket.getInputStream())
                    while (readLine(input).isNotEmpty()) Unit
                    val body = ByteArray(1024) { 7 }
                    socket.getOutputStream().apply {
                        write("HTTP/1.1 200 OK\r\nContent-Length: 4096\r\nConnection: close\r\n\r\n".toByteArray())
                        write(body); flush()
                    }
                }
            }
        }.apply { isDaemon = true; start() }
        val root = kotlin.io.path.createTempDirectory("v86-http").toFile()
        try {
            val target = root.resolve("cloud.ply").apply { writeText("known-good") }
            val result = runCatching {
                V86HttpClient("http://127.0.0.1:${server.localPort}", "secret")
                    .downloadToFile("/cloud.ply", target) { _, _ -> }
            }
            assertTrue(result.isFailure)
            assertEquals("known-good", target.readText())
            assertFalse(root.resolve("cloud.ply.part").exists())
        } finally { server.close(); thread.join(1_000); root.deleteRecursively() }
    }

    @Test fun `oversized json response is rejected from content length`() {
        val server = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))
        val thread = Thread {
            runCatching {
                server.accept().use { socket ->
                    val input = BufferedInputStream(socket.getInputStream())
                    while (readLine(input).isNotEmpty()) Unit
                    socket.getOutputStream().apply {
                        write(("HTTP/1.1 200 OK\r\nContent-Length: " +
                            (V86HttpClient.MAX_JSON_RESPONSE_BYTES + 1) +
                            "\r\nConnection: close\r\n\r\n").toByteArray())
                        flush()
                    }
                }
            }
        }.apply { isDaemon = true; start() }
        try {
            val error = runCatching {
                V86HttpClient("http://127.0.0.1:${server.localPort}", "secret")
                    .getSession("s12345")
            }.exceptionOrNull()
            assertTrue(error is V86HttpBodyLimitException)
        } finally { server.close(); thread.join(1_000) }
    }

    @Test fun `point cloud byte limit keeps valid cache and removes part`() {
        val server = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))
        val thread = Thread {
            runCatching {
                server.accept().use { socket ->
                    val input = BufferedInputStream(socket.getInputStream())
                    while (readLine(input).isNotEmpty()) Unit
                    socket.getOutputStream().apply {
                        write("HTTP/1.1 200 OK\r\nContent-Length: 32\r\nConnection: close\r\n\r\n".toByteArray())
                        write(ByteArray(32) { 1 }); flush()
                    }
                }
            }
        }.apply { isDaemon = true; start() }
        val root = kotlin.io.path.createTempDirectory("v86-max-download").toFile()
        try {
            val target = root.resolve("cloud.ply").apply { writeText("known-good") }
            val error = runCatching {
                V86HttpClient("http://127.0.0.1:${server.localPort}", "secret")
                    .downloadToFile("/cloud.ply", target, maxBytes = 8) { _, _ -> }
            }.exceptionOrNull()
            assertTrue(error is V86HttpBodyLimitException)
            assertEquals("known-good", target.readText())
            assertFalse(root.resolve("cloud.ply.part").exists())
        } finally { server.close(); thread.join(1_000); root.deleteRecursively() }
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
}
