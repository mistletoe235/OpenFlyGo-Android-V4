package edu.playground.djivln.survey

object SurveyLowBatteryPolicy {
    const val AUTO_RETURN_THRESHOLD_PERCENT = 20
    const val AUTO_RETURN_COUNTDOWN_MILLIS = 5_000L

    fun shouldTrigger(
        batteryPercent: Int,
        aircraftFlying: Boolean,
        simulatorActive: Boolean,
        executionState: SurveyExecutionState?,
    ): Boolean = batteryPercent in 0 until AUTO_RETURN_THRESHOLD_PERCENT &&
        aircraftFlying && !simulatorActive && executionState in setOf(
            SurveyExecutionState.ARMING,
            SurveyExecutionState.RUNNING,
            SurveyExecutionState.PAUSED,
        )
}
