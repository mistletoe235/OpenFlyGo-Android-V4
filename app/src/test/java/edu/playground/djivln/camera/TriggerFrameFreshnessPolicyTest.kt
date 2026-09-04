package edu.playground.djivln.camera

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerFrameFreshnessPolicyTest {
    @Test fun `rejects a fresh frame arriving too late after the trigger`() {
        val trigger = 1_000_000_000L
        assertTrue(TriggerFrameFreshnessPolicy.isFreshPostTriggerFrame(
            3, 4, trigger, trigger + 500_000_000L, trigger + 500_000_000L))
        assertFalse(TriggerFrameFreshnessPolicy.isFreshPostTriggerFrame(
            3, 4, trigger, trigger + 501_000_000L, trigger + 501_000_000L))
    }

    @Test fun `requires a sequence received after the photo trigger`() {
        val trigger = 1_000_000_000L
        assertFalse(TriggerFrameFreshnessPolicy.isFreshPostTriggerFrame(
            7, 7, trigger, trigger + 1, trigger + 2))
        assertFalse(TriggerFrameFreshnessPolicy.isFreshPostTriggerFrame(
            7, 8, trigger, trigger - 1, trigger + 2))
        assertTrue(TriggerFrameFreshnessPolicy.isFreshPostTriggerFrame(
            7, 8, trigger, trigger + 1, trigger + 2))
    }

    @Test fun `rejects a post trigger frame older than five hundred milliseconds`() {
        val trigger = 1_000_000_000L
        val frame = trigger + 1_000_000L
        assertFalse(TriggerFrameFreshnessPolicy.isFreshPostTriggerFrame(
            3, 4, trigger, frame, frame + 501_000_000L))
    }
}
