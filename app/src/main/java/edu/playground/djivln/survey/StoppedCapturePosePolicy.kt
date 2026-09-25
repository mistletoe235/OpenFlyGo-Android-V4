package edu.playground.djivln.survey

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot

/** Shared MSDK4 stopped-capture gate: arrival alone is not a photo trigger. */
object StoppedCapturePosePolicy {
    const val REQUIRED_STABLE_MILLIS = 800L
    const val MAX_HORIZONTAL_SPEED_METERS_PER_SECOND = 0.35
    const val MAX_HORIZONTAL_ERROR_METERS = 1.5
    const val MAX_ALTITUDE_ERROR_METERS = 1.0
    const val MAX_HEADING_ERROR_DEGREES = 3.0
    private const val MAX_SAMPLE_AGE_MILLIS = 1_000L

    @JvmStatic
    fun requiresStoppedPose(action: CaptureAction): Boolean = action == CaptureAction.CAPTURE_ON_REACH

    @JvmStatic
    fun aligned(
        connected: Boolean,
        headingDegrees: Double,
        gimbalPitchDegrees: Double,
        altitudeMeters: Double,
        position: GeoPoint,
        target: SurveyWaypoint,
        horizontalSpeedMetersPerSecond: Double,
        nowEpochMillis: Long,
        flightStateUpdatedAtMillis: Long,
        gimbalStateUpdatedAtMillis: Long,
    ): Boolean {
        fun fresh(updatedAt: Long): Boolean = updatedAt > 0L && nowEpochMillis >= updatedAt &&
            nowEpochMillis - updatedAt <= MAX_SAMPLE_AGE_MILLIS
        return connected && headingDegrees.isFinite() && gimbalPitchDegrees.isFinite() &&
            altitudeMeters.isFinite() && fresh(flightStateUpdatedAtMillis) &&
            fresh(gimbalStateUpdatedAtMillis) &&
            horizontalSpeedMetersPerSecond.isFinite() &&
            horizontalSpeedMetersPerSecond in 0.0..MAX_HORIZONTAL_SPEED_METERS_PER_SECOND &&
            distanceMeters(position, target.point) <= MAX_HORIZONTAL_ERROR_METERS &&
            abs(altitudeMeters - target.point.altitudeMeters) <= MAX_ALTITUDE_ERROR_METERS &&
            angleDifference(headingDegrees, target.headingDegrees) <= MAX_HEADING_ERROR_DEGREES &&
            SurveyGimbalSettlePolicy.isSettled(target.gimbalPitchDegrees, gimbalPitchDegrees)
    }

    @JvmStatic
    fun stable(stableSinceMillis: Long, nowElapsedMillis: Long): Boolean =
        stableSinceMillis > 0L && nowElapsedMillis - stableSinceMillis >= REQUIRED_STABLE_MILLIS

    @JvmStatic
    fun updateStableSince(aligned: Boolean, previous: Long, nowElapsedMillis: Long): Long = when {
        !aligned -> 0L
        previous > 0L -> previous
        else -> nowElapsedMillis
    }

    private fun distanceMeters(first: GeoPoint, second: GeoPoint): Double {
        val north = (second.latitude - first.latitude) * 111_132.0
        val east = (second.longitude - first.longitude) * 111_320.0 *
            cos(Math.toRadians((first.latitude + second.latitude) / 2.0))
        return hypot(north, east)
    }

    private fun angleDifference(first: Double, second: Double): Double =
        abs(((second - first) % 360.0 + 540.0) % 360.0 - 180.0)
}
