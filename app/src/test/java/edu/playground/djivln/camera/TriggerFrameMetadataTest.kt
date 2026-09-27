package edu.playground.djivln.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerFrameMetadataTest {
    @Test fun `sidecar preserves trigger-time pose and mission indices`() {
        val metadata = TriggerFrameMetadata(
            capturedAtEpochMillis = 123L, telemetryUpdatedAtEpochMillis = 120L,
            triggerReason = "CAPTURE_ON_REACH", captureView = "NADIR", product = "DJI Mini 2",
            latitude = 31.1, longitude = 121.2, altitudeMeters = 10.0, aslMeters = 15.0,
            groundClearanceMeters = 2.0, headingDegrees = 90.0, aircraftPitchDegrees = 3.0,
            gimbalPitchDegrees = -45.0, velocityNorthMetersPerSecond = 1.0,
            velocityEastMetersPerSecond = 2.0, velocityDownMetersPerSecond = -0.5,
            missionId = "mission", executionLegIndex = 4, waypointIndex = 5,
            aircraftRollDegrees = 1.0, aircraftYawDegrees = 89.5,
            gimbalRollDegrees = 0.5, gimbalYawDegrees = 12.0,
            gimbalYawRelativeToAircraftHeadingDegrees = 20.0,
            gimbalStateUpdatedAtEpochMillis = 122L,
        )
        val json = metadata.toJson("Download/test.jpg")
        assertEquals("openfly.trigger-frame.v1", json.getString("schema"))
        assertEquals(31.1, json.getDouble("latitude"), 0.0)
        assertEquals(4, json.getInt("execution_leg_index"))
        assertTrue(json.getString("image_path").endsWith("test.jpg"))
        assertEquals(110.0, json.getDouble("camera_yaw_deg_true"), 0.0)
        assertEquals("aircraft_heading_plus_gimbal_relative_absolute_conflict", json.getString("camera_yaw_source"))
        assertEquals(98.0, json.getDouble("camera_yaw_consistency_error_deg"), 0.0)
        assertEquals(1L, json.getLong("gimbal_age_ms"))
    }

    @Test fun `GPS and ASL are accepted only while aircraft telemetry is fresh`() {
        val fresh = TriggerFrameMetadata(
            2_000L, 1_000L, "TEST", "NADIR", "DJI", 31.1, 121.2,
            12.0, 30.0, 2.0, 90.0, 0.0, -90.0, 0.0, 0.0, 0.0,
            null, null, null,
        )
        assertTrue(fresh.hasFreshAircraftGps)
        assertEquals(30.0, fresh.trustedAslMeters!!, 0.0)

        val stale = fresh.copy(capturedAtEpochMillis = 4_001L)
        assertTrue(!stale.hasFreshAircraftGps)
        assertEquals(null, stale.trustedAslMeters)
        assertTrue(!fresh.copy(latitude = 0.0, longitude = 0.0).hasFreshAircraftGps)
        assertEquals(null, fresh.copy(aslMeters = 0.0, altitudeMeters = 12.0).trustedAslMeters)
    }
}
