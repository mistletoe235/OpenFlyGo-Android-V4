package edu.playground.djivln.survey

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveySimulatorExecutionTest {
    private val now = 1_000_000L

    private fun mission(): SurveyMission = SurveyPlanner.plan(
        name = "sim-only",
        roi = listOf(
            GeoPoint(31.23030, 121.47360),
            GeoPoint(31.23030, 121.47410),
            GeoPoint(31.23065, 121.47410),
            GeoPoint(31.23065, 121.47360),
        ),
        constraints = SurveyConstraints(altitudeMetersAgl = 40.0),
    )

    private fun telemetry(
        simulatorActive: Boolean = true,
        simulatorFlying: Boolean = true,
        virtualStickEnabled: Boolean = false,
        sticksActive: Boolean = false,
        updatedAt: Long = now,
    ) = SurveyExecutionTelemetry(
        connected = true,
        simulatorActive = simulatorActive,
        simulatorFlying = simulatorFlying,
        virtualStickEnabled = virtualStickEnabled,
        sticksActive = sticksActive,
        latitude = 31.23045,
        longitude = 121.47375,
        altitudeMeters = 10.0,
        updatedAtEpochMillis = updatedAt,
    )

    @Test
    fun `eta adds delay only for capture on reach`() {
        val base = mission()
        fun estimate(action: CaptureAction): Double {
            val waypoint = base.waypoints.first().copy(
                kind = if (action == CaptureAction.CAPTURE_ON_REACH) {
                    SurveyWaypointKind.CAPTURE_POINT
                } else SurveyWaypointKind.TRANSIT,
                captureAction = action,
                captureIntervalMeters = null,
            )
            val value = base.copy(waypoints = listOf(waypoint))
            return SurveySimulatorExecutionStateMachine(value)
                .remainingEstimate(waypoint.point).totalSeconds
        }
        assertEquals(SurveyEtaPolicy.CAPTURE_ON_REACH_SECONDS,
            estimate(CaptureAction.CAPTURE_ON_REACH), 0.0)
        assertEquals(0.0, estimate(CaptureAction.NONE), 0.0)
    }

    @Test
    fun `arming is impossible outside DJI simulator`() {
        val result = SurveySimulatorGate.evaluate(
            mission(), telemetry(simulatorActive = false), now, requireVirtualStick = false,
        )

        assertFalse(result.allowed)
        assertTrue(result.blocks.contains(SurveyExecutionBlock.SIMULATOR_REQUIRED))
    }

    @Test
    fun `automatic takeoff preflight may allow grounded simulator but normal arm may not`() {
        val grounded = telemetry(simulatorFlying = false)

        val automaticPreflight = SurveySimulatorGate.evaluate(
            mission(), grounded, now, requireVirtualStick = false, allowNotFlying = true,
        )
        val normalArm = SurveySimulatorGate.evaluate(
            mission(), grounded, now, requireVirtualStick = false,
        )

        assertTrue(automaticPreflight.allowed)
        assertFalse(normalArm.allowed)
        assertTrue(normalArm.blocks.contains(SurveyExecutionBlock.SIMULATOR_NOT_FLYING))
    }

    @Test
    fun `automatic takeoff preflight may defer grounded simulator GPS until after takeoff`() {
        val groundedWithoutGps = telemetry(simulatorFlying = false).copy(
            latitude = Double.NaN,
            longitude = Double.NaN,
        )

        val automaticPreflight = SurveySimulatorGate.evaluate(
            mission(), groundedWithoutGps, now, requireVirtualStick = false,
            allowNotFlying = true, allowGroundedPositionUnavailable = true,
        )
        val normalArm = SurveySimulatorGate.evaluate(
            mission(), groundedWithoutGps, now, requireVirtualStick = false,
        )

        assertTrue(automaticPreflight.allowed)
        assertTrue(normalArm.blocks.contains(SurveyExecutionBlock.GPS_UNAVAILABLE))
    }

    @Test
    fun `arming allows simulator before virtual stick and running requires it`() {
        val arm = SurveySimulatorGate.evaluate(mission(), telemetry(), now, requireVirtualStick = false)
        val run = SurveySimulatorGate.evaluate(mission(), telemetry(), now, requireVirtualStick = true)

        assertTrue(arm.allowed)
        assertFalse(run.allowed)
        assertEquals(setOf(SurveyExecutionBlock.VIRTUAL_STICK_REQUIRED), run.blocks)
    }

    @Test
    fun `unsafe camera rate blocks simulator execution`() {
        val base = mission()
        val unsafe = SurveyPlanner.plan(
            "too-fast-camera",
            base.roi,
            constraints = base.constraints.copy(
                altitudeMetersAgl = 10.0,
                speedMetersPerSecond = 5.0,
            ),
        )
        val result = SurveySimulatorGate.evaluate(unsafe, telemetry(), now, false)

        assertFalse(result.allowed)
        assertTrue(result.blocks.contains(SurveyExecutionBlock.CAMERA_TRIGGER_UNSAFE))
    }

    @Test
    fun `imported unsafe takeoff altitude is blocked`() {
        val base = mission()
        val unsafe = base.copy(
            constraints = base.constraints.copy(safeTakeoffAltitudeMeters = 130.0),
        )
        val result = SurveySimulatorGate.evaluate(unsafe, telemetry(), now, false)

        assertFalse(result.allowed)
        assertTrue(result.blocks.contains(SurveyExecutionBlock.MISSION_ALTITUDE_UNSAFE))
    }

    @Test
    fun `duplicate pause preserves original reason and recovery point`() {
        val mission = mission()
        val machine = SurveySimulatorExecutionStateMachine(mission)
        machine.requestArm(SurveySimulatorGate.evaluate(mission, telemetry(), now, false))
        machine.onVirtualStickReady(
            SurveySimulatorGate.evaluate(mission, telemetry(virtualStickEnabled = true), now, true),
        )
        val recovery = GeoPoint(31.23042, 121.47372, 40.0)
        val first = machine.pause("pose timeout", recovery)
        val second = machine.pause("background", null)
        assertEquals(first, second)
        assertEquals(SurveyExecutionState.PAUSED, second.state)
        assertEquals(recovery, machine.pausedRecoveryPoint())
    }

    @Test
    fun `manual takeover and stale telemetry pause a running mission`() {
        val mission = mission()
        val machine = SurveySimulatorExecutionStateMachine(mission)
        val armGate = SurveySimulatorGate.evaluate(mission, telemetry(), now, false)
        val runGate = SurveySimulatorGate.evaluate(
            mission, telemetry(virtualStickEnabled = true), now, true,
        )
        machine.requestArm(armGate)
        machine.onVirtualStickReady(runGate)
        assertEquals(SurveyExecutionState.RUNNING, machine.status.state)

        val failed = SurveySimulatorGate.evaluate(
            mission,
            telemetry(virtualStickEnabled = true, sticksActive = true, updatedAt = now - 5_000L),
            now,
            true,
        )
        val status = machine.validate(failed)
        assertEquals(SurveyExecutionState.PAUSED, status.state)
        assertTrue(status.reason!!.contains("MANUAL_TAKEOVER"))
        assertTrue(status.reason!!.contains("TELEMETRY_STALE"))
    }

    @Test
    fun `failed resume keeps checkpoint paused after simulator exits`() {
        val mission = mission()
        val machine = SurveySimulatorExecutionStateMachine(mission)
        machine.requestArm(SurveySimulatorGate.evaluate(mission, telemetry(), now, false))
        machine.onVirtualStickReady(
            SurveySimulatorGate.evaluate(mission, telemetry(virtualStickEnabled = true), now, true),
        )
        machine.pause()
        val result = machine.resume(
            SurveySimulatorGate.evaluate(
                mission,
                telemetry(simulatorActive = false, virtualStickEnabled = true),
                now,
                true,
            ),
        )

        assertEquals(SurveyExecutionState.PAUSED, result.state)
        assertTrue(result.reason!!.contains("SIMULATOR_REQUIRED"))
    }

    @Test
    fun `resume first returns to exact pause point without capture`() {
        val mission = mission()
        val machine = SurveySimulatorExecutionStateMachine(mission)
        machine.requestArm(SurveySimulatorGate.evaluate(mission, telemetry(), now, false))
        val runGate = SurveySimulatorGate.evaluate(
            mission, telemetry(virtualStickEnabled = true), now, true,
        )
        machine.onVirtualStickReady(runGate)
        val originalTarget = machine.currentTarget
        val pausedAt = GeoPoint(31.23042, 121.47372, 40.0)
        val resumedFrom = GeoPoint(31.23035, 121.47360, 40.0)

        machine.pause("manual", pausedAt)
        assertEquals(pausedAt, machine.currentTarget.point)
        assertEquals(pausedAt, machine.pausedRecoveryPoint())

        machine.resume(runGate, resumedFrom)

        assertEquals(SurveyExecutionPhase.RECOVERY_TO_PAUSE, machine.currentPhase)
        assertEquals(pausedAt, machine.currentTarget.point)
        assertEquals(CaptureAction.NONE, machine.currentTarget.captureAction)
        assertEquals(SurveyWaypointKind.TRANSIT, machine.currentTarget.kind)
        assertTrue(machine.currentTarget.headingDegrees in 0.0..360.0)

        machine.reachWaypoint()

        assertEquals(SurveyExecutionPhase.SURVEY, machine.currentPhase)
        assertEquals(originalTarget, machine.currentTarget)
        assertEquals(null, machine.pausedRecoveryPoint())
    }

    @Test
    fun `resume climbs before a horizontal recovery transit and descends at pause point`() {
        val mission = mission().copy(
            constraints = mission().constraints.copy(safeTakeoffAltitudeMeters = 30.0),
        )
        val machine = SurveySimulatorExecutionStateMachine(mission)
        machine.requestArm(SurveySimulatorGate.evaluate(mission, telemetry(), now, false))
        val runGate = SurveySimulatorGate.evaluate(
            mission, telemetry(virtualStickEnabled = true), now, true,
        )
        machine.onVirtualStickReady(runGate)
        val pausedAt = GeoPoint(31.23042, 121.47372, 8.0)
        val resumedFrom = GeoPoint(31.23035, 121.47360, 6.0)
        machine.pause("manual", pausedAt)

        machine.resume(runGate, resumedFrom, 123.0)

        assertEquals(SurveyExecutionPhase.SAFE_CLIMB, machine.currentPhase)
        assertEquals(resumedFrom.latitude, machine.currentTarget.point.latitude, 0.0)
        assertEquals(resumedFrom.longitude, machine.currentTarget.point.longitude, 0.0)
        assertEquals(30.0, machine.currentTarget.point.altitudeMeters, 0.0)
        assertEquals(123.0, machine.currentTarget.headingDegrees, 0.0)
        assertEquals(CaptureAction.NONE, machine.currentTarget.captureAction)

        machine.reachWaypoint()
        assertEquals(SurveyExecutionPhase.RECOVERY_TO_PAUSE, machine.currentPhase)
        assertEquals(30.0, machine.currentTarget.point.altitudeMeters, 0.0)

        machine.reachWaypoint()
        assertEquals(SurveyExecutionPhase.RECOVERY_TO_PAUSE, machine.currentPhase)
        assertEquals(pausedAt, machine.currentTarget.point)

        machine.reachWaypoint()
        assertEquals(SurveyExecutionPhase.SURVEY, machine.currentPhase)
        assertEquals(null, machine.pausedRecoveryPoint())
    }

    @Test
    fun `waypoint completion is explicit and bounded`() {
        val mission = mission()
        val machine = SurveySimulatorExecutionStateMachine(mission)
        machine.requestArm(SurveySimulatorGate.evaluate(mission, telemetry(), now, false))
        machine.onVirtualStickReady(
            SurveySimulatorGate.evaluate(mission, telemetry(virtualStickEnabled = true), now, true),
        )
        repeat(mission.waypoints.size) { machine.reachWaypoint() }

        assertEquals(SurveyExecutionState.COMPLETED, machine.status.state)
        assertEquals(mission.waypoints.lastIndex, machine.status.waypointIndex)
        machine.reachWaypoint()
        assertEquals(SurveyExecutionState.COMPLETED, machine.status.state)
    }

    @Test
    fun `restored checkpoint is paused and never resumes implicitly`() {
        val mission = mission()
        val machine = SurveySimulatorExecutionStateMachine(mission)

        val restored = machine.restorePaused(2)

        assertEquals(SurveyExecutionState.PAUSED, restored.state)
        assertEquals(2, restored.waypointIndex)
    }

    @Test
    fun `arming mission can be paused before virtual stick becomes ready`() {
        val mission = mission()
        val machine = SurveySimulatorExecutionStateMachine(mission)
        machine.requestArm(SurveySimulatorGate.evaluate(mission, telemetry(), now, false))

        assertEquals(SurveyExecutionState.PAUSED, machine.pause().state)
    }

    @Test
    fun `legacy checkpoint remaps mission waypoint after safe transit legs`() {
        val mission = mission()
        val machine = SurveySimulatorExecutionStateMachine(
            mission,
            GeoPoint(31.2302, 121.4735, 8.0),
        )

        val restored = machine.restorePaused(2, Int.MAX_VALUE)

        assertEquals(SurveyExecutionState.PAUSED, restored.state)
        assertEquals(SurveyExecutionPhase.SURVEY, machine.currentPhase)
        assertEquals(mission.waypoints[2], machine.currentTarget)
        assertEquals(4, machine.executionLegIndex)
    }

    @Test
    fun `execution plan climbs safely before transit and survey`() {
        val mission = mission().copy(
            constraints = mission().constraints.copy(safeTakeoffAltitudeMeters = 55.0),
        )
        val launch = GeoPoint(31.23020, 121.47350, 8.0)
        val machine = SurveySimulatorExecutionStateMachine(mission, launch)
        machine.requestArm(SurveySimulatorGate.evaluate(mission, telemetry(), now, false))
        machine.onVirtualStickReady(
            SurveySimulatorGate.evaluate(mission, telemetry(virtualStickEnabled = true), now, true),
        )

        assertEquals(SurveyExecutionPhase.SAFE_CLIMB, machine.currentPhase)
        assertEquals(launch.latitude, machine.currentTarget.point.latitude, 0.0)
        assertEquals(55.0, machine.currentTarget.point.altitudeMeters, 0.0)
        assertEquals(0.0, machine.currentTarget.gimbalPitchDegrees, 0.0)
        machine.reachWaypoint()
        assertEquals(SurveyExecutionPhase.TRANSIT_TO_START, machine.currentPhase)
        assertEquals(mission.waypoints.first().point.latitude, machine.currentTarget.point.latitude, 0.0)
        assertEquals(0.0, machine.currentTarget.gimbalPitchDegrees, 0.0)
        assertTrue(machine.currentTarget.headingDegrees in 0.0..360.0)
        machine.reachWaypoint()
        assertEquals(SurveyExecutionPhase.SURVEY, machine.currentPhase)
        assertEquals(mission.waypoints.first(), machine.currentTarget)
    }

    @Test
    fun `safe climb uses current aircraft position while completion uses home`() {
        val mission = mission().copy(
            constraints = mission().constraints.copy(
                safeTakeoffAltitudeMeters = 55.0,
                completionAction = SurveyCompletionAction.RETURN_TO_HOME,
            ),
        )
        val current = GeoPoint(31.23100, 121.47500, 40.0)
        val home = GeoPoint(31.23020, 121.47350, 1.2)
        val machine = SurveySimulatorExecutionStateMachine(mission, current, home)

        assertEquals(current.latitude, machine.currentTarget.point.latitude, 0.0)
        assertEquals(current.longitude, machine.currentTarget.point.longitude, 0.0)
        assertEquals(55.0, machine.currentTarget.point.altitudeMeters, 0.0)

        machine.requestArm(SurveySimulatorGate.evaluate(mission, telemetry(), now, false))
        machine.onVirtualStickReady(
            SurveySimulatorGate.evaluate(mission, telemetry(virtualStickEnabled = true), now, true),
        )
        repeat(mission.waypoints.size + 3) { machine.reachWaypoint() }
        assertEquals(SurveyExecutionPhase.RETURN_HOME, machine.currentPhase)
        assertEquals(home.latitude, machine.currentTarget.point.latitude, 0.0)
        assertEquals(home.longitude, machine.currentTarget.point.longitude, 0.0)
    }

    @Test
    fun `route tracking repairs fixed legacy oblique headings at execution time`() {
        val planned = SurveyPlanner.plan(
            name = "legacy-fixed",
            roi = listOf(
                GeoPoint(31.23030, 121.47360),
                GeoPoint(31.23030, 121.47410),
                GeoPoint(31.23065, 121.47410),
                GeoPoint(31.23065, 121.47360),
            ),
            constraints = SurveyConstraints(
                altitudeMetersAgl = 40.0,
                collectionMode = SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION,
                enabledCaptureViews = setOf(SurveyCaptureView.FORWARD_OBLIQUE),
                obliqueHeadingMode = SurveyObliqueHeadingMode.TRACK_ROUTE,
            ),
        )
        val legacy = planned.copy(
            waypoints = planned.waypoints.map { it.copy(headingDegrees = 60.0) },
        )

        val surveyLegs = SurveySimulatorExecutionStateMachine.buildExecutionLegs(
            legacy,
            GeoPoint(31.23020, 121.47350, 40.0),
            GeoPoint(31.23020, 121.47350, 1.2),
        ).filter { it.phase == SurveyExecutionPhase.SURVEY && it.missionWaypointIndex != null }
        surveyLegs.chunked(2).forEach { pair ->
            val start = pair.first().target.point
            val end = pair.last().target.point
            val latitude1 = Math.toRadians(start.latitude)
            val latitude2 = Math.toRadians(end.latitude)
            val longitudeDelta = Math.toRadians(end.longitude - start.longitude)
            val expected = (Math.toDegrees(kotlin.math.atan2(
                kotlin.math.sin(longitudeDelta) * kotlin.math.cos(latitude2),
                kotlin.math.cos(latitude1) * kotlin.math.sin(latitude2) -
                    kotlin.math.sin(latitude1) * kotlin.math.cos(latitude2) *
                    kotlin.math.cos(longitudeDelta),
            )) + 360.0) % 360.0
            assertEquals(pair.first().target.headingDegrees, pair.last().target.headingDegrees, 0.0)
            val error = kotlin.math.abs(
                ((pair.first().target.headingDegrees - expected + 540.0) % 360.0) - 180.0,
            )
            assertTrue(error < 0.1)
        }
    }

    @Test
    fun `return to route start adds safe completion legs`() {
        val base = mission()
        val returnMission = base.copy(
            constraints = base.constraints.copy(
                safeTakeoffAltitudeMeters = 60.0,
                completionAction = SurveyCompletionAction.RETURN_TO_ROUTE_START,
            ),
        )
        val machine = SurveySimulatorExecutionStateMachine(
            returnMission,
            GeoPoint(31.2302, 121.4735, 10.0),
        )
        val expectedLegCount = returnMission.waypoints.size + 5
        assertEquals(expectedLegCount, machine.executionLegCount)
        machine.requestArm(SurveySimulatorGate.evaluate(returnMission, telemetry(), now, false))
        machine.onVirtualStickReady(
            SurveySimulatorGate.evaluate(returnMission, telemetry(virtualStickEnabled = true), now, true),
        )
        repeat(returnMission.waypoints.size + 2) { machine.reachWaypoint() }
        assertEquals(SurveyExecutionPhase.RETURN_TO_START, machine.currentPhase)
        assertEquals(60.0, machine.currentTarget.point.altitudeMeters, 0.0)
    }

    @Test
    fun `simulator return home is represented by controlled completion legs`() {
        val base = mission()
        val launch = GeoPoint(31.2302, 121.4735, 1.2)
        val machine = SurveySimulatorExecutionStateMachine(base, launch)

        assertEquals(base.waypoints.size + 5, machine.executionLegCount)
        machine.requestArm(SurveySimulatorGate.evaluate(base, telemetry(), now, false))
        machine.onVirtualStickReady(
            SurveySimulatorGate.evaluate(base, telemetry(virtualStickEnabled = true), now, true),
        )
        repeat(base.waypoints.size + 3) { machine.reachWaypoint() }
        assertEquals(SurveyExecutionPhase.RETURN_HOME, machine.currentPhase)
        assertEquals(launch.latitude, machine.currentTarget.point.latitude, 0.0)
        assertEquals(launch.longitude, machine.currentTarget.point.longitude, 0.0)
        assertTrue(machine.currentTarget.point.altitudeMeters > launch.altitudeMeters)
        assertEquals(0.0, machine.currentTarget.gimbalPitchDegrees, 0.0)
        machine.reachWaypoint()
        assertEquals(launch, machine.currentTarget.point)
        assertEquals(-90.0, machine.currentTarget.gimbalPitchDegrees, 0.0)
    }

    @Test
    fun `remaining estimate includes turns and decreases as legs complete`() {
        val base = mission()
        val launch = GeoPoint(31.2302, 121.4735, 1.2)
        val machine = SurveySimulatorExecutionStateMachine(base, launch)

        val initial = machine.remainingEstimate(launch)
        assertTrue(initial.totalSeconds > base.estimatedPathMeters / base.constraints.speedMetersPerSecond)
        assertTrue(initial.currentSectionSeconds < initial.totalSeconds)

        machine.requestArm(SurveySimulatorGate.evaluate(base, telemetry(), now, false))
        machine.onVirtualStickReady(
            SurveySimulatorGate.evaluate(base, telemetry(virtualStickEnabled = true), now, true),
        )
        val reached = machine.currentTarget.point
        machine.reachWaypoint()
        val afterFirstLeg = machine.remainingEstimate(reached)

        assertTrue(afterFirstLeg.totalSeconds < initial.totalSeconds)
    }

    @Test
    fun `remaining estimate uses live speed for current section`() {
        val base = mission()
        val launch = GeoPoint(31.2302, 121.4735, 1.2)
        val machine = SurveySimulatorExecutionStateMachine(base, launch)

        val slow = machine.remainingEstimate(
            launch,
            currentHeadingDegrees = 0.0,
            currentHorizontalSpeedMetersPerSecond = 0.4,
            currentVerticalSpeedMetersPerSecond = 0.2,
        )
        val fast = machine.remainingEstimate(
            launch,
            currentHeadingDegrees = 0.0,
            currentHorizontalSpeedMetersPerSecond = base.constraints.speedMetersPerSecond,
            currentVerticalSpeedMetersPerSecond = base.constraints.takeoffSpeedMetersPerSecond,
        )

        assertTrue(slow.currentSectionSeconds > fast.currentSectionSeconds)
        assertTrue(slow.totalSeconds > fast.totalSeconds)
    }
}
