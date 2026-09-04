package edu.playground.djivln.camera

/** Timing contract for the downlink frame saved alongside a DJI photo trigger. */
object TriggerFrameFreshnessPolicy {
    const val MAX_FRAME_AGE_MILLIS = 500L

    fun isFreshPostTriggerFrame(
        baselineSequence: Long,
        candidateSequence: Long,
        triggerElapsedRealtimeNanos: Long,
        frameReceivedElapsedRealtimeNanos: Long,
        nowElapsedRealtimeNanos: Long,
    ): Boolean {
        if (candidateSequence <= baselineSequence) return false
        if (frameReceivedElapsedRealtimeNanos < triggerElapsedRealtimeNanos) return false
        if (frameReceivedElapsedRealtimeNanos - triggerElapsedRealtimeNanos >
            MAX_FRAME_AGE_MILLIS * 1_000_000L) return false
        val ageNanos = nowElapsedRealtimeNanos - frameReceivedElapsedRealtimeNanos
        return ageNanos >= 0L && ageNanos <= MAX_FRAME_AGE_MILLIS * 1_000_000L
    }
}
