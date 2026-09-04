package edu.playground.djivln.survey

enum class SurveySimulatorSwitchAction {
    NONE,
    PAUSE_AND_PRESERVE_CHECKPOINT,
}

object SurveySimulatorSwitchPolicy {
    fun actionFor(state: SurveyExecutionState?): SurveySimulatorSwitchAction = when (state) {
        SurveyExecutionState.ARMING,
        SurveyExecutionState.RUNNING -> SurveySimulatorSwitchAction.PAUSE_AND_PRESERVE_CHECKPOINT
        else -> SurveySimulatorSwitchAction.NONE
    }

    /** A paused route still owns its resumable checkpoint and excludes other VS controllers. */
    fun reservesControl(state: SurveyExecutionState?): Boolean = when (state) {
        SurveyExecutionState.ARMING,
        SurveyExecutionState.RUNNING,
        SurveyExecutionState.PAUSED -> true
        else -> false
    }
}
