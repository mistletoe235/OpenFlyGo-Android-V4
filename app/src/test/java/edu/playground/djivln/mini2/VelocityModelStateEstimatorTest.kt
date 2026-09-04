package edu.playground.djivln.mini2

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class VelocityModelStateEstimatorTest {
    @Test
    fun `integrates every flight telemetry sample in the initial body frame`() {
        val estimator = VelocityModelStateEstimator()
        estimator.update(1_000L, 1.0, 0.0, 0.0, 0.0)
        estimator.update(1_100L, 1.0, 0.0, 0.0, 0.0)
        estimator.update(1_200L, 1.0, 0.0, 0.0, 0.0)

        assertEquals(0.2, estimator.modelState()[0], 1e-6)
    }

    @Test
    fun `rotates north east displacement by the starting yaw`() {
        val estimator = VelocityModelStateEstimator()
        estimator.update(1_000L, 0.0, 1.0, 0.0, 90.0)
        estimator.update(2_000L, 0.0, 1.0, 0.0, 100.0)
        val state = estimator.modelState()

        assertEquals(1.0, state[0], 1e-6)
        assertEquals(0.0, state[1], 1e-6)
        assertEquals(Math.toRadians(10.0), state[3], 1e-6)
    }

    @Test
    fun `reset clears displacement and yaw origin`() {
        val estimator = VelocityModelStateEstimator()
        estimator.update(1_000L, 1.0, 0.0, 0.0, 0.0)
        estimator.update(1_500L, 1.0, 0.0, 0.0, 0.0)
        estimator.reset()

        assertArrayEquals(doubleArrayOf(0.0, 0.0, 0.0, 0.0), estimator.modelState(), 0.0)
    }
}
