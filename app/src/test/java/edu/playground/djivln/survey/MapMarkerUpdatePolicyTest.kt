package edu.playground.djivln.survey

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapMarkerUpdatePolicyTest {
    @Test fun `small frequent changes do not redraw marker`() {
        val point = GeoPoint(31.0, 121.0)
        assertFalse(MapMarkerUpdatePolicy.shouldUpdate(
            point, point.copy(latitude = 31.000001), 10.0, 10.5, 1_000, 1_100))
    }

    @Test fun `movement or circular heading change redraws after interval`() {
        val point = GeoPoint(31.0, 121.0)
        assertTrue(MapMarkerUpdatePolicy.shouldUpdate(
            point, point.copy(latitude = 31.00001), 359.0, 1.0, 1_000, 1_200))
        assertTrue(MapMarkerUpdatePolicy.circularHeadingDelta(359.0, 1.0) == 2.0)
    }

    @Test fun `device location prefers RC GPS and falls back to phone`() {
        val rc = GeoPoint(31.0, 121.0)
        val phone = GeoPoint(31.1, 121.1)
        assertEquals(rc, MapMarkerUpdatePolicy.preferredDeviceLocation(rc, phone)?.point)
        assertEquals(phone, MapMarkerUpdatePolicy.preferredDeviceLocation(null, phone)?.point)
        assertNull(MapMarkerUpdatePolicy.preferredDeviceLocation(null, null))
    }

    @Test fun `rendered anchor is retained until cumulative movement reaches threshold`() {
        val origin = GeoPoint(31.0, 121.0)
        var rendered = origin
        listOf(0.2, 0.4, 0.6).forEach { northMeters ->
            val sample = origin.copy(latitude = origin.latitude + northMeters / 111_132.0)
            if (MapMarkerUpdatePolicy.shouldMove(rendered, sample)) rendered = sample
        }
        assertTrue(MapMarkerUpdatePolicy.distanceMeters(origin, rendered) >=
            MapMarkerUpdatePolicy.MINIMUM_MOVE_METERS)
    }
}
