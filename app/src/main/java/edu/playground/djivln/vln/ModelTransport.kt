package edu.playground.djivln.vln

enum class ModelTransport { LOCAL, USB, ETHERNET }

object ModelEndpoint {
    const val LOCAL_BASE = "disabled://local"
    const val USB_BASE = "disabled://usb"
    const val DEFAULT_ETHERNET_BASE = "disabled://ethernet"
    const val LEGACY_CONFLICTING_ETHERNET_BASE = DEFAULT_ETHERNET_BASE
    fun baseUrl(transport: ModelTransport, ethernetInput: String): String = "disabled://${transport.name.lowercase()}"
    fun inferenceUrl(transport: ModelTransport, ethernetInput: String): String = baseUrl(transport, ethernetInput)
    fun normalizeEthernetBase(input: String): String = DEFAULT_ETHERNET_BASE
}
