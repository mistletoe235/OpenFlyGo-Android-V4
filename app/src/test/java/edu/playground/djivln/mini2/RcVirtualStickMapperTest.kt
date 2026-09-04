package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Test

class RcVirtualStickMapperTest {
    @Test
    fun mapsMode2AxesToBodyVelocity() {
        val value = RcVirtualStickMapper.map(
            leftHorizontal = 330,
            leftVertical = 330,
            rightHorizontal = -165,
            rightVertical = 165,
        )

        assertEquals(1.0f, value.forwardMetersPerSecond, 1e-6f)
        assertEquals(-1.0f, value.rightMetersPerSecond, 1e-6f)
        assertEquals(1.0f, value.upMetersPerSecond, 1e-6f)
        assertEquals(45.0f, value.yawRateDegreesPerSecond, 1e-6f)
    }

    @Test
    fun appliesCenterDeadband() {
        val value = RcVirtualStickMapper.map(25, -25, 10, -10)

        assertEquals(0f, value.forwardMetersPerSecond, 0f)
        assertEquals(0f, value.rightMetersPerSecond, 0f)
        assertEquals(0f, value.upMetersPerSecond, 0f)
        assertEquals(0f, value.yawRateDegreesPerSecond, 0f)
    }
}
