package edu.playground.djivln.camera

import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import org.json.JSONObject

data class TriggerFrameMetadata(
    val capturedAtEpochMillis: Long,
    val telemetryUpdatedAtEpochMillis: Long,
    val triggerReason: String,
    val captureView: String,
    val product: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val aslMeters: Double,
    val groundClearanceMeters: Double,
    val headingDegrees: Double,
    val aircraftPitchDegrees: Double,
    val gimbalPitchDegrees: Double,
    val velocityNorthMetersPerSecond: Double,
    val velocityEastMetersPerSecond: Double,
    val velocityDownMetersPerSecond: Double,
    val missionId: String?,
    val executionLegIndex: Int?,
    val waypointIndex: Int?,
) {
    val gpsAgeMillis: Long
        get() = capturedAtEpochMillis - telemetryUpdatedAtEpochMillis

    val hasFreshAircraftGps: Boolean
        get() = gpsAgeMillis in 0..MAX_AIRCRAFT_GPS_AGE_MILLIS &&
            latitude.isFinite() && latitude in -90.0..90.0 &&
            longitude.isFinite() && longitude in -180.0..180.0 &&
            !(latitude == 0.0 && longitude == 0.0)

    val trustedAslMeters: Double?
        get() = aslMeters.takeIf {
            hasFreshAircraftGps && it.isFinite() &&
                !(kotlin.math.abs(it) < 0.01 && altitudeMeters.isFinite() &&
                    kotlin.math.abs(altitudeMeters) > 2.0)
        }

    fun toJson(imagePath: String? = null): JSONObject = JSONObject()
        .put("schema", "openfly.trigger-frame.v1")
        .put("captured_at_epoch_ms", capturedAtEpochMillis)
        .put("telemetry_updated_at_epoch_ms", telemetryUpdatedAtEpochMillis)
        .put("gps_age_ms", gpsAgeMillis)
        .put("trigger_reason", triggerReason)
        .put("capture_view", captureView)
        .put("product", product)
        .put("latitude", finiteOrNull(latitude))
        .put("longitude", finiteOrNull(longitude))
        .put("altitude_m", finiteOrNull(altitudeMeters))
        .put("asl_m", finiteOrNull(aslMeters))
        .put("ground_clearance_m", finiteOrNull(groundClearanceMeters))
        .put("heading_deg", finiteOrNull(headingDegrees))
        .put("aircraft_pitch_deg", finiteOrNull(aircraftPitchDegrees))
        .put("gimbal_pitch_deg", finiteOrNull(gimbalPitchDegrees))
        .put("velocity_north_mps", finiteOrNull(velocityNorthMetersPerSecond))
        .put("velocity_east_mps", finiteOrNull(velocityEastMetersPerSecond))
        .put("velocity_down_mps", finiteOrNull(velocityDownMetersPerSecond))
        .put("mission_id", missionId ?: JSONObject.NULL)
        .put("execution_leg_index", executionLegIndex ?: JSONObject.NULL)
        .put("waypoint_index", waypointIndex ?: JSONObject.NULL)
        .put("image_path", imagePath ?: JSONObject.NULL)

    private fun finiteOrNull(value: Double): Any = if (value.isFinite()) value else JSONObject.NULL

    private companion object {
        const val MAX_AIRCRAFT_GPS_AGE_MILLIS = 2_000L
    }
}

object SurveyFrameExifWriter {
    fun write(jpeg: ByteArray, metadata: TriggerFrameMetadata, cacheDirectory: File): ByteArray {
        require(jpeg.isNotEmpty())
        cacheDirectory.mkdirs()
        val file = File(cacheDirectory, "trigger-${UUID.randomUUID()}.jpg")
        try {
            file.writeBytes(jpeg)
            val exif = ExifInterface(file.absolutePath)
            val formatter = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL,
                formatter.format(Date(metadata.capturedAtEpochMillis)))
            exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
                "%03d".format(Locale.US, metadata.capturedAtEpochMillis % 1_000L))
            exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL, "+00:00")
            exif.setAttribute(ExifInterface.TAG_MAKE, "DJI")
            exif.setAttribute(ExifInterface.TAG_MODEL, metadata.product)
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, "OpenFly Go V4")
            if (metadata.hasFreshAircraftGps) {
                exif.setLatLong(metadata.latitude, metadata.longitude)
                metadata.trustedAslMeters?.let(exif::setAltitude)
            }
            if (metadata.hasFreshAircraftGps && metadata.headingDegrees.isFinite()) {
                val heading = (metadata.headingDegrees % 360.0 + 360.0) % 360.0
                exif.setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION_REF, "T")
                exif.setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION,
                    rational(heading, 1_000L))
            }
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, metadata.toJson().toString())
            exif.saveAttributes()
            return file.readBytes()
        } finally {
            file.delete()
        }
    }

    private fun rational(value: Double, denominator: Long): String =
        "${(value * denominator).toLong()}/$denominator"
}
