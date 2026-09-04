package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FlightEnduranceBarPolicyTest {
    @Test fun `normalizes MSDK4 endurance telemetry`() {
        val state = FlightEnduranceBarPolicy.from(Mini2AircraftBridge.Snapshot(
            connected = true,
            aircraftBattery = 72,
            remainingFlightTimeSeconds = 812,
            batteryNeededToGoHomePercent = 28,
            batteryNeededToLandPercent = 12,
        ))
        assertEquals("13:32", state.timeLabel)
        assertEquals(28, state.goHomePercent)
        assertEquals(12, state.landPercent)
    }

    @Test fun `invalid assessment percentages remain unavailable`() {
        val state = FlightEnduranceBarPolicy.from(Mini2AircraftBridge.Snapshot(
            batteryNeededToGoHomePercent = 255,
            batteryNeededToLandPercent = -1,
        ))
        assertNull(state.goHomePercent)
        assertNull(state.landPercent)
        assertEquals("--:--", state.timeLabel)
    }
}
