package edu.playground.djivln.vln

import android.content.Context

/** Public-build stub. USB inference transport is not part of this release. */
class AoaUsbProbe(
    @Suppress("UNUSED_PARAMETER") context: Context,
    @Suppress("UNUSED_PARAMETER") report: (String) -> Unit,
) : AutoCloseable {
    data class Response(val statusCode: Int, val body: String)
    fun start() = Unit
    fun reconnect() = Unit
    fun disable() = Unit
    fun isConnected(): Boolean = false
    fun request(method: String, path: String, body: String? = null): Response =
        throw UnsupportedOperationException("Remote inference is not included in this public release")
    override fun close() = Unit
}
