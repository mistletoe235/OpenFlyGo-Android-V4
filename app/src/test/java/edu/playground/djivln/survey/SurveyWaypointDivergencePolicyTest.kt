package edu.playground.djivln.survey

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveyWaypointDivergencePolicyTest {
    @Test fun `sustained twelve metre growth pauses`() {
        assertFalse(SurveyWaypointDivergencePolicy.shouldPause(4.0, 16.0, 1_000, 3_499))
        assertTrue(SurveyWaypointDivergencePolicy.shouldPause(4.0, 16.0, 1_000, 3_500))
    }

    @Test fun `normal noise and improving error continue`() {
        assertFalse(SurveyWaypointDivergencePolicy.shouldPause(4.0, 15.9, 1_000, 9_000))
        assertFalse(SurveyWaypointDivergencePolicy.shouldPause(4.0, 3.0, 1_000, 9_000))
        assertFalse(SurveyWaypointDivergencePolicy.shouldPause(Double.NaN, 30.0, 1_000, 9_000))
    }
}
