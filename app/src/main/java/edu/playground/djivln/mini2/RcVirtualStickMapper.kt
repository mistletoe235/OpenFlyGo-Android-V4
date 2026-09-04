package edu.playground.djivln.mini2

import kotlin.math.abs

/** DJI Mode 2 RC axes converted to body-frame Virtual Stick velocity commands. */
object RcVirtualStickMapper {
    data class Command(
        val forwardMetersPerSecond: Float,
        val rightMetersPerSecond: Float,
        val upMetersPerSecond: Float,
        val yawRateDegreesPerSecond: Float,
    )

    fun map(
        leftHorizontal: Int,
        leftVertical: Int,
        rightHorizontal: Int,
        rightVertical: Int,
    ): Command = Command(
        forwardMetersPerSecond = scale(rightVertical, MAX_HORIZONTAL_SPEED),
        rightMetersPerSecond = scale(rightHorizontal, MAX_HORIZONTAL_SPEED),
        upMetersPerSecond = scale(leftVertical, MAX_VERTICAL_SPEED),
        yawRateDegreesPerSecond = scale(leftHorizontal, MAX_YAW_RATE),
    )

    private fun scale(raw: Int, maximum: Float): Float {
        if (abs(raw) <= DEADBAND) return 0f
        return (raw.toFloat() / FULL_SCALE).coerceIn(-1f, 1f) * maximum
    }

    private const val DEADBAND = 25
    private const val FULL_SCALE = 660f
    private const val MAX_HORIZONTAL_SPEED = 4f
    private const val MAX_VERTICAL_SPEED = 2f
    private const val MAX_YAW_RATE = 90f
}
