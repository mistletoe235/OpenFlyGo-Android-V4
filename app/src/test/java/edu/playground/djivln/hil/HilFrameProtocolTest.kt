package edu.playground.djivln.hil

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.zip.CRC32
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class HilFrameProtocolTest {
    @Test
    fun readsCompleteCrcCheckedFrame() {
        val payload = byteArrayOf(1, 2, 3, 4, 5)
        val bytes = frameBytes(payload, CRC32().apply { update(payload) }.value)
        val frame = HilFrameProtocol.read(DataInputStream(ByteArrayInputStream(bytes)), 999L)
        assertEquals(11L, frame.frameId)
        assertEquals(12L, frame.poseSequence)
        assertEquals(1440, frame.width)
        assertEquals(1080, frame.height)
        assertArrayEquals(payload, frame.encoded)
    }

    @Test
    fun rejectsCrcMismatch() {
        val bytes = frameBytes(byteArrayOf(1, 2, 3), 0L)
        assertThrows(IllegalArgumentException::class.java) {
            HilFrameProtocol.read(DataInputStream(ByteArrayInputStream(bytes)), 1L)
        }
    }

    @Test
    fun `receive clock is sampled after the first header word arrives`() {
        val payload = byteArrayOf(9, 8, 7)
        val bytes = frameBytes(payload, CRC32().apply { update(payload) }.value)
        val source = object : ByteArrayInputStream(bytes) {
            fun consumedBytes(): Int = pos
        }
        val frame = HilFrameProtocol.read(DataInputStream(source)) {
            assertTrue(source.consumedBytes() >= Int.SIZE_BYTES)
            777L
        }

        assertEquals(777L, frame.receivedAndroidMonotonicNanos)
    }

    private fun frameBytes(payload: ByteArray, crc: Long): ByteArray = ByteArrayOutputStream().also { output ->
        DataOutputStream(output).use { data ->
            data.writeInt(HilFrameProtocol.MAGIC)
            data.writeShort(HilFrameProtocol.VERSION.toInt())
            data.writeShort(HilFrameProtocol.FORMAT_JPEG.toInt())
            data.writeInt(HilFrameProtocol.HEADER_BYTES)
            data.writeInt(payload.size)
            data.writeLong(11L)
            data.writeLong(12L)
            data.writeLong(13L)
            data.writeInt(1440)
            data.writeInt(1080)
            data.writeInt(0)
            data.writeInt(crc.toInt())
            data.write(payload)
        }
    }.toByteArray()
}
