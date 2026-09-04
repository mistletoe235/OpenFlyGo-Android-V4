package edu.playground.djivln.reconstruction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JpegMetadataSegmentsTest {
    @Test fun `extracts and reinjects self contained DJI XMP`() {
        val signature = "http://ns.adobe.com/xap/1.0/\u0000".toByteArray(Charsets.US_ASCII)
        val payload = signature + "<x:xmpmeta>DJI</x:xmpmeta>".toByteArray()
        val length = payload.size + 2
        val xmp = byteArrayOf(0xff.toByte(), 0xe1.toByte(),
            (length ushr 8).toByte(), length.toByte()) + payload
        val source = byteArrayOf(0xff.toByte(), 0xd8.toByte()) + xmp +
            byteArrayOf(0xff.toByte(), 0xd9.toByte())
        val segments = JpegMetadataSegments.extractSelfContainedXmp(source)
        assertEquals(1, segments.size)

        val target = byteArrayOf(0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0xd9.toByte())
        val injected = JpegMetadataSegments.injectAfterExif(target, segments)
        assertTrue(injected.asList().windowed(signature.size).any { window ->
            window.toByteArray().contentEquals(signature)
        })
    }
}
