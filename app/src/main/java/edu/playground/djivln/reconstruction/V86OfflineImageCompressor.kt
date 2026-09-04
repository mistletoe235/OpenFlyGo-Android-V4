package edu.playground.djivln.reconstruction

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.sqrt

class V86OfflineImageCompressor(private val context: Context) {
    data class Inspection(
        val latitude: Double,
        val longitude: Double,
        val altitudeMeters: Double,
        val timestamp: String?,
        val make: String?,
        val model: String?,
        val captureView: String,
    )
    data class PreparedImage(
        val displayName: String,
        val bytes: ByteArray,
        val inspection: Inspection,
        val recompressed: Boolean,
    )

    fun validate(displayName: String, input: InputStream): Inspection =
        withBoundedInputFile(input) { file -> inspect(displayName, file) }

    fun prepare(displayName: String, input: InputStream): PreparedImage =
        withBoundedInputFile(input) { originalFile ->
        val inspection = inspect(displayName, originalFile)
        if (originalFile.length() <= TARGET_BYTES) {
            return@withBoundedInputFile PreparedImage(
                displayName, originalFile.readBytes(), inspection, false)
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(originalFile.absolutePath, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Cannot decode $displayName" }
        var sample = 1
        while (bounds.outWidth / sample > MAX_DECODE_DIMENSION ||
            bounds.outHeight / sample > MAX_DECODE_DIMENSION ||
            bounds.outWidth.toLong() * bounds.outHeight.toLong() / sample / sample > MAX_DECODE_PIXELS
        ) sample *= 2
        val bitmap = BitmapFactory.decodeFile(
            originalFile.absolutePath,
            BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            },
        ) ?: error("Cannot decode $displayName")
        try {
            var scale = sqrt(TARGET_BYTES.toDouble() / originalFile.length()).coerceIn(0.25, 1.0)
            var encoded = ByteArray(0)
            for (attempt in 0 until 5) {
                val width = max(320, (bitmap.width * scale).toInt())
                val height = max(240, (bitmap.height * scale).toInt())
                val resized = if (width == bitmap.width && height == bitmap.height) bitmap
                    else Bitmap.createScaledBitmap(bitmap, width, height, true)
                try {
                    for (quality in intArrayOf(90, 84, 78, 72, 66, 60)) {
                        encoded = ByteArrayOutputStream().also { output ->
                            check(resized.compress(Bitmap.CompressFormat.JPEG, quality, output))
                        }.toByteArray()
                        if (encoded.size <= TARGET_BYTES) break
                    }
                } finally { if (resized !== bitmap) resized.recycle() }
                if (encoded.size <= TARGET_BYTES) break
                scale *= 0.82
            }
            require(encoded.isNotEmpty() && encoded.size <= HARD_LIMIT_BYTES) {
                "Cannot compress $displayName below 640 KiB"
            }
            val withExif = rewriteStandardExif(encoded, inspection)
            val xmp = originalFile.inputStream().use { stream ->
                JpegMetadataSegments.extractSelfContainedXmp(stream.readAtMost(MAX_METADATA_PREFIX_BYTES))
            }
            val result = JpegMetadataSegments.injectAfterExif(withExif, xmp)
            require(result.size <= HARD_LIMIT_BYTES) { "Preserved metadata exceeds 640 KiB hard limit" }
            return PreparedImage(displayName, result, inspection, true)
        } finally { bitmap.recycle() }
    }

    private fun inspect(displayName: String, file: File): Inspection {
        require(file.length() >= 128) { "$displayName is empty" }
        val exif = ExifInterface(file.absolutePath)
        val location = exif.latLong ?: error("$displayName has no EXIF GPS")
        val altitude = exif.getAltitude(Double.NaN)
        require(altitude.isFinite()) { "$displayName has no GPSAltitude" }
        val timestamp = exifTimestampToIso(
            exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME),
            exif.getAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL)
                ?: exif.getAttribute(ExifInterface.TAG_OFFSET_TIME),
        )
        return Inspection(location[0], location[1], altitude,
            timestamp,
            exif.getAttribute(ExifInterface.TAG_MAKE),
            exif.getAttribute(ExifInterface.TAG_MODEL), inferCaptureView(displayName))
    }

    private fun rewriteStandardExif(jpeg: ByteArray, inspection: Inspection): ByteArray {
        val file = File(context.cacheDir, "v86-offline-${System.nanoTime()}.jpg")
        try {
            file.writeBytes(jpeg)
            val exif = ExifInterface(file.absolutePath)
            exif.setLatLong(inspection.latitude, inspection.longitude)
            exif.setAltitude(inspection.altitudeMeters)
            inspection.make?.let { exif.setAttribute(ExifInterface.TAG_MAKE, it) }
            inspection.model?.let { exif.setAttribute(ExifInterface.TAG_MODEL, it) }
            inspection.timestamp?.let { iso ->
                isoTimestampToExif(iso)?.let {
                    exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, it)
                    exif.setAttribute(ExifInterface.TAG_DATETIME, it)
                }
            }
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, "OpenFly Go V4 offline replay")
            exif.saveAttributes()
            return file.readBytes()
        } finally { file.delete() }
    }

    private fun inferCaptureView(name: String): String {
        val value = name.lowercase(Locale.US)
        return when {
            "backward" in value || "back" in value -> "BACKWARD_OBLIQUE"
            "forward" in value || "front" in value -> "FORWARD_OBLIQUE"
            "left" in value -> "LEFT_OBLIQUE"
            "right" in value -> "RIGHT_OBLIQUE"
            else -> "NADIR"
        }
    }

    private fun exifTimestampToIso(value: String?, offset: String?): String? {
        if (value.isNullOrBlank() || offset.isNullOrBlank()) return null
        return runCatching {
        val input = SimpleDateFormat("yyyy:MM:dd HH:mm:ssXXX", Locale.US)
        val output = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        output.format(requireNotNull(input.parse(value + offset)))
        }.getOrNull()
    }

    private inline fun <T> withBoundedInputFile(input: InputStream, block: (File) -> T): T {
        val file = File.createTempFile("v86-offline-input-", ".jpg", context.cacheDir)
        try {
            file.outputStream().use { output ->
                val buffer = ByteArray(COPY_BUFFER_BYTES)
                var total = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (count == 0) continue
                    total += count
                    require(total <= MAX_INPUT_BYTES) { "Image exceeds 64 MB input limit" }
                    output.write(buffer, 0, count)
                }
            }
            return block(file)
        } finally {
            file.delete()
        }
    }

    private fun InputStream.readAtMost(maximumBytes: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(COPY_BUFFER_BYTES)
        var remaining = maximumBytes
        while (remaining > 0) {
            val count = read(buffer, 0, minOf(buffer.size, remaining))
            if (count < 0) break
            if (count == 0) continue
            output.write(buffer, 0, count)
            remaining -= count
        }
        return output.toByteArray()
    }

    private fun isoTimestampToExif(value: String): String? = runCatching {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val output = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        output.format(requireNotNull(input.parse(value)))
    }.getOrNull()

    companion object {
        const val TARGET_BYTES = 600 * 1024
        const val HARD_LIMIT_BYTES = 640 * 1024
        const val MAX_INPUT_BYTES = 64 * 1024 * 1024
        private const val MAX_METADATA_PREFIX_BYTES = 4 * 1024 * 1024
        private const val MAX_DECODE_DIMENSION = 4096
        private const val MAX_DECODE_PIXELS = 8_000_000L
        private const val COPY_BUFFER_BYTES = 64 * 1024
    }
}

