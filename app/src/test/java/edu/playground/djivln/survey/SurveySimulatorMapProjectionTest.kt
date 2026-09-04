package edu.playground.djivln.survey

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SurveySimulatorMapProjectionTest {
    @Test fun `fresh RAW projects position altitude and yaw without connection state`() {
        val pose = SurveySimulatorMapProjection.project(
            simulatorActive = true,
            sampleElapsedRealtimeNanos = 1_000,
            nowElapsedRealtimeNanos = 1_500,
            maximumAgeNanos = 1_000,
            originLatitude = 31.2,
            originLongitude = 121.4,
            eastMeters = 95.3,
            northMeters = 111.132,
            downMeters = -18.0,
            yawDegrees = -10.0,
        )

        assertNotNull(pose)
        assertEquals(31.201, pose!!.point.latitude, 1e-6)
        assertEquals(121.401, pose.point.longitude, 2e-5)
        assertEquals(18.0, pose.point.altitudeMeters, 1e-9)
        assertEquals(350.0, pose.headingDegrees, 1e-9)
    }

    @Test fun `inactive stale future and invalid-origin RAW fail closed`() {
        fun project(active: Boolean = true, sample: Long = 1_000, now: Long = 1_500,
                    latitude: Double = 31.2, longitude: Double = 121.4) =
            SurveySimulatorMapProjection.project(
                active, sample, now, 1_000, latitude, longitude,
                0.0, 0.0, 0.0, 0.0,
            )

        assertNull(project(active = false))
        assertNull(project(now = 2_001))
        assertNull(project(sample = 2_000, now = 1_500))
        assertNull(project(latitude = 0.0, longitude = 0.0))
    }
}
