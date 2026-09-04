package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AltitudeTelemetryResolverTest {
    @Test
    fun resolvesBarometricEstimatedAslAndDownwardRangeSeparately() {
        val result = AltitudeTelemetryResolver.resolve(
            relativeBarometricMeters = 2.4,
            takeoffAslMeters = 31.2,
            downwardRangeUsed = true,
            downwardRangeHasError = false,
            downwardRangeMeters = 1.7,
            downwardVisionActive = true,
        )

        assertEquals(2.4, result.relativeBarometricMeters, 0.0)
        assertEquals(33.6, result.estimatedAslMeters, 1e-9)
        assertEquals(1.7, result.groundClearanceMeters, 0.0)
        assertEquals(AltitudeTelemetryResolver.SOURCE_DOWNWARD_RANGE, result.groundClearanceSource)
        assertTrue(result.groundClearanceReliableForSafety)
        assertTrue(result.downwardVisionActive)
    }

    @Test
    fun unsupportedAircraftReportsNoGroundClearance() {
        val result = AltitudeTelemetryResolver.resolve(
            relativeBarometricMeters = 8.0,
            takeoffAslMeters = 0.0,
            downwardRangeUsed = false,
            downwardRangeHasError = false,
            downwardRangeMeters = 0.0,
            downwardVisionActive = false,
        )

        assertTrue(result.groundClearanceMeters.isNaN())
        assertEquals(AltitudeTelemetryResolver.SOURCE_NONE, result.groundClearanceSource)
        assertFalse(result.groundClearanceReliableForSafety)
    }

    @Test
    fun badOrHighRangeIsNeverUsedAsLowAltitudeSafetyMeasurement() {
        val errored = AltitudeTelemetryResolver.resolve(
            2.0, 10.0, true, true, 0.3, false,
        )
        val high = AltitudeTelemetryResolver.resolve(
            6.0, 10.0, true, false, 8.0, false,
        )

        assertTrue(errored.groundClearanceMeters.isNaN())
        assertEquals(8.0, high.groundClearanceMeters, 0.0)
        assertFalse(high.groundClearanceReliableForSafety)
    }

    @Test
    fun invalidInputsStayUnavailableInsteadOfBecomingZero() {
        val result = AltitudeTelemetryResolver.resolve(
            Double.NaN, Double.NaN, true, false, Double.NaN, false,
        )

        assertTrue(result.relativeBarometricMeters.isNaN())
        assertTrue(result.estimatedAslMeters.isNaN())
        assertTrue(result.groundClearanceMeters.isNaN())
    }
}
