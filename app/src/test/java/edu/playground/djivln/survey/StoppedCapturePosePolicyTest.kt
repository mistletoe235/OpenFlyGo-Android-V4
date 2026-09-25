package edu.playground.djivln.survey

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoppedCapturePosePolicyTest {
    @Test fun onlyPointPhotosRequireStoppedPose() {
        assertTrue(StoppedCapturePosePolicy.requiresStoppedPose(CaptureAction.CAPTURE_ON_REACH))
        assertFalse(StoppedCapturePosePolicy.requiresStoppedPose(CaptureAction.START_DISTANCE_INTERVAL))
        assertFalse(StoppedCapturePosePolicy.requiresStoppedPose(CaptureAction.STOP_DISTANCE_INTERVAL))
        assertFalse(StoppedCapturePosePolicy.requiresStoppedPose(CaptureAction.NONE))
    }

    @Test
    fun `stopped capture requires aligned fresh pose and stable dwell`() {
        val target = SurveyWaypoint(
            point = GeoPoint(31.0, 121.0, 40.0),
            headingDegrees = 0.0,
            gimbalPitchDegrees = -45.0,
            kind = SurveyWaypointKind.CAPTURE_POINT,
            captureAction = CaptureAction.CAPTURE_ON_REACH,
            passIndex = 0,
        )
        val position = target.point
        assertTrue(StoppedCapturePosePolicy.aligned(
            connected = true, headingDegrees = 0.0, gimbalPitchDegrees = -45.0,
            altitudeMeters = 40.0, position = position, target = target,
            horizontalSpeedMetersPerSecond = 0.0, nowEpochMillis = 2_000L,
            flightStateUpdatedAtMillis = 2_000L, gimbalStateUpdatedAtMillis = 2_000L,
        ))
        assertFalse(StoppedCapturePosePolicy.aligned(
            connected = true, headingDegrees = 8.0, gimbalPitchDegrees = -45.0,
            altitudeMeters = 40.0, position = position, target = target,
            horizontalSpeedMetersPerSecond = 0.0, nowEpochMillis = 2_000L,
            flightStateUpdatedAtMillis = 2_000L, gimbalStateUpdatedAtMillis = 2_000L,
        ))
        val since = StoppedCapturePosePolicy.updateStableSince(true, 0L, 1_000L)
        assertFalse(StoppedCapturePosePolicy.stable(since, 1_799L))
        assertTrue(StoppedCapturePosePolicy.stable(since, 1_800L))
    }
}
