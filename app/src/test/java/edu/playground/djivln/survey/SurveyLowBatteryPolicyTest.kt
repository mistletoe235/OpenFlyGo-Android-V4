package edu.playground.djivln.survey

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveyLowBatteryPolicyTest {
    @Test
    fun `real survey triggers below twenty percent`() {
        assertTrue(SurveyLowBatteryPolicy.shouldTrigger(
            19, aircraftFlying = true, simulatorActive = false,
            executionState = SurveyExecutionState.RUNNING,
        ))
        assertFalse(SurveyLowBatteryPolicy.shouldTrigger(
            20, aircraftFlying = true, simulatorActive = false,
            executionState = SurveyExecutionState.RUNNING,
        ))
    }

    @Test
    fun `grounded simulator and completed missions never trigger`() {
        assertFalse(SurveyLowBatteryPolicy.shouldTrigger(
            10, aircraftFlying = false, simulatorActive = false,
            executionState = SurveyExecutionState.RUNNING,
        ))
        assertFalse(SurveyLowBatteryPolicy.shouldTrigger(
            10, aircraftFlying = true, simulatorActive = true,
            executionState = SurveyExecutionState.RUNNING,
        ))
        assertFalse(SurveyLowBatteryPolicy.shouldTrigger(
            10, aircraftFlying = true, simulatorActive = false,
            executionState = SurveyExecutionState.COMPLETED,
        ))
    }
}
