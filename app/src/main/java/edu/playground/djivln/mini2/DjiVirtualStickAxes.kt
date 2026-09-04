package edu.playground.djivln.mini2

/** Converts OpenFly body FRU commands through MSDK4's swapped pitch/roll API fields. */
object DjiVirtualStickAxes {
    data class Values(
        val pitch: Float,
        val roll: Float,
        val yaw: Float,
        val verticalThrottle: Float,
    )

    fun fromFru(
        forward: Float,
        right: Float,
        up: Float,
        yawRate: Float,
    ): Values = Values(
        pitch = right,
        roll = forward,
        yaw = yawRate,
        verticalThrottle = up,
    )
}
