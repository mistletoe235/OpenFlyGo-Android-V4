package edu.playground.djivln.survey

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.hypot

class ChinaCoordinateTransformTest {
    @Test
    fun `Shanghai WGS84 round trip stays sub-meter`() {
        val original = GeoPoint(31.2304, 121.4737)
        val gcj = ChinaCoordinateTransform.wgs84ToGcj02(original)
        val restored = ChinaCoordinateTransform.gcj02ToWgs84(gcj)

        assertTrue(distanceMeters(original, gcj) > 100.0)
        assertTrue(distanceMeters(original, restored) < 0.2)
    }

    @Test
    fun `coordinates outside mainland China stay unchanged`() {
        val point = GeoPoint(37.7749, -122.4194, 50.0)
        assertEquals(point, ChinaCoordinateTransform.wgs84ToGcj02(point))
        assertEquals(point, ChinaCoordinateTransform.gcj02ToWgs84(point))
    }

    private fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val north = (a.latitude - b.latitude) * 111_132.0
        val east = (a.longitude - b.longitude) * 111_320.0 * cos(Math.toRadians(a.latitude))
        return hypot(north, east)
    }
}
