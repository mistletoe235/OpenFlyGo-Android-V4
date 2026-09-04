package edu.playground.djivln.hil

import java.io.DataInputStream
import java.io.EOFException
import java.util.zip.CRC32

/** Length-prefixed, persistent TCP frame stream. A newer complete frame always replaces the older one. */
object HilFrameProtocol {
    const val MAGIC = 0x4f464652 // OFFR
    const val VERSION: Short = 1
    const val HEADER_BYTES = 56
    const val FORMAT_JPEG: Short = 1
    const val FORMAT_PNG: Short = 2
    const val MAX_FRAME_BYTES = 16 * 1024 * 1024

    data class Frame(
        val format: Short,
        val frameId: Long,
        val poseSequence: Long,
        val capturePeerMonotonicNanos: Long,
        val receivedAndroidMonotonicNanos: Long,
        val width: Int,
        val height: Int,
        val flags: Int,
        val encoded: ByteArray,
    )

    fun read(input: DataInputStream, receivedAndroidMonotonicNanos: Long): Frame =
        read(input) { receivedAndroidMonotonicNanos }

    /** Captures receive age immediately after the first frame-header word becomes available. */
    fun read(input: DataInputStream, receiveClock: () -> Long): Frame {
        val magic = input.readInt()
        if (magic != MAGIC) throw IllegalArgumentException("invalid HIL frame magic")
        val receivedAndroidMonotonicNanos = receiveClock()
        val version = input.readShort()
        require(version == VERSION) { "unsupported HIL frame version" }
        val format = input.readShort()
        require(format == FORMAT_JPEG || format == FORMAT_PNG) { "unsupported frame format $format" }
        require(input.readInt() == HEADER_BYTES) { "invalid HIL frame header size" }
        val payloadBytes = input.readInt()
        require(payloadBytes in 1..MAX_FRAME_BYTES) { "invalid HIL frame payload size" }
        val frameId = input.readLong()
        val poseSequence = input.readLong()
        val captureNanos = input.readLong()
        val width = input.readInt()
        val height = input.readInt()
        val flags = input.readInt()
        val expectedCrc = input.readInt().toLong() and 0xffff_ffffL
        require(width in 1..8192 && height in 1..8192) { "invalid HIL frame dimensions" }
        val payload = ByteArray(payloadBytes)
        try {
            input.readFully(payload)
        } catch (error: EOFException) {
            throw EOFException("truncated HIL frame $frameId: ${error.message}")
        }
        val actualCrc = CRC32().apply { update(payload) }.value
        require(actualCrc == expectedCrc) { "HIL frame CRC mismatch" }
        return Frame(
            format, frameId, poseSequence, captureNanos, receivedAndroidMonotonicNanos,
            width, height, flags, payload,
        )
    }
}
