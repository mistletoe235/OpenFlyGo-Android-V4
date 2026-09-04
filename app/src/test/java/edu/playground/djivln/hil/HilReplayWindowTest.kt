package edu.playground.djivln.hil

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HilReplayWindowTest {
    @Test
    fun acceptsUniqueAdjacentReorderingButRejectsDuplicates() {
        val window = HilReplayWindow()

        assertTrue(window.accept(10L))
        assertTrue(window.accept(12L))
        assertTrue(window.accept(11L))
        assertFalse(window.accept(11L))
        assertFalse(window.accept(12L))
        assertTrue(window.accept(13L))
    }

    @Test
    fun rejectsPacketsOutsideTheSixtyFourPacketWindow() {
        val window = HilReplayWindow()

        assertTrue(window.accept(1L))
        assertTrue(window.accept(65L))
        assertFalse(window.accept(1L))
        assertTrue(window.accept(2L))
        assertFalse(window.accept(2L))
    }

    @Test
    fun resetAllowsPeerSequenceRestartAfterWatchdogTransition() {
        val window = HilReplayWindow()

        assertTrue(window.accept(7L))
        assertFalse(window.accept(7L))
        window.reset()
        assertTrue(window.accept(1L))
    }

    @Test
    fun rejectsNonPositiveSequenceValues() {
        val window = HilReplayWindow()

        assertFalse(window.accept(0L))
        assertFalse(window.accept(-1L))
    }
}
