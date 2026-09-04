package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneHeadingSourceTest {
    @Test fun `heading smoothing crosses north through shortest arc`() {
        val value = PhoneHeadingSource.smooth(359.0, 1.0)
        assertTrue(value > 359.0 || value < 1.0)
        assertEquals(0.0, PhoneHeadingSource.normalize(360.0), 0.0)
    }

    @Test fun `only fresh finite heading is displayable`() {
        val heading = PhoneHeadingSource.Heading(90.0, 0, 10L)
        assertTrue(PhoneHeadingSource.isDisplayable(heading, 20L))
        assertFalse(PhoneHeadingSource.isDisplayable(
            heading, 10L + PhoneHeadingSource.MAX_HEADING_AGE_NANOS + 1L))
    }
}
