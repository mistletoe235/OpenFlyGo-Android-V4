package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Test

class DjiSimulatorCoordinatesTest {
    @Test
    fun normalizesSdkNedPositionAndPitchToHilEnuFru() {
        val value = DjiSimulatorCoordinates.fromSdk(
            positionX = 12.5f,
            positionY = -3.25f,
            positionZ = -8.0f,
            roll = 6.0f,
            pitch = -9.0f,
        )

        assertEquals(-3.25, value.eastMeters, 1e-9)
        assertEquals(12.5, value.northMeters, 1e-9)
        assertEquals(-8.0, value.downMeters, 1e-9)
        assertEquals(8.0, -value.downMeters, 1e-9)
        assertEquals(6.0, value.rollDegrees, 1e-9)
        assertEquals(9.0, value.pitchDegrees, 1e-9)
    }
}
