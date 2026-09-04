package edu.playground.djivln.reconstruction

import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V86OfflineImageCompressorInstrumentedTest {
    @Test fun recompressesLargePhotoAndRebuildsGpsExif() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(context.cacheDir, "offline-large.jpg")
        val output = File(context.cacheDir, "offline-large-output.jpg")
        try {
            val width = 1400; val height = 1000
            val pixels = IntArray(width * height) { index ->
                val value = (index * 1103515245 + 12345)
                0xff000000.toInt() or (value and 0x00ffffff)
            }
            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888).apply {
                file.outputStream().use { compress(Bitmap.CompressFormat.JPEG, 98, it) }
                recycle()
            }
            ExifInterface(file.absolutePath).apply {
                setLatLong(31.2, 121.3); setAltitude(55.0)
                setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, "2026:08:30 12:00:00")
                setAttribute(ExifInterface.TAG_MAKE, "DJI"); setAttribute(ExifInterface.TAG_MODEL, "M30T")
                saveAttributes()
            }
            val prepared = V86OfflineImageCompressor(context).prepare(file.name, file.inputStream())
            assertTrue(prepared.recompressed)
            assertTrue(prepared.bytes.size <= V86OfflineImageCompressor.HARD_LIMIT_BYTES)
            output.writeBytes(prepared.bytes)
            val exif = ExifInterface(output.absolutePath)
            assertEquals(31.2, exif.latLong!![0], 1e-5)
            assertEquals(55.0, exif.getAltitude(Double.NaN), 0.1)
        } finally { file.delete(); output.delete() }
    }

    @Test fun validatesAndPreservesStandardGpsExif() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(context.cacheDir, "offline-source.jpg")
        try {
            file.outputStream().use { output ->
                Bitmap.createBitmap(64, 48, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(0xff556677.toInt())
                    compress(Bitmap.CompressFormat.JPEG, 90, output)
                    recycle()
                }
            }
            ExifInterface(file.absolutePath).apply {
                setLatLong(31.1, 121.2); setAltitude(42.5)
                setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, "2026:08:30 12:00:00")
                setAttribute(ExifInterface.TAG_MAKE, "DJI")
                setAttribute(ExifInterface.TAG_MODEL, "M30T")
                saveAttributes()
            }
            val prepared = V86OfflineImageCompressor(context).prepare("left_DJI_0001.jpg", file.inputStream())
            assertFalse(prepared.recompressed)
            assertEquals(31.1, prepared.inspection.latitude, 1e-5)
            assertEquals(42.5, prepared.inspection.altitudeMeters, 0.1)
            assertEquals("LEFT_OBLIQUE", prepared.inspection.captureView)
        } finally { file.delete() }
    }

    @Test fun batchPreflightRejectsImageWithoutGps() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(context.cacheDir, "offline-no-gps.jpg")
        try {
            file.outputStream().use { output ->
                Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888).apply {
                    compress(Bitmap.CompressFormat.JPEG, 90, output); recycle()
                }
            }
            val result = runCatching {
                V86OfflineImageCompressor(context).validate(file.name, file.inputStream())
            }
            assertTrue(result.isFailure)
        } finally { file.delete() }
    }

    @Test fun rejectsOversizedStreamWithoutHoldingItAllInMemory() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        var remaining = V86OfflineImageCompressor.MAX_INPUT_BYTES.toLong() + 1L
        val stream = object : InputStream() {
            override fun read(): Int = if (remaining-- > 0L) 0 else -1
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                if (remaining <= 0L) return -1
                val count = minOf(length.toLong(), remaining).toInt()
                java.util.Arrays.fill(buffer, offset, offset + count, 0.toByte())
                remaining -= count
                return count
            }
        }
        val result = runCatching {
            V86OfflineImageCompressor(context).validate("oversized.jpg", stream)
        }
        assertTrue(result.exceptionOrNull()?.message?.contains("64 MB") == true)
    }
}
