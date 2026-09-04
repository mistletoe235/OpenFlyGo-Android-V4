package edu.playground.djivln.mini2

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HilRawRegressionCriteriaTest {
    @Test
    fun requiresEveryRawEvidenceGate() {
        assertTrue(HilRawRegressionCriteria.rawPass(100, 50.0, true, true, 0.05, 0.05, 0.5))
        assertFalse(HilRawRegressionCriteria.rawPass(100, 49.9, true, true, 0.05, 0.05, 0.5))
        assertFalse(HilRawRegressionCriteria.rawPass(100, 50.0, false, true, 0.05, 0.05, 0.5))
        assertFalse(HilRawRegressionCriteria.rawPass(100, 50.0, true, false, 0.05, 0.05, 0.5))
        assertFalse(HilRawRegressionCriteria.rawPass(100, 50.0, true, true, 0.049, 0.05, 0.5))
        assertFalse(HilRawRegressionCriteria.rawPass(100, 50.0, true, true, 0.05, 0.049, 0.5))
        assertFalse(HilRawRegressionCriteria.rawPass(100, 50.0, true, true, 0.05, 0.05, 0.49))
    }

    @Test
    fun requiresFreshBidirectionalPoseLinkAtTargetRate() {
        assertTrue(HilRawRegressionCriteria.linkPass(100, true, 20, 20, 50.0))
        assertFalse(HilRawRegressionCriteria.linkPass(100, false, 20, 20, 50.0))
        assertFalse(HilRawRegressionCriteria.linkPass(100, true, 19, 20, 50.0))
        assertFalse(HilRawRegressionCriteria.linkPass(100, true, 20, 19, 50.0))
        assertFalse(HilRawRegressionCriteria.linkPass(100, true, 20, 20, 49.9))
    }

    @Test
    fun frequencyThresholdHasTenHertzFloor() {
        assertTrue(HilRawRegressionCriteria.rawPass(10, 10.0, true, true, 0.05, 0.05, 0.5))
        assertFalse(HilRawRegressionCriteria.rawPass(10, 9.9, true, true, 0.05, 0.05, 0.5))
    }
}
