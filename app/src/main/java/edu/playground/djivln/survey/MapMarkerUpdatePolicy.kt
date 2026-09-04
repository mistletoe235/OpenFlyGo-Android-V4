package edu.playground.djivln.survey

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot

object MapMarkerUpdatePolicy {
    const val MINIMUM_UPDATE_MILLIS = 200L
    const val MINIMUM_MOVE_METERS = 0.5
    const val MINIMUM_HEADING_DEGREES = 1.0

    enum class DeviceLocationSource { REMOTE_CONTROLLER, PHONE }
    data class DeviceLocation(val point: GeoPoint, val source: DeviceLocationSource)

    fun preferredDeviceLocation(
        remoteController: GeoPoint?,
        phone: GeoPoint?,
    ): DeviceLocation? = when {
        remoteController != null -> DeviceLocation(remoteController, DeviceLocationSource.REMOTE_CONTROLLER)
        phone != null -> DeviceLocation(phone, DeviceLocationSource.PHONE)
        else -> null
    }

    fun shouldMove(previous: GeoPoint?, current: GeoPoint): Boolean =
        previous == null || distanceMeters(previous, current) >= MINIMUM_MOVE_METERS

    fun shouldUpdate(
        previous: GeoPoint?,
        current: GeoPoint,
        previousHeading: Double?,
        currentHeading: Double?,
        lastUpdateMillis: Long,
        nowMillis: Long,
    ): Boolean {
        if (previous == null || nowMillis - lastUpdateMillis >= MINIMUM_UPDATE_MILLIS &&
            (shouldMove(previous, current) ||
                headingChanged(previousHeading, currentHeading))) return true
        return false
    }

    fun distanceMeters(from: GeoPoint, to: GeoPoint): Double {
        val north = (to.latitude - from.latitude) * 111_132.0
        val east = (to.longitude - from.longitude) * 111_320.0 *
            cos(Math.toRadians((from.latitude + to.latitude) * 0.5))
        return hypot(north, east)
    }

    fun circularHeadingDelta(from: Double, to: Double): Double =
        abs((to - from + 540.0) % 360.0 - 180.0)

    private fun headingChanged(previous: Double?, current: Double?): Boolean = when {
        previous == null && current == null -> false
        previous == null || current == null -> true
        else -> circularHeadingDelta(previous, current) >= MINIMUM_HEADING_DEGREES
    }
}
