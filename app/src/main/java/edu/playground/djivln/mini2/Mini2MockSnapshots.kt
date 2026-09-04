package edu.playground.djivln.mini2

import android.content.Context

/** Debug-only UI snapshots used to validate the iOS-parity HUD in an emulator. */
object Mini2MockSnapshots {
    @JvmStatic
    @JvmOverloads
    fun create(mode: String, context: Context? = null): Mini2AircraftBridge.Snapshot {
        val aircraftConnected = mode != "disconnected" && mode != "rc_only"
        val rcConnected = mode != "disconnected"
        val airborne = aircraftConnected && mode != "ground"
        return Mini2AircraftBridge.Snapshot(
            connected = aircraftConnected,
            product = if (aircraftConnected) "DJI Aircraft"
                else context?.getString(R.string.state_unrecognized) ?: "Unrecognized",
            flying = airborne,
            mode = if (airborne) "P-GPS" else "READY",
            satellites = if (aircraftConnected) 18 else 0,
            gpsLevel = if (aircraftConnected) "LEVEL_5" else "LEVEL_0",
            latitude = if (aircraftConnected) 31.2304 else Double.NaN,
            longitude = if (aircraftConnected) 121.4737 else Double.NaN,
            homeLatitude = if (aircraftConnected) 31.2302 else Double.NaN,
            homeLongitude = if (aircraftConnected) 121.4735 else Double.NaN,
            altitude = if (airborne) 12.4 else 0.0,
            asl = if (airborne) 18.7 else 6.3,
            heading = 128.0,
            horizontalSpeed = if (airborne) 1.6 else 0.0,
            verticalSpeed = if (airborne) 0.2 else 0.0,
            flightTimeSeconds = if (airborne) 94 else 0,
            remainingFlightTimeSeconds = if (airborne) 812 else 0,
            timeNeededToGoHomeSeconds = if (airborne) 180 else 0,
            timeNeededToLandSeconds = if (airborne) 45 else 0,
            batteryNeededToGoHomePercent = if (airborne) 28 else -1,
            batteryNeededToLandPercent = if (airborne) 12 else -1,
            aircraftBattery = if (aircraftConnected) 78 else -1,
            aircraftVoltageMv = if (aircraftConnected) 7_640 else 0,
            rcBattery = if (rcConnected) 86 else -1,
            rcSignal = if (rcConnected) 96 else -1,
            rcMode = "NORMAL",
            rcLocationValid = rcConnected,
            rcLatitude = if (rcConnected) 31.2301 else Double.NaN,
            rcLongitude = if (rcConnected) 121.4734 else Double.NaN,
            rcCourseDegrees = if (rcConnected && mode != "ground") 42.0 else Double.NaN,
            rcCourseValid = rcConnected && mode != "ground",
            gimbalPitch = if (airborne) -24.0 else 0.0,
            cameraMode = if (mode == "recording") "VIDEO" else "PHOTO",
            recording = mode == "recording",
            recordingSeconds = if (mode == "recording") 37 else 0,
            virtualStickEnabled = mode == "armed" || mode == "thinking" || mode == "manual_no_camera",
            sticksActive = false,
            simulatorAvailable = aircraftConnected,
            simulatorActive = mode == "simulator",
            simulatorMotorsOn = mode == "simulator",
            simulatorFlying = mode == "simulator",
            simulatorX = if (mode == "simulator") 3.2 else 0.0,
            simulatorY = if (mode == "simulator") -1.4 else 0.0,
            simulatorZ = if (mode == "simulator") 6.0 else 0.0,
            flightStateUpdatedAtMs = System.currentTimeMillis(),
            updatedAtMs = System.currentTimeMillis(),
        )
    }
}
