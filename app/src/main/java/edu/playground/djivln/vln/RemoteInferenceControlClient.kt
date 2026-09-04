package edu.playground.djivln.vln

class RemoteInferenceControlClient(
    baseUrl: String,
    usbTransport: AoaUsbProbe? = null,
    connectTimeoutMs: Int = 800,
    readTimeoutMs: Int = 2_000,
) : InferenceControlClient {
    private val unavailable = InferenceControlClient.Result(false, 501, "Remote inference is not included")
    override fun health() = unavailable
    override fun load() = unavailable
    override fun preflight() = unavailable
    override fun reset() = unavailable
    override fun start(prompt: String) = unavailable
    override fun stop() = unavailable
}
