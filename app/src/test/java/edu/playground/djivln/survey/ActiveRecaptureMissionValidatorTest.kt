package edu.playground.djivln.survey

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class ActiveRecaptureMissionValidatorTest {
    @Test
    fun `obsolete two buildings mission is blocked but its capture contract remains testable`() {
        val root = File(System.getProperty("user.dir"))
        val candidates = listOf(
            File(root, "testdata/active-recapture/two-buildings/openfly-active-recapture-two-buildings-v1.json"),
            File(root.parentFile, "testdata/active-recapture/two-buildings/openfly-active-recapture-two-buildings-v1.json"),
        )
        val file = candidates.firstOrNull(File::isFile)
            ?: error("compiled active recapture fixture not found from ${root.absolutePath}")
        val obsoleteMission = SurveyMissionJson.decode(file.readText())
        assertThrows(IllegalArgumentException::class.java) {
            ActiveRecaptureMissionValidator.validate(obsoleteMission)
        }
        val mission = obsoleteMission.copy(
            activeMapping = obsoleteMission.activeMapping!!.copy(
                selectionMethod = "scene-agnostic GPS + fast SfM + Scal3R test fixture",
            ),
        )

        val report = ActiveRecaptureMissionValidator.validate(mission)

        assertEquals(339, report.captureCount)
        assertEquals(33, report.pointCaptureCount)
        assertEquals(27, report.continuousPassCount)
        assertTrue(report.minimumAltitudeMeters >= 5.0)
        assertTrue(report.maximumAltitudeMeters <= 120.0)
        assertTrue(report.maximumAdjacentDistanceMeters <= 25.0 + 1.0e-6)
        assertTrue(report.maximumYawStepDegrees <= 45.0 + 1.0e-6)
        assertTrue(report.maximumGimbalPitchStepDegrees <= 15.0 + 1.0e-6)
        assertEquals(339, SurveyCaptureSchedule.build(mission).size)
        val statistics = SurveyPlanner.statistics(mission)
        assertEquals(339, statistics.photoCountByView.values.sum())
        assertEquals(339, statistics.sorties.sumOf { it.estimatedPhotoCount })

        val replay = SurveyMissionReplay(mission, sampleSpacingMeters = 25.0)
        var replaySnapshot = replay.start()
        var replaySteps = 0
        while (replaySnapshot.state == SurveyReplayState.RUNNING && replaySteps < 10_000) {
            replaySnapshot = replay.advance()
            replaySteps++
        }
        assertEquals(SurveyReplayState.COMPLETED, replaySnapshot.state)
        assertTrue(replaySteps < 10_000)

        val now = 1_000_000L
        val start = mission.waypoints.first().point
        val gate = SurveySimulatorGate.evaluate(
            mission = mission,
            telemetry = SurveyExecutionTelemetry(
                connected = true,
                simulatorActive = true,
                simulatorFlying = true,
                virtualStickEnabled = true,
                sticksActive = false,
                latitude = start.latitude,
                longitude = start.longitude,
                altitudeMeters = start.altitudeMeters,
                updatedAtEpochMillis = now,
            ),
            nowEpochMillis = now,
            requireVirtualStick = true,
        )
        assertTrue("simulator gate blocks=${gate.blocks}", gate.allowed)
    }
}