object JpegMetadataSegments {
    private val XMP_SIGNATURE = "http://ns.adobe.com/xap/1.0/\u0000".toByteArray(Charsets.US_ASCII)

    fun extractSelfContainedXmp(jpeg: ByteArray): List<ByteArray> = segments(jpeg).filter { segment ->
        segment.size > 4 + XMP_SIGNATURE.size && segment[1] == 0xE1.toByte() &&
            segment.copyOfRange(4, 4 + XMP_SIGNATURE.size).contentEquals(XMP_SIGNATURE)
    }

    fun injectAfterExif(jpeg: ByteArray, app1: List<ByteArray>): ByteArray {
        if (app1.isEmpty()) return jpeg
        require(jpeg.size >= 2 && jpeg[0] == 0xff.toByte() && jpeg[1] == 0xd8.toByte())
        var offset = 2
        while (offset + 4 <= jpeg.size && jpeg[offset] == 0xff.toByte()) {
            val marker = jpeg[offset + 1].toInt() and 0xff
            if (marker !in 0xe0..0xef) break
            val length = ((jpeg[offset + 2].toInt() and 0xff) shl 8) or (jpeg[offset + 3].toInt() and 0xff)
            if (length < 2 || offset + 2 + length > jpeg.size) break
            offset += 2 + length
        }
        return ByteArrayOutputStream(jpeg.size + app1.sumOf(ByteArray::size)).apply {
            write(jpeg, 0, offset); app1.forEach { write(it) }; write(jpeg, offset, jpeg.size - offset)
        }.toByteArray()
    }

    private fun segments(jpeg: ByteArray): List<ByteArray> {
        if (jpeg.size < 4 || jpeg[0] != 0xff.toByte() || jpeg[1] != 0xd8.toByte()) return emptyList()
        val result = mutableListOf<ByteArray>()
        var offset = 2
        while (offset + 4 <= jpeg.size && jpeg[offset] == 0xff.toByte()) {
            val marker = jpeg[offset + 1].toInt() and 0xff
            if (marker == 0xda || marker == 0xd9) break
            val length = ((jpeg[offset + 2].toInt() and 0xff) shl 8) or (jpeg[offset + 3].toInt() and 0xff)
            if (length < 2 || offset + 2 + length > jpeg.size) break
            result += jpeg.copyOfRange(offset, offset + 2 + length)
            offset += 2 + length
        }
        return result
    }
}
