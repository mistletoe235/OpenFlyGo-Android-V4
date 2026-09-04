package edu.playground.djivln.survey

import kotlin.math.abs
import kotlin.math.cos

data class SurveySimulatorMapPose(
    val point: GeoPoint,
    val headingDegrees: Double,
)

/** Projects one fresh, authoritative DJI Simulator RAW sample onto WGS-84. */
object SurveySimulatorMapProjection {
    fun project(
        simulatorActive: Boolean,
        sampleElapsedRealtimeNanos: Long,
        nowElapsedRealtimeNanos: Long,
        maximumAgeNanos: Long,
        originLatitude: Double,
        originLongitude: Double,
        eastMeters: Double,
        northMeters: Double,
        downMeters: Double,
        yawDegrees: Double,
    ): SurveySimulatorMapPose? {
        if (!simulatorActive || maximumAgeNanos <= 0L || sampleElapsedRealtimeNanos <= 0L ||
            nowElapsedRealtimeNanos < sampleElapsedRealtimeNanos ||
            nowElapsedRealtimeNanos - sampleElapsedRealtimeNanos > maximumAgeNanos ||
            !originLatitude.isFinite() || !originLongitude.isFinite() ||
            !eastMeters.isFinite() || !northMeters.isFinite() || !downMeters.isFinite() ||
            !yawDegrees.isFinite() || originLatitude !in -90.0..90.0 ||
            originLongitude !in -180.0..180.0 ||
            abs(originLatitude) < 1e-9 && abs(originLongitude) < 1e-9
        ) return null
        val latitude = originLatitude + northMeters / 111_132.0
        val longitudeScale = 111_320.0 * cos(Math.toRadians(latitude))
        if (!longitudeScale.isFinite() || abs(longitudeScale) <= 1.0) return null
        val longitude = originLongitude + eastMeters / longitudeScale
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
        return SurveySimulatorMapPose(
            GeoPoint(latitude, longitude, maxOf(0.0, -downMeters)),
            (yawDegrees % 360.0 + 360.0) % 360.0,
        )
    }
}
