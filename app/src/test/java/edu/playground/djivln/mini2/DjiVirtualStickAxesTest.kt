package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Test

class DjiVirtualStickAxesTest {
    @Test
    fun mapsBodyForwardAndRightThroughMsdk4SwappedFields() {
        val value = DjiVirtualStickAxes.fromFru(
            forward = 1.5f,
            right = -0.75f,
            up = 0.4f,
            yawRate = 12f,
        )

        assertEquals(-0.75f, value.pitch, 0f)
        assertEquals(1.5f, value.roll, 0f)
        assertEquals(12f, value.yaw, 0f)
        assertEquals(0.4f, value.verticalThrottle, 0f)
    }
}
