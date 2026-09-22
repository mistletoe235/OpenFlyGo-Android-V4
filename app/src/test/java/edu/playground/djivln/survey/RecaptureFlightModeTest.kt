package edu.playground.djivln.survey

import edu.playground.djivln.reconstruction.V86SessionConfig
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class RecaptureFlightModeTest {
    private fun mission(spacing: Double = 8.0): SurveyMission {
        val base = SurveyRegressionMissionFactory.create(GeoPoint(31.0, 121.0), false)
        val template = base.waypoints.first()
        return base.copy(
            waypoints = (0..5).map { index -> template.copy(
                point = GeoPoint(31.0 + index * spacing / 111_132.0, 121.0, 30.0),
                kind = SurveyWaypointKind.CAPTURE_POINT, captureAction = CaptureAction.CAPTURE_ON_REACH,
                captureIntervalMeters = null, headingDegrees = 0.0, gimbalPitchDegrees = -90.0,
                passIndex = index, captureView = SurveyCaptureView.NADIR,
            ) },
            activeMapping = ActiveMappingMetadata(
                selectionMethod = "test", groundTruthUsed = false, gsUsedForSelection = false,
                ordinaryGpsUsed = true, sourceCaptureCount = 6, surveyCaptureCount = 6,
                bridgeCaptureCount = 0, sourceEstimatedRouteDistanceMeters = spacing * 5,
                passes = (0..5).map { index -> ActiveMappingPassMetadata(
                    index, "region", "EXACT_CAPTURE_POINT", "SURVEY", "test", false,
                ) },
            ),
            recaptureFlightMode = RecaptureFlightMode.CONTINUOUS_EXPERIMENTAL,
        )
    }

    @Test fun continuousSchema14RoundTripsWithoutDowngrade() {
        val source = mission()
        val raw = SurveyMissionJson.encode(source)
        assertEquals(14, JSONObject(raw).getInt("schema_version"))
        val decoded = SurveyMissionJson.decode(raw)
        assertEquals(source.recaptureFlightMode, decoded.recaptureFlightMode)
        assertEquals(source.waypoints, decoded.waypoints)
        assertEquals(source.activeMapping, decoded.activeMapping)
        assertEquals(14, JSONObject(SurveyMissionJson.encode(decoded)).getInt("schema_version"))
    }

    @Test fun legacyMissionsKeepStopCaptureAndSchema13() {
        val stopped = mission().copy(recaptureFlightMode = RecaptureFlightMode.STOP_AND_CAPTURE)
        val raw = SurveyMissionJson.encode(stopped)
        assertEquals(13, JSONObject(raw).getInt("schema_version"))
        assertFalse(JSONObject(raw).has("recapture_flight_mode"))
        assertEquals(RecaptureFlightMode.STOP_AND_CAPTURE, SurveyMissionJson.decode(raw).recaptureFlightMode)
        assertFalse(ContinuousRecapturePolicy.eligible(stopped, 2))
    }

    @Test fun unknownMissingMisversionedAndNonRecaptureModesAreRejected() {
        val raw = SurveyMissionJson.encode(mission())
        val invalid = listOf(
            JSONObject(raw).put("recapture_flight_mode", "UNKNOWN"),
            JSONObject(raw).also { it.remove("recapture_flight_mode") },
            JSONObject(raw).put("schema_version", 13),
            JSONObject(raw).put("schema_version", 15),
            JSONObject(raw).put("active_mapping", JSONObject.NULL),
        )
        for (root in invalid) assertTrue(runCatching { SurveyMissionJson.decode(root.toString()) }.isFailure)
    }

    @Test fun executionReviewRemainsRequiredForSchema14() {
        val raw = JSONObject(SurveyMissionJson.encode(mission()))
            .put("execution_review", JSONObject().put("safe_to_execute", false))
        assertThrows(IllegalArgumentException::class.java) { SurveyMissionJson.decode(raw.toString()) }
        raw.getJSONObject("execution_review").put("safe_to_execute", true)
        assertEquals(RecaptureFlightMode.CONTINUOUS_EXPERIMENTAL, SurveyMissionJson.decode(raw.toString()).recaptureFlightMode)
    }

    @Test fun uploadAdvertises14ButDefaultsToExplicitStopCapture() {
        val config = V86SessionConfig("test", 70.0, 10.0, "camera")
        assertEquals("[13,14]", config.toJson().getJSONArray("supported_mission_schemas").toString())
        assertEquals("STOP_AND_CAPTURE", config.toJson().getString("recapture_flight_mode"))
        assertEquals("CONTINUOUS_EXPERIMENTAL", config.copy(
            recaptureFlightMode = RecaptureFlightMode.CONTINUOUS_EXPERIMENTAL,
        ).toJson().getString("recapture_flight_mode"))
    }

    @Test fun onlyInteriorSameRegionAlignedCapturePointsPassThrough() {
        val source = mission()
        assertFalse(ContinuousRecapturePolicy.eligible(source, 0))
        assertFalse(ContinuousRecapturePolicy.eligible(source, source.waypoints.lastIndex))
        assertTrue(ContinuousRecapturePolicy.eligible(source, 2))
        assertFalse(ContinuousRecapturePolicy.eligible(mission(2.5), 2))
        val next = source.waypoints[3]
        val changes = listOf(next.copy(passIndex = 99), next.copy(headingDegrees = 20.0),
            next.copy(gimbalPitchDegrees = -45.0), next.copy(point = next.point.copy(altitudeMeters = 31.0)),
            next.copy(kind = SurveyWaypointKind.TRANSIT, captureAction = CaptureAction.NONE),
            next.copy(captureView = SurveyCaptureView.LOCAL_OBLIQUE),
            next.copy(point = GeoPoint(source.waypoints[2].point.latitude, 121.001, 30.0)))
        for (changed in changes) {
            val altered = source.copy(waypoints = source.waypoints.mapIndexed { index, waypoint ->
                if (index == 3) changed else waypoint
            })
            assertFalse(ContinuousRecapturePolicy.eligible(altered, 2))
        }
        val changedRegion = source.copy(activeMapping = source.activeMapping!!.copy(
            passes = source.activeMapping.passes.map { if (it.passIndex == 3) it.copy(regionId = "other") else it },
        ))
        assertFalse(ContinuousRecapturePolicy.eligible(changedRegion, 2))
    }

    @Test fun freshPoseIsRequiredWithoutDemandingZeroSpeed() {
        val target = mission().waypoints[2]
        val pose = SurveyFollowerPose(target.point.latitude, target.point.longitude, 30.0, 0.0)
        assertTrue(ContinuousRecapturePolicy.poseReady(true, pose, target, -90.0, 2_000L, 1_900L, 1_900L))
        assertFalse(ContinuousRecapturePolicy.poseReady(true, pose, target, -90.0, 2_000L, 900L, 1_900L))
        assertFalse(ContinuousRecapturePolicy.poseReady(true, pose, target, -90.0, 2_000L, 1_900L, 2_001L))
        assertFalse(ContinuousRecapturePolicy.poseReady(false, pose, target, -90.0, 2_000L, 1_900L, 1_900L))
        assertFalse(ContinuousRecapturePolicy.poseReady(true, pose.copy(headingDegrees = 5.0), target, -90.0, 2_000L, 1_900L, 1_900L))
        assertFalse(ContinuousRecapturePolicy.poseReady(true, pose, target, -45.0, 2_000L, 1_900L, 1_900L))
    }

    @Test fun captureWindowRetainsNonzeroVelocityButBoundsMotionWhileWaitingForAck() {
        val source = mission()
        val target = source.waypoints[2].point
        val pose = SurveyFollowerPose(target.latitude, target.longitude, target.altitudeMeters, 0.0)
        val moving = ContinuousRecapturePolicy.command(source, 2, pose, 2.0, 0.5)
        assertTrue(moving.reached)
        assertTrue(moving.forwardMetersPerSecond > 0.5)
        val limit = pose.copy(latitude = target.latitude + 4.0 / 111_132.0)
        val waiting = ContinuousRecapturePolicy.command(source, 2, limit, 2.0, 0.5)
        assertEquals(0.0, waiting.forwardMetersPerSecond, 1e-6)
        assertTrue(ContinuousRecapturePolicy.missedWindow(source, 2, limit))
    }

    @Test fun deterministicGuidanceSimulationVariesSpacingSpeedAndCameraLatency() {
        for (spacing in listOf(3.0, 5.0, 8.0, 15.0)) {
            for (speed in listOf(0.3, 1.0, 2.0, 4.0)) {
                for (latency in listOf(0.1, 0.5, 1.0, 3.0)) {
                    val source = mission(spacing)
                    val target = source.waypoints[2].point
                    var north = spacing
                    var requestAt = -1.0
                    var confirmed = false
                    for (step in 0..3_000) {
                        val time = step * 0.05
                        val pose = SurveyFollowerPose(31.0 + north / 111_132.0, 121.0, 30.0, 0.0)
                        val command = ContinuousRecapturePolicy.command(source, 2, pose, speed, 0.5)
                        assertTrue(hypot(command.forwardMetersPerSecond, command.rightMetersPerSecond) <= speed + 1e-6)
                        if (requestAt < 0.0) {
                            assertFalse(ContinuousRecapturePolicy.missedWindow(source, 2, pose))
                            if (command.reached) {
                                requestAt = time
                                assertTrue(command.forwardMetersPerSecond > 0.0)
                            }
                        } else if (time - requestAt >= latency) {
                            confirmed = true
                            break
                        }
                        north += command.forwardMetersPerSecond * 0.05
                        assertTrue(north <= spacing * 2.5 + 0.2)
                    }
                    assertTrue("spacing=$spacing speed=$speed latency=$latency", confirmed)
                    assertTrue(target.latitude.isFinite())
                }
            }
        }
    }
}
