package edu.playground.djivln.survey

object SurveyWaypointDivergencePolicy {
    const val MINIMUM_GROWTH_METERS = 12.0
    const val MINIMUM_DURATION_MILLIS = 2_500L

    fun shouldPause(
        bestErrorMeters: Double,
        currentErrorMeters: Double,
        lastProgressElapsedMillis: Long,
        nowElapsedMillis: Long,
    ): Boolean = bestErrorMeters.isFinite() && currentErrorMeters.isFinite() &&
        currentErrorMeters >= bestErrorMeters + MINIMUM_GROWTH_METERS &&
        nowElapsedMillis - lastProgressElapsedMillis >= MINIMUM_DURATION_MILLIS
}
