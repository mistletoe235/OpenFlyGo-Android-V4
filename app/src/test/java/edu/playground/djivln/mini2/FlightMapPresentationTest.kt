package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightMapPresentationTest {
    @Test
    fun `rc course is unavailable while stationary and north east while moving`() {
        assertNull(FlightMapPresentation.courseDegrees(0.1, 0.1))
        assertEquals(45.0, FlightMapPresentation.courseDegrees(1.0, 1.0)!!, 0.001)
    }

    @Test
    fun `home bearing label follows all eight compass sectors`() {
        assertEquals("↑", FlightMapPresentation.directionArrow(0.0))
        assertEquals("→", FlightMapPresentation.directionArrow(90.0))
        assertEquals("↙", FlightMapPresentation.directionArrow(225.0))
        assertEquals("↑", FlightMapPresentation.directionArrow(359.0))
    }

    @Test
    fun `home distance and marker rotation are normalized`() {
        assertTrue(FlightMapPresentation.distanceMeters(31.2304, 121.4737, 31.2302, 121.4735) > 20.0)
        assertEquals(232.0f, FlightMapPresentation.mapMarkerRotationDegrees(128.0), 0.001f)
    }
}
