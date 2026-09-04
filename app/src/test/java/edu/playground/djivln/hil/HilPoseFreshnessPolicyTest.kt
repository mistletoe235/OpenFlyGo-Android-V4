package edu.playground.djivln.hil

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HilPoseFreshnessPolicyTest {
    @Test
    fun groundedPoseMayBeRetransmittedWhenUnchanged() {
        assertTrue(HilPoseFreshnessPolicy.shouldSend(pose(sample = 1L, flags = 0), 9_000_000_000L))
        assertFalse(HilPoseFreshnessPolicy.isSampleUsable(0L, 9_000_000_000L, false))
    }

    @Test
    fun movingPoseIsRejectedAfterFiveHundredMilliseconds() {
        val moving = HilProtocol.POSE_FLAG_MOTORS_ON or HilProtocol.POSE_FLAG_FLYING
        assertTrue(HilPoseFreshnessPolicy.shouldSend(pose(1_000_000_000L, moving), 1_500_000_000L))
        assertFalse(HilPoseFreshnessPolicy.shouldSend(pose(1_000_000_000L, moving), 1_500_000_001L))
    }

    @Test
    fun movingPoseWithInvalidTimestampFailsClosed() {
        assertFalse(HilPoseFreshnessPolicy.shouldSend(
            pose(0L, HilProtocol.POSE_FLAG_FLYING), 1_000_000_000L,
        ))
        assertFalse(HilPoseFreshnessPolicy.shouldSend(
            pose(2_000_000_000L, HilProtocol.POSE_FLAG_FLYING), 1_000_000_000L,
        ))
    }

    private fun pose(sample: Long, flags: Int) = HilProtocol.Pose(
        sample, 31.0, 121.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0,
        0, 100f, flags,
    )
}
