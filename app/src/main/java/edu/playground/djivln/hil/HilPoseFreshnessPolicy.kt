package edu.playground.djivln.hil

internal object HilPoseFreshnessPolicy {
    const val MOVING_MAXIMUM_AGE_NANOS = 500_000_000L

    @JvmStatic
    fun isSampleUsable(sampleNanos: Long, nowNanos: Long, moving: Boolean): Boolean {
        if (!moving) return sampleNanos > 0L
        if (sampleNanos <= 0L || nowNanos < sampleNanos) return false
        return nowNanos - sampleNanos <= MOVING_MAXIMUM_AGE_NANOS
    }

    fun shouldSend(pose: HilProtocol.Pose, nowNanos: Long): Boolean {
        val moving = pose.stateFlags and
            (HilProtocol.POSE_FLAG_MOTORS_ON or HilProtocol.POSE_FLAG_FLYING) != 0
        return isSampleUsable(pose.sampleMonotonicNanos, nowNanos, moving)
    }
}
