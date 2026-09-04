package edu.playground.djivln.mini2

/** Normalizes DJI MSDK4 SimulatorState into the HIL ENU/FRU convention. */
object DjiSimulatorCoordinates {
    data class End(
        val eastMeters: Double,
        val northMeters: Double,
        val downMeters: Double,
        val rollDegrees: Double,
        val pitchDegrees: Double,
    )

    fun fromSdk(
        positionX: Number,
        positionY: Number,
        positionZ: Number,
        roll: Number,
        pitch: Number,
    ): End = End(
        northMeters = positionX.toDouble(),
        eastMeters = positionY.toDouble(),
        downMeters = positionZ.toDouble(),
        rollDegrees = roll.toDouble(),
        pitchDegrees = -pitch.toDouble(),
    )
}
