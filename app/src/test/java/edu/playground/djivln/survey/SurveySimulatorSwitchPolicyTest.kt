package edu.playground.djivln.survey

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveySimulatorSwitchPolicyTest {
    @Test fun `running and arming are paused before simulator switch`() {
        assertEquals(
            SurveySimulatorSwitchAction.PAUSE_AND_PRESERVE_CHECKPOINT,
            SurveySimulatorSwitchPolicy.actionFor(SurveyExecutionState.ARMING),
        )
        assertEquals(
            SurveySimulatorSwitchAction.PAUSE_AND_PRESERVE_CHECKPOINT,
            SurveySimulatorSwitchPolicy.actionFor(SurveyExecutionState.RUNNING),
        )
    }

    @Test fun `paused and terminal states preserve their current state`() {
        listOf(
            SurveyExecutionState.IDLE,
            SurveyExecutionState.PAUSED,
            SurveyExecutionState.COMPLETED,
            SurveyExecutionState.ABORTED,
        ).forEach {
            assertEquals(SurveySimulatorSwitchAction.NONE, SurveySimulatorSwitchPolicy.actionFor(it))
        }
    }

    @Test fun `paused mission retains exclusive control reservation`() {
        assertTrue(SurveySimulatorSwitchPolicy.reservesControl(SurveyExecutionState.ARMING))
        assertTrue(SurveySimulatorSwitchPolicy.reservesControl(SurveyExecutionState.RUNNING))
        assertTrue(SurveySimulatorSwitchPolicy.reservesControl(SurveyExecutionState.PAUSED))
        assertFalse(SurveySimulatorSwitchPolicy.reservesControl(SurveyExecutionState.IDLE))
        assertFalse(SurveySimulatorSwitchPolicy.reservesControl(SurveyExecutionState.COMPLETED))
    }
}
