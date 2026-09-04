package edu.playground.djivln.mini2

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StickTakeoverLatchTest {
    @Test
    fun `deliberate stick deflection triggers once while held`() {
        val latch = StickTakeoverLatch()

        assertTrue(latch.update(intArrayOf(0, 0, 51, 0), true))
        assertFalse(latch.update(intArrayOf(0, 0, 400, 0), true))
        assertFalse(latch.update(intArrayOf(0, 0, 60, 0), true))
    }

    @Test
    fun `stick must return near centre before next takeover`() {
        val latch = StickTakeoverLatch()

        assertTrue(latch.update(intArrayOf(-51, 0, 0, 0), true))
        assertFalse(latch.update(intArrayOf(-30, 0, 0, 0), true))
        assertFalse(latch.update(intArrayOf(-20, 0, 0, 0), true))
        assertTrue(latch.update(intArrayOf(0, 0, 0, 80), true))
    }

    @Test
    fun `stick movement cannot trigger when virtual stick is disabled`() {
        val latch = StickTakeoverLatch()

        assertFalse(latch.update(intArrayOf(0, 100, 0, 0), false))
        assertTrue(latch.update(intArrayOf(0, 100, 0, 0), true))
    }

    @Test
    fun `noise around trigger threshold does not retrigger until rearmed`() {
        val latch = StickTakeoverLatch()

        assertFalse(latch.update(intArrayOf(49, 0, 0, 0), true))
        assertTrue(latch.update(intArrayOf(50, 0, 0, 0), true))
        assertFalse(latch.update(intArrayOf(49, 0, 0, 0), true))
        assertFalse(latch.update(intArrayOf(21, 0, 0, 0), true))
        assertFalse(latch.update(intArrayOf(20, 0, 0, 0), true))
        assertTrue(latch.update(intArrayOf(50, 0, 0, 0), true))
    }
}
