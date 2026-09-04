package edu.playground.djivln.hil

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import edu.playground.djivln.mini2.R
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class AndroidHilController @JvmOverloads constructor(
    private val listener: Listener,
    context: Context? = null,
) : AutoCloseable {
    private val appContext = context?.applicationContext
    interface Listener {
        fun onHilEvent(event: HilProtocol.Event)
        fun onHilLinkStale()
        fun onHilStatus(status: Status)
        fun onHilError(message: String)
    }

    data class Config(
        val mode: HilConnectionMode = HilConnectionMode.LAN,
        val host: String,
        val udpServerPort: Int = 30_020,
        val udpLocalPort: Int = 30_021,
        val frameTcpPort: Int = 30_022,
        val poseSendHz: Int = 100,
        val simulatorStateHz: Int = 100,
        val heartbeatTimeoutMillis: Long = 1_000L,
    ) {
        init {
            require(mode == HilConnectionMode.HOTSPOT || host.isNotBlank())
            require(udpServerPort in 1..65_535 && udpLocalPort in 1..65_535 && frameTcpPort in 1..65_535)
            require(poseSendHz in 1..150 && simulatorStateHz in 2..150)
            require(heartbeatTimeoutMillis in 250L..10_000L)
        }
    }

    data class Status(
        val running: Boolean,
        val mode: HilConnectionMode,
        val peerHost: String?,
        val peerFresh: Boolean,
        val frameListening: Boolean,
        val frameConnected: Boolean,
        val framePeerHost: String?,
        val frame: HilVirtualFrameStore.Snapshot?,
        val sentPoseCount: Long,
        val receivedPacketCount: Long,
        val measuredPoseSendHz: Double,
        val roundTripMillis: Double,
        val message: String,
    )

    private val running = AtomicBoolean(false)
    private val peerFresh = AtomicBoolean(false)
    private val frameListening = AtomicBoolean(false)
    private val frameConnected = AtomicBoolean(false)
    private val frameServerConnected = AtomicBoolean(false)
    private val frameClientConnected = AtomicBoolean(false)
    private val latestStatus = AtomicReference<Status>()
    private val frameStore = HilVirtualFrameStore()
    @Volatile private var udp: HilUdpTransport? = null
    @Volatile private var frames: HilFrameStreamServer? = null
    @Volatile private var frameClient: HilFrameStreamClient? = null
    @Volatile private var frameClientHost: String? = null
    @Volatile private var frameClientGeneration = 0L
    @Volatile private var activeConfig: Config? = null
    @Volatile private var peerHost: String? = null
    @Volatile private var framePeerHost: String? = null

    fun start(config: Config) {
        stop()
        running.set(true)
        activeConfig = config
        peerHost = if (config.mode == HilConnectionMode.LAN) config.host else null
        val frameServer = createFrameServer(config.frameTcpPort)
        frames = frameServer
        if (!frameServer.start()) {
            running.set(false)
            frames = null
            val status = Status(
                false, config.mode, peerHost, false, false, false, null, null,
                0L, 0L, 0.0, Double.NaN, "HIL TCP listen failed",
            )
            latestStatus.set(status)
            listener.onHilStatus(status)
            return
        }
        val udpTransport = HilUdpTransport(config, object : HilUdpTransport.Listener {
            override fun onEvent(event: HilProtocol.Event) = listener.onHilEvent(event)
            override fun onPeerDiscovered(host: String) {
                peerHost = host
                ensureFrameClient(host, config.frameTcpPort)
            }
            override fun onPeerPacket() {
                peerFresh.set(true)
            }
            override fun onLinkStale() {
                peerFresh.set(false)
                if (config.mode == HilConnectionMode.HOTSPOT) resetHotspotPeerConnections()
                listener.onHilLinkStale()
            }
            override fun onStatus(status: HilUdpTransport.Status) {
                publishStatus(status, if (config.mode == HilConnectionMode.HOTSPOT) {
                    text(
                        R.string.hil_hotspot_discovery_status,
                        "Hotspot discovery ${peerHost ?: "waiting for UE"}:${config.udpServerPort}",
                        peerHost ?: text(R.string.waiting_for_ue, "waiting for UE"),
                        config.udpServerPort,
                    )
                } else {
                    text(R.string.hil_lan_status, "LAN ${config.host}:${config.udpServerPort}",
                        config.host, config.udpServerPort)
                })
            }
            override fun onTransportError(message: String) = listener.onHilError("UDP: $message")
        })
        udp = udpTransport
        if (!udpTransport.start()) {
            running.set(false)
            udp = null
            frameServer.close()
            frames = null
            val status = Status(
                false, config.mode, null, false, false, false, null, null,
                0L, 0L, 0.0, Double.NaN, "HIL UDP start failed",
            )
            latestStatus.set(status)
            listener.onHilStatus(status)
            return
        }
        if (config.mode == HilConnectionMode.LAN) {
            ensureFrameClient(config.host, config.frameTcpPort)
        }
    }

    fun submitPose(pose: HilProtocol.Pose) {
        udp?.submitPose(pose)
    }

    fun clearPose() {
        udp?.clearPose()
    }

    fun decodeLatestFrame(maxAgeMillis: Long): Bitmap? = frameStore.decodeLatest(maxAgeMillis)

    fun latestFrame(maxAgeMillis: Long): HilFrameProtocol.Frame? = frameStore.latestFrame(maxAgeMillis)

    fun frameSnapshot(): HilVirtualFrameStore.Snapshot? = frameStore.snapshot()

    fun isRunning(): Boolean = running.get()

    fun isPeerFresh(): Boolean = peerFresh.get()

    fun latestStatus(): Status? = latestStatus.get()

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        val stoppedMode = activeConfig?.mode ?: HilConnectionMode.LAN
        udp?.close()
        frames?.close()
        frameClient?.close()
        frameClientGeneration += 1L
        udp = null
        frames = null
        frameClient = null
        frameClientHost = null
        activeConfig = null
        peerHost = null
        framePeerHost = null
        peerFresh.set(false)
        frameListening.set(false)
        frameConnected.set(false)
        frameServerConnected.set(false)
        frameClientConnected.set(false)
        frameStore.clear()
        val status = Status(
            false, stoppedMode, null, false, false, false, null, null,
            0L, 0L, 0.0, Double.NaN, "HIL stopped",
        )
        latestStatus.set(status)
        listener.onHilStatus(status)
    }

    private fun publishStatus(status: HilUdpTransport.Status, message: String) {
        if (!running.get()) return
        val value = Status(
            true,
            activeConfig?.mode ?: HilConnectionMode.LAN,
            peerHost,
            peerFresh.get(),
            frameListening.get(),
            frameConnected.get(),
            framePeerHost,
            frameStore.snapshot(SystemClock.elapsedRealtimeNanos()),
            status.sentPoseCount,
            status.receivedPacketCount,
            status.measuredPoseSendHz,
            status.roundTripMillis,
            message,
        )
        latestStatus.set(value)
        listener.onHilStatus(value)
    }

    private fun createFrameServer(port: Int): HilFrameStreamServer {
        return HilFrameStreamServer(port, { peerHost }, object : HilFrameStreamServer.Listener {
            override fun onFrame(frame: HilFrameProtocol.Frame) {
                frameStore.offer(frame)
            }
            override fun onListeningChanged(listening: Boolean, message: String) {
                frameListening.set(listening)
                latestStatus.get()?.copy(frameListening = listening, message = message)?.let { updated ->
                    latestStatus.set(updated)
                    listener.onHilStatus(updated)
                }
            }
            override fun onFrameConnectionChanged(connected: Boolean, peerHost: String?, message: String) {
                handleFrameServerConnection(connected, peerHost, port, message)
            }
        }, appContext)
    }

    @Synchronized
    private fun handleFrameServerConnection(
        connected: Boolean,
        candidatePeerHost: String?,
        port: Int,
        message: String,
    ) {
        if (!running.get()) return
        if (connected) {
            val takingPriority = !frameServerConnected.get()
            frameServerConnected.set(true)
            if (takingPriority) frameStore.clear()
            frameClientGeneration += 1L
            frameClient?.close()
            frameClient = null
            frameClientHost = null
            frameClientConnected.set(false)
            updateFrameConnection(candidatePeerHost,
                text(R.string.tcp_inbound_priority, "TCP inbound priority: $message", message))
            return
        }
        frameServerConnected.set(false)
        updateFrameConnection(null, text(R.string.tcp_inbound, "TCP inbound: $message", message))
        val fallbackHost = peerHost
        if (!fallbackHost.isNullOrBlank()) ensureFrameClient(fallbackHost, port)
    }

    @Synchronized
    private fun ensureFrameClient(host: String, port: Int) {
        if (!running.get() || host.isBlank()) return
        if (frameServerConnected.get()) return
        if (frameClientHost == host && frameClient != null) return
        frameClientGeneration += 1L
        val generation = frameClientGeneration
        frameClient?.close()
        frameClient = null
        frameClientHost = host
        frameClientConnected.set(false)
        val client = HilFrameStreamClient(host, port, object : HilFrameStreamClient.Listener {
            override fun onFrame(frame: HilFrameProtocol.Frame) {
                if (generation != frameClientGeneration || frameServerConnected.get()) return
                frameStore.offer(frame)
            }

            override fun onFrameConnectionChanged(connected: Boolean, message: String) {
                if (generation != frameClientGeneration || frameServerConnected.get()) return
                frameClientConnected.set(connected)
                updateFrameConnection(if (connected) host else null,
                    text(R.string.tcp_outbound, "TCP outbound: $message", message))
            }
        })
        frameClient = client
        client.start()
    }

    @Synchronized
    private fun resetHotspotPeerConnections() {
        peerHost = null
        frameClientGeneration += 1L
        frameClient?.close()
        frameClient = null
        frameClientHost = null
        frameClientConnected.set(false)
        frames?.disconnectClient()
        frameServerConnected.set(false)
        frameConnected.set(false)
        framePeerHost = null
        frameStore.clear()
        latestStatus.get()?.copy(
            peerHost = null,
            peerFresh = false,
            frameConnected = false,
            framePeerHost = null,
            frame = null,
            message = text(
                R.string.hotspot_peer_lost_rediscovering,
                "Hotspot peer lost; restarting broadcast discovery",
            ),
        )?.let { updated ->
            latestStatus.set(updated)
            listener.onHilStatus(updated)
        }
    }

    private fun updateFrameConnection(candidatePeerHost: String?, message: String) {
        val connected = frameServerConnected.get() || frameClientConnected.get()
        val wasConnected = frameConnected.getAndSet(connected)
        if (connected && !wasConnected) frameStore.clear()
        framePeerHost = when {
            frameServerConnected.get() && candidatePeerHost != null -> candidatePeerHost
            frameClientConnected.get() -> frameClientHost
            frameServerConnected.get() -> peerHost
            else -> null
        }
        latestStatus.get()?.copy(
            frameConnected = connected,
            framePeerHost = framePeerHost,
            frame = frameStore.snapshot(),
            message = message,
        )?.let { updated ->
            latestStatus.set(updated)
            listener.onHilStatus(updated)
        }
    }

    override fun close() = stop()

    private fun text(resourceId: Int, fallback: String, vararg arguments: Any): String =
        appContext?.getString(resourceId, *arguments) ?: fallback
}
