package edu.playground.djivln.mini2

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VirtualStickCommandLeasePolicyTest {
    @Test fun `non-zero command expires after one second without refresh`() {
        assertFalse(VirtualStickCommandLeasePolicy.shouldExpire(true, 1_000L, 1_999L))
        assertTrue(VirtualStickCommandLeasePolicy.shouldExpire(true, 1_000L, 2_000L))
    }

    @Test fun `zero command and missing refresh never expire`() {
        assertFalse(VirtualStickCommandLeasePolicy.shouldExpire(false, 1_000L, 9_000L))
        assertFalse(VirtualStickCommandLeasePolicy.shouldExpire(true, 0L, 9_000L))
    }
}
