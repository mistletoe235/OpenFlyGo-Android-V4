package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Test

class ContinuousStepVelocityTest {
    @Test
    fun `queued step preserves measured horizontal speed`() {
        val command = ContinuousStepVelocity.preserveHorizontalMomentum(
            0.08, 0.0, 0.74, 2.0, true
        )

        assertEquals(0.74, command[0], 1e-6)
        assertEquals(0.0, command[1], 1e-6)
    }

    @Test
    fun `momentum remains bounded by selected speed limit`() {
        val command = ContinuousStepVelocity.preserveHorizontalMomentum(
            0.03, 0.04, 1.2, 0.5, true
        )

        assertEquals(0.5, Math.hypot(command[0], command[1]), 1e-6)
    }

    @Test
    fun `final step keeps proportional braking`() {
        val command = ContinuousStepVelocity.preserveHorizontalMomentum(
            0.08, -0.02, 0.8, 2.0, false
        )

        assertEquals(0.08, command[0], 1e-6)
        assertEquals(-0.02, command[1], 1e-6)
    }

    @Test
    fun `zero horizontal target never invents a direction`() {
        val command = ContinuousStepVelocity.preserveHorizontalMomentum(
            0.0, 0.0, 0.8, 2.0, true
        )

        assertEquals(0.0, command[0], 1e-6)
        assertEquals(0.0, command[1], 1e-6)
    }
}
