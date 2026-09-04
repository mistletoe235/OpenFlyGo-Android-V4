package edu.playground.djivln.hil

import android.os.SystemClock
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.security.SecureRandom
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

internal class HilUdpTransport(
    private val config: AndroidHilController.Config,
    private val listener: Listener,
) : AutoCloseable {
    interface Listener {
        fun onEvent(event: HilProtocol.Event)
        fun onPeerDiscovered(host: String)
        fun onPeerPacket()
        fun onLinkStale()
        fun onStatus(status: Status)
        fun onTransportError(message: String)
    }

    data class Status(
        val sessionId: Long,
        val peerHost: String?,
        val sentPoseCount: Long,
        val receivedPacketCount: Long,
        val measuredPoseSendHz: Double,
        val roundTripMillis: Double,
    )

    private val running = AtomicBoolean(false)
    private val latestPose = AtomicReference<HilProtocol.Pose?>()
    private val sequence = AtomicLong(0L)
    private val sentPoseCount = AtomicLong(0L)
    private val receivedPacketCount = AtomicLong(0L)
    private val lastReceivedSequence = AtomicLong(0L)
    private val outboundExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "openfly-hil-udp-outbound").apply { isDaemon = true }
    }
    private val statusExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "openfly-hil-udp-status").apply { isDaemon = true }
    }
    private val receiveExecutor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "openfly-hil-udp-receive").apply { isDaemon = true }
    }
    private val sessionId = SecureRandom().nextLong()
    private val hotspotPeerLock = HilHotspotPeerLock()
    private val peerStateLock = Any()
    private val outboundSendLock = Any()
    private val watchdog = HilLinkWatchdog(TimeUnit.MILLISECONDS.toNanos(config.heartbeatTimeoutMillis))
    private val replayWindow = HilReplayWindow()
    // Match the JVM scheduler/park time base. Some accelerated emulators advance
    // elapsedRealtime faster than the executor clock and otherwise under-report Hz.
    @Volatile private var rateStartedNanos = 0L
    @Volatile private var socket: DatagramSocket? = null
    @Volatile private var remoteAddress: InetSocketAddress? = null
    @Volatile private var lastRttMillis = Double.NaN
    @Volatile private var lastHelloNanos = 0L
    private var lastTransportErrorMessage: String? = null
    private var lastTransportErrorNanos = 0L
    private var suppressedTransportErrors = 0L

    fun start(): Boolean {
        if (!running.compareAndSet(false, true)) return true
        try {
            remoteAddress = if (config.mode == HilConnectionMode.LAN) {
                InetSocketAddress(InetAddress.getByName(config.host), config.udpServerPort)
            } else {
                null
            }
            socket = DatagramSocket(config.udpLocalPort).apply {
                soTimeout = 250
                broadcast = config.mode == HilConnectionMode.HOTSPOT
                runCatching { trafficClass = 0xb8 } // Expedited forwarding where DSCP is honored.
                sendBufferSize = 128 * 1024
                receiveBufferSize = 128 * 1024
            }
        } catch (error: Throwable) {
            running.set(false)
            outboundExecutor.shutdownNow()
            statusExecutor.shutdownNow()
            receiveExecutor.shutdownNow()
            reportTransportError(error.message ?: error.javaClass.simpleName, force = true)
            return false
        }
        rateStartedNanos = System.nanoTime()
        outboundExecutor.execute(::sendHello)
        val posePeriodNanos = 1_000_000_000L / config.poseSendHz
        outboundExecutor.scheduleAtFixedRate(
            ::sendLatestPose, 0L, posePeriodNanos, TimeUnit.NANOSECONDS,
        )
        outboundExecutor.scheduleAtFixedRate(
            ::sendHeartbeatAndPoll, 0L, HEARTBEAT_PERIOD_MS, TimeUnit.MILLISECONDS,
        )
        statusExecutor.scheduleAtFixedRate(
            ::reportStatus, 100L, STATUS_PERIOD_MS, TimeUnit.MILLISECONDS,
        )
        receiveExecutor.execute(::receiveLoop)
        return true
    }

    fun submitPose(pose: HilProtocol.Pose) {
        latestPose.set(pose)
    }

    fun clearPose() {
        latestPose.set(null)
    }

    private fun sendHello() {
        lastHelloNanos = SystemClock.elapsedRealtimeNanos()
        val targets = if (config.mode == HilConnectionMode.HOTSPOT && remoteAddress == null) {
            discoveryTargets()
        } else {
            null
        }
        sendSequenced(targets) { nextSequence ->
            HilProtocol.encodeHello(
                sessionId,
                nextSequence,
                HilProtocol.Hello(
                SystemClock.elapsedRealtimeNanos(), config.poseSendHz, config.simulatorStateHz,
                config.frameTcpPort, CAPABILITY_TCP_JPEG or CAPABILITY_SAFETY_EVENTS,
                ),
            )
        }
    }

    private fun sendLatestPose() {
        if (!running.get()) return
        if (remoteAddress == null) return
        val pose = latestPose.get() ?: return
        if (!HilPoseFreshnessPolicy.shouldSend(pose, SystemClock.elapsedRealtimeNanos())) return
        if (sendSequenced { nextSequence ->
                HilProtocol.encodePose(sessionId, nextSequence, pose)
            }) {
            sentPoseCount.incrementAndGet()
        }
    }

    private fun sendHeartbeatAndPoll() {
        if (!running.get()) return
        val now = SystemClock.elapsedRealtimeNanos()
        if (!watchdog.isFresh(now) && now - lastHelloNanos >= HELLO_PERIOD_NANOS) sendHello()
        if (config.mode == HilConnectionMode.HOTSPOT && remoteAddress == null) return
        sendSequenced { nextSequence ->
            HilProtocol.encodeHeartbeat(
                sessionId,
                nextSequence,
                HilProtocol.Heartbeat(now, lastReceivedSequence.get(), 0),
            )
        }
        sendSequenced { nextSequence ->
            HilProtocol.encodePing(sessionId, nextSequence, HilProtocol.Ping(now))
        }
        if (watchdog.pollStaleTransition(now)) {
            replayWindow.reset()
            if (config.mode == HilConnectionMode.HOTSPOT) {
                synchronized(peerStateLock) {
                    remoteAddress = null
                    hotspotPeerLock.clear()
                    listener.onLinkStale()
                }
                sendHello()
            } else {
                listener.onLinkStale()
            }
        }
    }

    private fun receiveLoop() {
        val bytes = ByteArray(HilProtocol.MAX_DATAGRAM_BYTES)
        while (running.get()) {
            try {
                val packet = DatagramPacket(bytes, bytes.size)
                socket?.receive(packet) ?: break
                val datagram = HilProtocol.decode(packet.data, packet.length)
                if (datagram.header.sessionId != sessionId) continue
                if (!acceptPeer(packet.address)) continue
                if (!replayWindow.accept(datagram.header.sequence)) continue
                val now = SystemClock.elapsedRealtimeNanos()
                watchdog.onReceive(now)
                lastReceivedSequence.set(datagram.header.sequence)
                receivedPacketCount.incrementAndGet()
                listener.onPeerPacket()
                when (datagram.header.type) {
                    HilProtocol.TYPE_PING -> {
                        val ping = HilProtocol.decodePing(datagram.payload)
                        outboundExecutor.execute {
                            sendSequenced { nextSequence ->
                                HilProtocol.encodePong(
                                    sessionId,
                                    nextSequence,
                                    HilProtocol.Pong(ping.senderMonotonicNanos, now),
                                )
                            }
                        }
                    }
                    HilProtocol.TYPE_PONG -> {
                        val pong = HilProtocol.decodePong(datagram.payload)
                        val delta = now - pong.echoedSenderMonotonicNanos
                        if (delta >= 0L) lastRttMillis = delta / 1_000_000.0
                    }
                    HilProtocol.TYPE_EVENT -> listener.onEvent(HilProtocol.decodeEvent(datagram.payload))
                }
            } catch (_: SocketTimeoutException) {
                // Timeout exists only so close() is observed promptly.
            } catch (error: Throwable) {
                if (running.get()) reportTransportError(error.message ?: error.javaClass.simpleName)
            }
        }
    }

    private fun acceptPeer(address: InetAddress): Boolean = synchronized(peerStateLock) {
        val expected = remoteAddress
        if (expected != null) return@synchronized address == expected.address
        if (config.mode != HilConnectionMode.HOTSPOT) return@synchronized false
        val candidateHost = address.hostAddress ?: address.hostName
        if (!hotspotPeerLock.accept(candidateHost)) return@synchronized false
        remoteAddress = InetSocketAddress(address, config.udpServerPort)
        listener.onPeerDiscovered(candidateHost)
        true
    }

    private fun reportStatus() {
        if (!running.get()) return
        val elapsed = (System.nanoTime() - rateStartedNanos).coerceAtLeast(1L) / 1_000_000_000.0
        listener.onStatus(Status(
            sessionId,
            remoteAddress?.address?.hostAddress,
            sentPoseCount.get(),
            receivedPacketCount.get(),
            sentPoseCount.get() / elapsed,
            lastRttMillis,
        ))
    }

    private fun sendSequenced(
        explicitTargets: Set<InetSocketAddress>? = null,
        encode: (Long) -> ByteArray,
    ): Boolean = synchronized(outboundSendLock) {
        if (!running.get()) return@synchronized false
        val targets = explicitTargets ?: setOf(remoteAddress ?: return@synchronized false)
        val bytes = encode(sequence.incrementAndGet())
        var sent = false
        for (address in targets) {
            try {
                socket?.send(DatagramPacket(bytes, bytes.size, address)) ?: continue
                sent = true
            } catch (error: Throwable) {
                if (running.get()) reportTransportError(error.message ?: error.javaClass.simpleName)
            }
        }
        sent
    }

    @Synchronized
    private fun reportTransportError(message: String, force: Boolean = false) {
        val now = SystemClock.elapsedRealtimeNanos()
        val sameError = message == lastTransportErrorMessage
        if (!force && sameError && now - lastTransportErrorNanos < ERROR_REPORT_PERIOD_NANOS) {
            suppressedTransportErrors += 1L
            return
        }
        val suffix = if (suppressedTransportErrors > 0L) {
            " (suppressed $suppressedTransportErrors repeats)"
        } else {
            ""
        }
        lastTransportErrorMessage = message
        lastTransportErrorNanos = now
        suppressedTransportErrors = 0L
        listener.onTransportError(message + suffix)
    }

    private fun discoveryTargets(): Set<InetSocketAddress> {
        val addresses = linkedSetOf(InetSocketAddress("255.255.255.255", config.udpServerPort))
        runCatching {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val network = interfaces.nextElement()
                if (!network.isUp || network.isLoopback) continue
                network.interfaceAddresses.mapNotNullTo(addresses) { value ->
                    value.broadcast?.let { InetSocketAddress(it, config.udpServerPort) }
                }
            }
        }
        return addresses
    }

    override fun close() {
        if (!running.compareAndSet(true, false)) return
        socket?.close()
        socket = null
        watchdog.reset()
        replayWindow.reset()
        hotspotPeerLock.clear()
        outboundExecutor.shutdownNow()
        statusExecutor.shutdownNow()
        receiveExecutor.shutdownNow()
    }

    private companion object {
        const val CAPABILITY_TCP_JPEG = 1
        const val CAPABILITY_SAFETY_EVENTS = 1 shl 1
        const val HEARTBEAT_PERIOD_MS = 20L
        const val STATUS_PERIOD_MS = 100L
        const val HELLO_PERIOD_NANOS = 100_000_000L
        const val ERROR_REPORT_PERIOD_NANOS = 2_000_000_000L
    }
}
