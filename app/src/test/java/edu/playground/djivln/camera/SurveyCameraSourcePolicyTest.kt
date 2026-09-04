package edu.playground.djivln.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveyCameraSourcePolicyTest {
    @Test fun `selection prefers a connected non thermal camera`() {
        val cameras = listOf(
            SurveyCameraSourcePolicy.Candidate(connected = true, thermal = true),
            SurveyCameraSourcePolicy.Candidate(connected = true, thermal = false),
        )
        assertEquals(1, SurveyCameraSourcePolicy.selectedIndex(cameras))
    }

    @Test fun `primary feed association requires one verified non thermal camera`() {
        val single = listOf(SurveyCameraSourcePolicy.Candidate(true, false))
        assertTrue(SurveyCameraSourcePolicy.canAssociatePrimaryFeed(single, 0, true, "DJI MINI 2"))
        assertFalse(SurveyCameraSourcePolicy.canAssociatePrimaryFeed(single, 0, false, "DJI MINI 2"))
        assertFalse(SurveyCameraSourcePolicy.canAssociatePrimaryFeed(
            single + SurveyCameraSourcePolicy.Candidate(true, true), 0, true, "DJI MINI 2"))
    }

    @Test fun `M30 primary feed is ambiguous even with a wide profile`() {
        val single = listOf(SurveyCameraSourcePolicy.Candidate(true, false))
        assertFalse(SurveyCameraSourcePolicy.canAssociatePrimaryFeed(single, 0, true, "M30T"))
        assertFalse(SurveyCameraSourcePolicy.canAssociatePrimaryFeed(single, 0, true, "MATRICE_30_SERIES"))
        assertFalse(SurveyCameraSourcePolicy.canAssociatePrimaryFeed(single, 0, true, "MAVIC_3_ENTERPRISE"))
    }
}
