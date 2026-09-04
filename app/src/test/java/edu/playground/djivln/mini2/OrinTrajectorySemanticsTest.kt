package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Test

class OrinTrajectorySemanticsTest {
    @Test
    fun `right displacement becomes clockwise target yaw`() {
        assertEquals(5.7106, OrinTrajectorySemantics.yawDeltaDegrees(2.0, 0.2), 1e-3)
        assertEquals(-5.7106, OrinTrajectorySemantics.yawDeltaDegrees(2.0, -0.2), 1e-3)
    }

    @Test
    fun `yaw command matches Orin limits and slew rate`() {
        val first = OrinTrajectorySemantics.yawRateDegreesPerSecond(0.0, 90.0, 0.0, 0.1)
        val second = OrinTrajectorySemantics.yawRateDegreesPerSecond(0.0, 90.0, first, 0.1)

        assertEquals(4.5, first, 1e-6)
        assertEquals(9.0, second, 1e-6)
    }

    @Test
    fun `fly through radius and timeout match Orin`() {
        assertEquals(0.125, OrinTrajectorySemantics.flyThroughRadiusMeters(0.5), 1e-6)
        assertEquals(0.75, OrinTrajectorySemantics.flyThroughRadiusMeters(3.0), 1e-6)
        assertEquals(10_000L, OrinTrajectorySemantics.flyThroughTimeoutMillis(0.5))
        assertEquals(20_000L, OrinTrajectorySemantics.flyThroughTimeoutMillis(3.0))
    }

    @Test
    fun `final position timeout gives short actions five seconds to settle`() {
        assertEquals(5_000L, OrinTrajectorySemantics.finalPositionTimeoutMillis(0.5, 0.0))
        assertEquals(6_285L, OrinTrajectorySemantics.finalPositionTimeoutMillis(3.0, 0.0))
        assertEquals(20_000L, OrinTrajectorySemantics.finalPositionTimeoutMillis(20.0, 0.0))
    }
}
