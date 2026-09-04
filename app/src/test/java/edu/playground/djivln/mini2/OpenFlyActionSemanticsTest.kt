package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenFlyActionSemanticsTest {
    @Test
    fun mapsModelAxesToBodyFruLikeIos() {
        val action = OpenFlyActionSemantics.map(1.2, -0.4, 0.1, 0.69)
        assertEquals(1.2, action.forwardMeters, 0.0)
        assertEquals(0.4, action.rightMeters, 0.0)
        assertEquals(0.1, action.upMeters, 0.0)
        assertFalse(action.shouldStop(0.7))
    }

    @Test
    fun stopsAtIosThreshold() {
        assertTrue(OpenFlyActionSemantics.map(0.0, 0.0, 0.0, 0.7).shouldStop(0.7))
        assertTrue(OpenFlyActionSemantics.map(0.0, 0.0, 0.0, 0.9).shouldStop(0.7))
    }

    @Test
    fun keepsUavFlowRightAxisAndExplicitYaw() {
        val action = OpenFlyActionSemantics.mapUavFlow(1.2, -0.4, 0.1, 12.5, 0.2)
        assertEquals(-0.4, action.rightMeters, 0.0)
        assertEquals(12.5, action.yawDegrees, 0.0)
    }
}
