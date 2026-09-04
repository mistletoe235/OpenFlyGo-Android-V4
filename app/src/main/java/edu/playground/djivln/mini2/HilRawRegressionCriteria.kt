package edu.playground.djivln.mini2

object HilRawRegressionCriteria {
    @JvmStatic
    fun minimumFrequencyHz(requestedHz: Int): Double = maxOf(10.0, requestedHz * 0.5)

    @JvmStatic
    fun rawPass(
        requestedHz: Int,
        measuredRawHz: Double,
        motorsObserved: Boolean,
        flyingObserved: Boolean,
        altitudeGainMeters: Double,
        forwardMovementMeters: Double,
        attitudeChangeDegrees: Double,
    ): Boolean = measuredRawHz >= minimumFrequencyHz(requestedHz) &&
        motorsObserved &&
        flyingObserved &&
        altitudeGainMeters >= 0.05 &&
        forwardMovementMeters >= 0.05 &&
        attitudeChangeDegrees >= 0.5

    @JvmStatic
    fun linkPass(
        requestedHz: Int,
        peerFresh: Boolean,
        sentPoseCount: Long,
        receivedPoseCount: Long,
        measuredPoseHz: Double,
    ): Boolean = peerFresh &&
        sentPoseCount >= 20L &&
        receivedPoseCount >= 20L &&
        measuredPoseHz >= minimumFrequencyHz(requestedHz)
}
