package edu.playground.djivln.mini2

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal class VelocityModelStateEstimator {
    private var initialized = false
    private var lastTimestampMs = 0L
    private var startYawDegrees = 0.0
    private var currentYawDegrees = 0.0
    private var lastNorthMetersPerSecond = 0.0
    private var lastEastMetersPerSecond = 0.0
    private var lastUpMetersPerSecond = 0.0
    private var northMeters = 0.0
    private var eastMeters = 0.0
    private var upMeters = 0.0

    fun reset() {
        initialized = false
        lastTimestampMs = 0L
        startYawDegrees = 0.0
        currentYawDegrees = 0.0
        lastNorthMetersPerSecond = 0.0
        lastEastMetersPerSecond = 0.0
        lastUpMetersPerSecond = 0.0
        northMeters = 0.0
        eastMeters = 0.0
        upMeters = 0.0
    }

    fun update(
        timestampMs: Long,
        northMetersPerSecond: Double,
        eastMetersPerSecond: Double,
        upMetersPerSecond: Double,
        headingDegrees: Double,
    ) {
        if (timestampMs <= 0L || !listOf(
                northMetersPerSecond,
                eastMetersPerSecond,
                upMetersPerSecond,
                headingDegrees,
            ).all(Double::isFinite)
        ) return

        if (!initialized) {
            initialized = true
            lastTimestampMs = timestampMs
            startYawDegrees = headingDegrees
            currentYawDegrees = headingDegrees
            lastNorthMetersPerSecond = northMetersPerSecond
            lastEastMetersPerSecond = eastMetersPerSecond
            lastUpMetersPerSecond = upMetersPerSecond
            return
        }
        if (timestampMs <= lastTimestampMs) return

        val seconds = (timestampMs - lastTimestampMs) / 1_000.0
        if (seconds <= MAX_CONTINUOUS_SAMPLE_GAP_SECONDS) {
            northMeters += (lastNorthMetersPerSecond + northMetersPerSecond) * 0.5 * seconds
            eastMeters += (lastEastMetersPerSecond + eastMetersPerSecond) * 0.5 * seconds
            upMeters += (lastUpMetersPerSecond + upMetersPerSecond) * 0.5 * seconds
        }
        lastTimestampMs = timestampMs
        currentYawDegrees = headingDegrees
        lastNorthMetersPerSecond = northMetersPerSecond
        lastEastMetersPerSecond = eastMetersPerSecond
        lastUpMetersPerSecond = upMetersPerSecond
    }

    fun modelState(): DoubleArray {
        if (!initialized) return doubleArrayOf(0.0, 0.0, 0.0, 0.0)
        val yaw0 = Math.toRadians(startYawDegrees)
        val forward = northMeters * cos(yaw0) + eastMeters * sin(yaw0)
        val right = -northMeters * sin(yaw0) + eastMeters * cos(yaw0)
        val yawRadians = wrapRadians(Math.toRadians(currentYawDegrees - startYawDegrees))
        return doubleArrayOf(forward, right, upMeters, yawRadians)
    }

    private fun wrapRadians(value: Double): Double {
        var wrapped = value
        while (wrapped > PI) wrapped -= 2.0 * PI
        while (wrapped < -PI) wrapped += 2.0 * PI
        return wrapped
    }

    private companion object {
        const val MAX_CONTINUOUS_SAMPLE_GAP_SECONDS = 1.0
    }
}
