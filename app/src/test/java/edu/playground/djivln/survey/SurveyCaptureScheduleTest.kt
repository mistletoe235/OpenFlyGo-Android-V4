package edu.playground.djivln.survey

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveyCaptureScheduleTest {
    @Test
    fun `point capture produces one exact event`() {
        val point = GeoPoint(31.2304, 121.4737, 74.5)
        val waypoint = SurveyWaypoint(
            point = point,
            headingDegrees = 123.0,
            gimbalPitchDegrees = -44.0,
            kind = SurveyWaypointKind.CAPTURE_POINT,
            captureAction = CaptureAction.CAPTURE_ON_REACH,
            passIndex = 0,
            captureView = SurveyCaptureView.LOCAL_OBLIQUE,
        )
        val mission = SurveyMission(
            name = "point",
            cameraProfile = CameraProfile.DJI_MINI_2,
            constraints = SurveyConstraints(),
            roi = listOf(
                point,
                point.copy(latitude = point.latitude + 0.0001),
                point.copy(longitude = point.longitude + 0.0001),
            ),
            waypoints = listOf(waypoint),
            estimatedPathMeters = 0.0,
            estimatedPhotoCount = 1,
            estimatedFlightSeconds = 0.0,
        )

        val event = SurveyCaptureSchedule.build(mission).single()

        assertEquals(point, event.point)
        assertEquals(123.0, event.headingDegrees, 0.0)
        assertEquals(-44.0, event.gimbalPitchDegrees, 0.0)
        assertEquals(SurveyCaptureView.LOCAL_OBLIQUE, event.captureView)
        assertTrue(SurveyPlanner.groundCoverage(mission).areaSquareMeters > 0.0)
    }

    @Test
    fun `five direction schedule follows complete route groups rather than same hover bursts`() {
        val mission = SurveyRegressionMissionFactory.create(GeoPoint(31.2304, 121.4737), true)
        val events = SurveyCaptureSchedule.build(mission)

        assertEquals(mission.estimatedPhotoCount, events.size)
        val runs = events.fold(mutableListOf<SurveyCaptureView>()) { values, event ->
            if (values.lastOrNull() != event.captureView) values += event.captureView
            values
        }
        assertEquals(
            listOf(
                SurveyCaptureView.NADIR,
                SurveyCaptureView.FORWARD_OBLIQUE,
                SurveyCaptureView.BACKWARD_OBLIQUE,
                SurveyCaptureView.LEFT_OBLIQUE,
                SurveyCaptureView.RIGHT_OBLIQUE,
            ),
            runs,
        )
        assertTrue(events.zipWithNext().all { (a, b) ->
            b.estimatedMissionDistanceMeters + 1.0e-9 >= a.estimatedMissionDistanceMeters
        })
        assertTrue(events.groupBy { it.captureView }.values.all { group ->
            group.map { it.passIndex }.distinct().size >= 2
        })
    }
}
