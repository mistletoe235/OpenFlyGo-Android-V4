package edu.playground.djivln.survey

import org.junit.Assert.assertEquals
import org.junit.Test

class SurveyExecutionCheckpointTest {
    @Test
    fun `checkpoint round trips without changing recovery position`() {
        val original = SurveyExecutionCheckpoint(
            "mission-1", 7, SurveyExecutionState.RUNNING, 123_456L,
            executionLegIndex = 9,
            phase = SurveyExecutionPhase.TRANSIT_TO_START,
            recoveryPoint = GeoPoint(31.2304, 121.4737, 42.5),
        )

        assertEquals(original, SurveyExecutionCheckpointJson.decode(
            SurveyExecutionCheckpointJson.encode(original),
        ))
    }

    @Test
    fun `schema one checkpoint restores as survey leg`() {
        val legacy = """{"schema_version":1,"mission_id":"m","waypoint_index":3,"state":"PAUSED","updated_at_epoch_ms":9}"""
        val decoded = SurveyExecutionCheckpointJson.decode(legacy)

        assertEquals(Int.MAX_VALUE, decoded.executionLegIndex)
        assertEquals(SurveyExecutionPhase.SURVEY, decoded.phase)
    }

    @Test
    fun `paused strip resumes from strip start to avoid coverage gaps`() {
        val recovery = SurveyCheckpointRecoveryPolicy.position(
            waypointIndex = 7,
            executionLegIndex = 9,
            state = SurveyExecutionState.PAUSED,
            phase = SurveyExecutionPhase.SURVEY,
            targetCaptureAction = CaptureAction.STOP_DISTANCE_INTERVAL,
            hasRecoveryPoint = false,
        )

        assertEquals(SurveyRecoveryPosition(6, 8), recovery)
    }

    @Test
    fun `paused strip with exact recovery point retains current target`() {
        val recovery = SurveyCheckpointRecoveryPolicy.position(
            waypointIndex = 7,
            executionLegIndex = 9,
            state = SurveyExecutionState.PAUSED,
            phase = SurveyExecutionPhase.SURVEY,
            targetCaptureAction = CaptureAction.STOP_DISTANCE_INTERVAL,
            hasRecoveryPoint = true,
        )

        assertEquals(SurveyRecoveryPosition(7, 9), recovery)
    }

    @Test
    fun `transit and running checkpoints retain exact target`() {
        assertEquals(
            SurveyRecoveryPosition(7, 9),
            SurveyCheckpointRecoveryPolicy.position(
                7, 9, SurveyExecutionState.PAUSED,
                SurveyExecutionPhase.RETURN_HOME, CaptureAction.NONE, false,
            ),
        )
        assertEquals(
            SurveyRecoveryPosition(7, 9),
            SurveyCheckpointRecoveryPolicy.position(
                7, 9, SurveyExecutionState.RUNNING,
                SurveyExecutionPhase.SURVEY, CaptureAction.STOP_DISTANCE_INTERVAL, false,
            ),
        )
    }
}
