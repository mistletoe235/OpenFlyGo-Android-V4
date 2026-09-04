package edu.playground.djivln.camera

import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.ByteArrayOutputStream
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SurveyFrameExifWriterInstrumentedTest {
    @Test fun writesGpsTimeHeadingAndPoseComment() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val raw = ByteArrayOutputStream().also {
            Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888).apply {
                eraseColor(0xff336699.toInt())
                compress(Bitmap.CompressFormat.JPEG, 90, it)
                recycle()
            }
        }.toByteArray()
        val metadata = TriggerFrameMetadata(
            1_700_000_000_123L, 1_700_000_000_000L, "TEST", "NADIR", "DJI Test",
            31.1, 121.2, 12.3, 18.0, 2.0,
            87.5, 1.0, -45.0, 1.0, 2.0, -0.5, "mission", 3, 4)
        val encoded = SurveyFrameExifWriter.write(raw, metadata, context.cacheDir)
        val file = File(context.cacheDir, "verify-trigger-exif.jpg")
        try {
            file.writeBytes(encoded)
            val exif = ExifInterface(file.absolutePath)
            assertEquals("DJI Test", exif.getAttribute(ExifInterface.TAG_MODEL))
            assertNotNull(exif.latLong)
            assertEquals(31.1, exif.latLong!![0], 1.0e-5)
            assertEquals(121.2, exif.latLong!![1], 1.0e-5)
            assertEquals(18.0, exif.getAltitude(Double.NaN), 0.1)
            assertNotNull(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))
            assertEquals("T", exif.getAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION_REF))
            assertNotNull(exif.getAttribute(ExifInterface.TAG_USER_COMMENT))
        } finally {
            file.delete()
        }
    }

    @Test fun staleGpsIsNotWrittenToStandardExif() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val raw = ByteArrayOutputStream().also {
            Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply {
                compress(Bitmap.CompressFormat.JPEG, 90, it); recycle()
            }
        }.toByteArray()
        val metadata = TriggerFrameMetadata(
            5_001L, 1_000L, "TEST", "NADIR", "DJI Test",
            31.1, 121.2, 12.3, 18.0, 2.0,
            87.5, 1.0, -45.0, 1.0, 2.0, -0.5, null, null, null)
        val file = File(context.cacheDir, "verify-stale-trigger-exif.jpg")
        try {
            file.writeBytes(SurveyFrameExifWriter.write(raw, metadata, context.cacheDir))
            val exif = ExifInterface(file.absolutePath)
            assertEquals(null, exif.latLong)
            assertEquals(null, exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE))
        } finally { file.delete() }
    }
}
