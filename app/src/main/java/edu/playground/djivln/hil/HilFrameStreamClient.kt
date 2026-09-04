package edu.playground.djivln.hil

import android.os.SystemClock
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal class HilFrameStreamClient(
    private val host: String,
    private val port: Int,
    private val listener: Listener,
) : AutoCloseable {
    interface Listener {
        fun onFrame(frame: HilFrameProtocol.Frame)
        fun onFrameConnectionChanged(connected: Boolean, message: String)
    }

    private val running = AtomicBoolean(false)
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "openfly-hil-frame").apply { isDaemon = true }
    }
    @Volatile private var socket: Socket? = null

    fun start() {
        if (!running.compareAndSet(false, true)) return
        executor.execute(::runLoop)
    }

    private fun runLoop() {
        while (running.get()) {
            try {
                val active = Socket().apply {
                    tcpNoDelay = true
                    keepAlive = true
                    receiveBufferSize = 4 * 1024 * 1024
                    connect(InetSocketAddress(host, port), 1_500)
                }
                socket = active
                listener.onFrameConnectionChanged(true, "TCP $host:$port")
                DataInputStream(BufferedInputStream(active.getInputStream(), 256 * 1024)).use { input ->
                    while (running.get()) {
                        // Timestamp as soon as the first header word arrives. A slow payload or
                        // parser backlog must look older, never newly fresh after parsing ends.
                        listener.onFrame(HilFrameProtocol.read(input) {
                            SystemClock.elapsedRealtimeNanos()
                        })
                    }
                }
            } catch (error: Throwable) {
                if (running.get()) {
                    listener.onFrameConnectionChanged(false, error.message ?: error.javaClass.simpleName)
                    try {
                        Thread.sleep(RECONNECT_DELAY_MS)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            } finally {
                runCatching { socket?.close() }
                socket = null
            }
        }
    }

    override fun close() {
        if (!running.compareAndSet(true, false)) return
        runCatching { socket?.close() }
        socket = null
        executor.shutdownNow()
    }

    private companion object {
        const val RECONNECT_DELAY_MS = 500L
    }
}
