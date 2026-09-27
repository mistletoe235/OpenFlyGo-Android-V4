package edu.playground.djivln.camera

import org.junit.Assert.assertEquals
import org.junit.Test

class CameraOrientationResolverTest {
    @Test fun `relative gimbal yaw is composed with aircraft heading`() {
        val result = CameraOrientationResolver.resolve(350.0, 0.5, -45.0, 12.0, 20.0)
        assertEquals(12.0, result.yawDegrees!!, 0.0)
        assertEquals(CameraOrientationResolver.SOURCE_ABSOLUTE_GIMBAL_CROSS_CHECKED, result.yawSource)
        assertEquals(2.0, result.yawConsistencyErrorDegrees!!, 0.0)
    }

    @Test fun `composed yaw wins when absolute gimbal yaw conflicts`() {
        val result = CameraOrientationResolver.resolve(350.0, 0.0, -45.0, 100.0, 20.0)
        assertEquals(10.0, result.yawDegrees!!, 0.0)
        assertEquals(CameraOrientationResolver.SOURCE_HEADING_PLUS_RELATIVE_GIMBAL_CONFLICT, result.yawSource)
        assertEquals(90.0, result.yawConsistencyErrorDegrees!!, 0.0)
    }

    @Test fun `absolute and aircraft fallbacks remain explicitly labelled`() {
        assertEquals(
            CameraOrientationResolver.SOURCE_ABSOLUTE_GIMBAL,
            CameraOrientationResolver.resolve(45.0, 0.0, -90.0, -30.0, null).yawSource,
        )
        assertEquals(
            CameraOrientationResolver.SOURCE_AIRCRAFT_HEADING_FALLBACK,
            CameraOrientationResolver.resolve(725.0, null, -90.0, null, null).yawSource,
        )
    }
}
