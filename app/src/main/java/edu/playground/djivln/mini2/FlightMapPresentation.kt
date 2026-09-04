package edu.playground.djivln.mini2

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

object FlightMapPresentation {
    const val MIN_RC_COURSE_SPEED_METERS_PER_SECOND = 0.35

    @JvmStatic
    fun courseDegrees(eastMetersPerSecond: Double, northMetersPerSecond: Double): Double? {
        if (!eastMetersPerSecond.isFinite() || !northMetersPerSecond.isFinite()) return null
        if (hypot(eastMetersPerSecond, northMetersPerSecond) < MIN_RC_COURSE_SPEED_METERS_PER_SECOND) {
            return null
        }
        return normalizeDegrees(Math.toDegrees(atan2(eastMetersPerSecond, northMetersPerSecond)))
    }

    @JvmStatic
    fun bearingDegrees(fromLatitude: Double, fromLongitude: Double, toLatitude: Double, toLongitude: Double): Double {
        val fromLat = Math.toRadians(fromLatitude)
        val toLat = Math.toRadians(toLatitude)
        val deltaLon = Math.toRadians(toLongitude - fromLongitude)
        val y = sin(deltaLon) * cos(toLat)
        val x = cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(deltaLon)
        return normalizeDegrees(Math.toDegrees(atan2(y, x)))
    }

    @JvmStatic
    fun distanceMeters(fromLatitude: Double, fromLongitude: Double, toLatitude: Double, toLongitude: Double): Double {
        val radius = 6_371_000.0
        val dLat = Math.toRadians(toLatitude - fromLatitude)
        val dLon = Math.toRadians(toLongitude - fromLongitude)
        val lat1 = Math.toRadians(fromLatitude)
        val lat2 = Math.toRadians(toLatitude)
        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
            cos(lat1) * cos(lat2) * sin(dLon / 2.0) * sin(dLon / 2.0)
        return 2.0 * radius * atan2(sqrt(a.coerceIn(0.0, 1.0)), sqrt((1.0 - a).coerceIn(0.0, 1.0)))
    }

    @JvmStatic
    fun directionArrow(bearingDegrees: Double): String {
        val arrows = arrayOf("↑", "↗", "→", "↘", "↓", "↙", "←", "↖")
        val index = ((normalizeDegrees(bearingDegrees) + 22.5) / 45.0).toInt() % arrows.size
        return arrows[index]
    }

    @JvmStatic
    fun mapMarkerRotationDegrees(headingDegrees: Double): Float =
        (360.0 - normalizeDegrees(headingDegrees)).toFloat()

    private fun normalizeDegrees(value: Double): Double = (value % 360.0 + 360.0) % 360.0
}
