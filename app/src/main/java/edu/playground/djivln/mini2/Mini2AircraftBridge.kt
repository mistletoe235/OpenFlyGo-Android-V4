package edu.playground.djivln.mini2

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import dji.common.camera.SettingsDefinitions.CameraMode
import dji.common.camera.SettingsDefinitions.FlatCameraMode
import dji.common.camera.SettingsDefinitions.StorageLocation
import dji.common.error.DJIError
import dji.common.flightcontroller.FlightControllerState
import dji.common.flightcontroller.simulator.InitializationData
import dji.common.flightcontroller.virtualstick.FlightControlData
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem
import dji.common.flightcontroller.virtualstick.RollPitchControlMode
import dji.common.flightcontroller.virtualstick.VerticalControlMode
import dji.common.flightcontroller.virtualstick.YawControlMode
import dji.common.gimbal.Rotation
import dji.common.gimbal.RotationMode
import dji.common.remotecontroller.HardwareState
import dji.common.remotecontroller.ChargeMobileMode
import dji.common.util.CommonCallbacks
import dji.common.model.LocationCoordinate2D
import dji.sdk.camera.Camera
import dji.sdk.media.MediaFile
import dji.sdk.flightcontroller.FlightController
import dji.sdk.flightcontroller.Simulator
import dji.sdk.products.Aircraft
import dji.sdk.sdkmanager.DJISDKManager
import edu.playground.djivln.survey.CameraProfile
import edu.playground.djivln.survey.DjiCameraProfileCatalog
import edu.playground.djivln.camera.SurveyCameraSourcePolicy
import edu.playground.djivln.mini2.R
import kotlin.math.hypot

/** Generic MSDK4 aircraft adapter used by the complete DJI VLN flight UI. */
class Mini2AircraftBridge @JvmOverloads constructor(
    private val listener: Listener,
    context: Context? = null,
) : AutoCloseable {
    private val appContext = context?.applicationContext
    interface Listener {
        fun onSnapshot(snapshot: Snapshot)
        fun onAction(label: String, ok: Boolean, message: String)
        fun onManualTakeover()
        fun onSimulatorSample(sample: SimulatorSample) {}
    }

    fun interface MediaFilesListener {
        fun onMediaFiles(files: List<MediaFile>, error: String?)
    }

    fun interface MediaThumbnailListener {
        fun onThumbnail(bitmap: android.graphics.Bitmap?)
    }

    fun interface PhotoResultListener {
        fun onResult(ok: Boolean, message: String)
    }

    fun interface PhotoTriggeredListener {
        fun onTriggered(elapsedRealtimeNanos: Long, epochMillis: Long)
    }

    fun interface ActionResultListener {
        fun onResult(ok: Boolean, message: String)
    }

    data class Snapshot(
        val connected: Boolean = false,
        val product: String = "--",
        val flying: Boolean = false,
        val mode: String = "--",
        val satellites: Int = 0,
        val gpsLevel: String = "--",
        val latitude: Double = Double.NaN,
        val longitude: Double = Double.NaN,
        val homeLatitude: Double = Double.NaN,
        val homeLongitude: Double = Double.NaN,
        val homeLocationSet: Boolean = false,
        val imuPreheating: Boolean = false,
        /** Barometric height relative to the takeoff location. */
        val altitude: Double = Double.NaN,
        /** Estimated mean-sea-level altitude; not a direct terrain measurement. */
        val asl: Double = Double.NaN,
        /** Height above the surface currently seen by the downward range sensor. */
        val groundClearance: Double = Double.NaN,
        val groundClearanceSource: String = AltitudeTelemetryResolver.SOURCE_NONE,
        val groundClearanceReliableForSafety: Boolean = false,
        val downwardVisionActive: Boolean = false,
        val heading: Double = 0.0,
        /** DJI aircraft attitude pitch; positive means nose-down/forward. */
        val aircraftPitch: Double = 0.0,
        val velocityNorth: Double = 0.0,
        val velocityEast: Double = 0.0,
        val horizontalSpeed: Double = 0.0,
        val verticalSpeed: Double = 0.0,
        val flightTimeSeconds: Int = 0,
        val remainingFlightTimeSeconds: Int = 0,
        val timeNeededToGoHomeSeconds: Int = 0,
        val timeNeededToLandSeconds: Int = 0,
        val batteryNeededToGoHomePercent: Int = -1,
        val batteryNeededToLandPercent: Int = -1,
        val goingHome: Boolean = false,
        val landing: Boolean = false,
        val landingConfirmationNeeded: Boolean = false,
        val aircraftBattery: Int = -1,
        val aircraftVoltageMv: Int = 0,
        val lowBatteryWarning: Boolean = false,
        val seriousLowBatteryWarning: Boolean = false,
        val rcBattery: Int = -1,
        val rcSignal: Int = -1,
        val rcMode: String = "--",
        val rcGoHomeButtonPresent: Boolean = false,
        val rcGoHomeButtonPressed: Boolean = false,
        val rcLocationValid: Boolean = false,
        val rcLatitude: Double = Double.NaN,
        val rcLongitude: Double = Double.NaN,
        /** Ground course from RC GPS velocity; unavailable while the controller is stationary. */
        val rcCourseDegrees: Double = Double.NaN,
        val rcCourseValid: Boolean = false,
        val rcPhoneChargingAvailable: Boolean = false,
        val rcPhoneChargingMode: String = "UNKNOWN",
        val gimbalPitch: Double = 0.0,
        val gimbalPitchAtStop: Boolean = false,
        val gimbalMotorOverloaded: Boolean = false,
        val gimbalMode: String = "--",
        val cameraMode: String = "PHOTO",
        val recording: Boolean = false,
        val recordingSeconds: Int = 0,
        val virtualStickEnabled: Boolean = false,
        val sticksActive: Boolean = false,
        val simulatorAvailable: Boolean = false,
        val simulatorActive: Boolean = false,
        val simulatorMotorsOn: Boolean = false,
        val simulatorFlying: Boolean = false,
        val simulatorX: Double = 0.0,
        val simulatorY: Double = 0.0,
        val simulatorZ: Double = 0.0,
        val simulatorRoll: Double = 0.0,
        val simulatorPitch: Double = 0.0,
        val simulatorYaw: Double = 0.0,
        val simulatorStateHz: Double = 0.0,
        val simulatorStateSequence: Long = 0L,
        val goHomeHeightMeters: Int = -1,
        val maxFlightHeightMeters: Int = -1,
        val maxFlightRadiusMeters: Int = -1,
        val maxFlightRadiusEnabled: Boolean = false,
        val flightStateUpdatedAtMs: Long = 0L,
        val updatedAtMs: Long = 0L,
    ) {
        val homeDistanceMeters: Double
            get() {
                if (!latitude.isFinite() || !longitude.isFinite() ||
                    !homeLatitude.isFinite() || !homeLongitude.isFinite()
                ) return Double.NaN
                val north = (latitude - homeLatitude) * 111_111.0
                val east = (longitude - homeLongitude) * 95_000.0
                return hypot(north, east)
            }
    }

    /** High-rate DJI simulator sample normalized to HIL ENU position and FRU attitude. */
    data class SimulatorSample(
        val sequence: Long,
        val elapsedRealtimeNanos: Long,
        val originLatitude: Double,
        val originLongitude: Double,
        val eastMeters: Double,
        val northMeters: Double,
        val downMeters: Double,
        val rollDegrees: Double,
        val pitchDegrees: Double,
        val yawDegrees: Double,
        val motorsOn: Boolean,
        val flying: Boolean,
        val measuredRateHz: Double,
        val commandForward: Double,
        val commandRight: Double,
        val commandUp: Double,
        val commandYawRate: Double,
    )

    private val main = Handler(Looper.getMainLooper())
    private var aircraft: Aircraft? = null
    private var flightController: FlightController? = null
    private var camera: Camera? = null
    @Volatile private var snapshot = Snapshot()
    private val stickTakeoverLatch = StickTakeoverLatch()
    @Volatile private var simulatorUpdateFrequencyHz = DEFAULT_SIMULATOR_STATE_HZ
    @Volatile private var simulatorOriginLatitude = Double.NaN
    @Volatile private var simulatorOriginLongitude = Double.NaN
    @Volatile private var lastCommandForward = 0.0
    @Volatile private var lastCommandRight = 0.0
    @Volatile private var lastCommandUp = 0.0
    @Volatile private var lastCommandYawRate = 0.0
    @Volatile private var commandLeaseRefreshedAtMillis = 0L
    private val commandLeaseGeneration = CallbackGeneration()
    private val commandLeaseRunnable = Runnable { expireCommandLeaseIfNeeded() }
    private var simulatorSampleSequence = 0L
    private var simulatorRateWindowStartedNanos = 0L
    private var simulatorRateWindowSamples = 0L
    @Volatile private var measuredSimulatorStateHz = 0.0
    @Volatile private var latestSimulatorSample: SimulatorSample? = null
    private val productSessionGeneration = CallbackGeneration()
    private val virtualStickTransitionGeneration = CallbackGeneration()
    private val mediaSessionGeneration = CallbackGeneration()
    @Volatile private var mediaSessionActive = false
    private var mediaPreviousMode: CameraMode? = null
    private var mediaPreviousFlatMode: FlatCameraMode? = null
    private var lastSimulatorSnapshotPublishNanos = 0L
    private var lastLoggedSimulatorMotorsOn: Boolean? = null
    private var lastLoggedSimulatorFlying: Boolean? = null
    private var lastCscCandidate = false
    private var lastRcGoHomeButtonPresent: Boolean? = null
    private var lastRcGoHomeButtonPressed: Boolean? = null
    private var rcGoHomePressedAtElapsedMs = 0L
    private var rcGoHomeFallbackGeneration = 0L
    private var rcGoHomeFallbackIssued = false
    @Volatile private var rcGoHomeObservationUntilElapsedMs = 0L
    private var lastObservedGoingHome: Boolean? = null
    private var lastObservedLanding: Boolean? = null
    private var lastObservedFlightMode: String? = null

    fun bindCurrentProduct() {
        val product = DJISDKManager.getInstance().product
        if (product !is Aircraft) {
            if (aircraft != null || flightController != null) invalidateCurrentProduct()
            snapshot = Snapshot(
                connected = false,
                product = if (product == null) text(R.string.unrecognized, "Unrecognized") else "DJI Aircraft",
            )
            publish()
            return
        }
        if (aircraft === product && flightController != null) {
            // Components on MSDK4 may become available after the Aircraft object itself.
            // Refreshing these callbacks is required when an RC is plugged in later.
            snapshot = snapshot.copy(
                connected = product.isConnected,
                product = product.model?.displayName ?: "DJI Aircraft",
                updatedAtMs = System.currentTimeMillis(),
            )
            refreshCameraBinding(product, productSessionGeneration.current())
            bindRemoteControllerCallbacks(product)
            publish()
            return
        }
        unbindCallbacks()
        aircraft = product
        flightController = product.flightController
        camera = selectSurveyCamera(product)
        val generation = productSessionGeneration.current()
        snapshot = snapshot.copy(
            connected = product.isConnected,
            product = product.model?.displayName ?: "DJI Aircraft",
        )

        val boundFlightController = flightController
        boundFlightController?.setStateCallback { state ->
            if (isCurrentProductSession(generation, product) && flightController === boundFlightController) {
                handleFlightState(state)
            }
        }
        refreshFlightSafetySettings()
        flightController?.simulator?.let { simulator ->
            snapshot = snapshot.copy(
                simulatorAvailable = true,
                simulatorActive = simulator.isSimulatorActive,
            )
            bindSimulatorStateCallback(simulator)
        }
        product.battery?.setStateCallback { state ->
            if (!isCurrentProductSession(generation, product)) return@setStateCallback
            snapshot = snapshot.copy(
                aircraftBattery = state.chargeRemainingInPercent,
                aircraftVoltageMv = state.voltage,
                updatedAtMs = System.currentTimeMillis(),
            )
            publish()
        }
        product.airLink?.setUplinkSignalQualityCallback { quality ->
            if (!isCurrentProductSession(generation, product)) return@setUplinkSignalQualityCallback
            snapshot = snapshot.copy(rcSignal = quality, updatedAtMs = System.currentTimeMillis())
            publish()
        }
        bindRemoteControllerCallbacks(product)
        product.gimbals?.firstOrNull()?.setStateCallback { state ->
            if (!isCurrentProductSession(generation, product)) return@setStateCallback
            snapshot = snapshot.copy(
                gimbalPitch = state.attitudeInDegrees.pitch.toDouble(),
                gimbalPitchAtStop = state.isPitchAtStop,
                gimbalMotorOverloaded = state.isMotorOverloaded,
                gimbalMode = state.mode?.name ?: "--",
                updatedAtMs = System.currentTimeMillis(),
            )
            publish()
        }
        bindCameraStateCallback(product, generation)
        publish()
    }

    private fun selectSurveyCamera(product: Aircraft): Camera? {
        val available = surveyCameraCandidates(product)
        val selectedIndex = SurveyCameraSourcePolicy.selectedIndex(available.map {
            SurveyCameraSourcePolicy.Candidate(it.isConnected, it.isThermalCamera)
        })
        return selectedIndex?.let(available::get) ?: product.camera
    }

    private fun surveyCameraCandidates(product: Aircraft): List<Camera> {
        val listed = product.cameras.orEmpty()
        val fallback = product.camera
        return if (fallback != null && listed.none { it === fallback }) listed + fallback else listed
    }

    private fun refreshCameraBinding(product: Aircraft, generation: Long) {
        val selected = selectSurveyCamera(product)
        if (camera === selected) return
        runCatching { camera?.setSystemStateCallback(null) }
        camera = selected
        bindCameraStateCallback(product, generation)
    }

    private fun bindCameraStateCallback(product: Aircraft, generation: Long) {
        val boundCamera = camera
        boundCamera?.setSystemStateCallback { state ->
            if (!isCurrentProductSession(generation, product) || camera !== boundCamera) {
                return@setSystemStateCallback
            }
            snapshot = snapshot.copy(
                cameraMode = when (state.mode) {
                    CameraMode.RECORD_VIDEO -> "VIDEO"
                    CameraMode.PLAYBACK, CameraMode.MEDIA_DOWNLOAD -> "ALBUM"
                    else -> "PHOTO"
                },
                recording = state.isRecording,
                recordingSeconds = state.currentVideoRecordingTimeInSeconds,
                updatedAtMs = System.currentTimeMillis(),
            )
            publish()
        }
    }

    /** Camera geometry for survey planning; unknown payloads use a clearly unverified fallback. */
    fun currentSurveyCameraResolution(): DjiCameraProfileCatalog.Resolution {
        val product = aircraft
        return DjiCameraProfileCatalog.resolveLocalized(appContext,
            product?.model?.name,
            product?.model?.displayName,
            camera?.displayName,
        )
    }

    fun currentSurveyCameraProfile(): CameraProfile = currentSurveyCameraResolution().profile

    fun isSurveyCameraConnected(): Boolean = camera?.isConnected == true

    /**
     * MSDK4 exposes a primary video feed separately from Camera objects. Only claim a trigger-
     * aligned phone frame when the connected camera is unique and the product is not a known
     * multi-lens enterprise camera whose stream source cannot be proven by this API.
     */
    fun isTriggerAlignedFrameSourceUnambiguous(): Boolean {
        val product = aircraft ?: return false
        val available = surveyCameraCandidates(product)
        val selectedIndex = available.indexOfFirst { it === camera }.takeIf { it >= 0 }
        val resolved = currentSurveyCameraResolution()
        return SurveyCameraSourcePolicy.canAssociatePrimaryFeed(
            available.map { SurveyCameraSourcePolicy.Candidate(it.isConnected, it.isThermalCamera) },
            selectedIndex,
            resolved.verifiedProfile,
            product.model?.name,
            product.model?.displayName,
        )
    }

    fun currentSurveyCameraProfileLabel(): String {
        val resolved = currentSurveyCameraResolution()
        return resolved.displayName + if (resolved.verifiedProfile) ""
        else text(R.string.camera_uncalibrated_suffix, " · Uncalibrated")
    }

    private fun bindSimulatorStateCallback(simulator: Simulator) {
        val generation = productSessionGeneration.current()
        simulator.setStateCallback callback@ { state ->
            if (!productSessionGeneration.accepts(generation) || flightController?.simulator !== simulator) {
                Log.w(TAG, "Ignoring stale SimulatorState callback generation=$generation current=${productSessionGeneration.current()}")
                return@callback
            }
            val nowNanos = SystemClock.elapsedRealtimeNanos()
            if (simulatorRateWindowStartedNanos == 0L) simulatorRateWindowStartedNanos = nowNanos
            simulatorRateWindowSamples += 1L
            val windowNanos = nowNanos - simulatorRateWindowStartedNanos
            if (windowNanos >= 1_000_000_000L) {
                measuredSimulatorStateHz = simulatorRateWindowSamples * 1_000_000_000.0 / windowNanos
                simulatorRateWindowSamples = 0L
                simulatorRateWindowStartedNanos = nowNanos
            }
            val sequence = ++simulatorSampleSequence
            val position = DjiSimulatorCoordinates.fromSdk(
                state.positionX, state.positionY, state.positionZ, state.roll, state.pitch,
            )
            val sample = SimulatorSample(
                sequence = sequence,
                elapsedRealtimeNanos = nowNanos,
                originLatitude = simulatorOriginLatitude,
                originLongitude = simulatorOriginLongitude,
                eastMeters = position.eastMeters,
                northMeters = position.northMeters,
                downMeters = position.downMeters,
                rollDegrees = position.rollDegrees,
                pitchDegrees = position.pitchDegrees,
                yawDegrees = state.yaw.toDouble(),
                motorsOn = state.areMotorsOn(),
                flying = state.isFlying,
                measuredRateHz = measuredSimulatorStateHz,
                commandForward = lastCommandForward,
                commandRight = lastCommandRight,
                commandUp = lastCommandUp,
                commandYawRate = lastCommandYawRate,
            )
            latestSimulatorSample = sample
            if (lastLoggedSimulatorMotorsOn != sample.motorsOn ||
                lastLoggedSimulatorFlying != sample.flying
            ) {
                Log.i(
                    TAG,
                    "Simulator state motors=${sample.motorsOn} flying=${sample.flying} " +
                        "xyz=${"%.2f".format(sample.eastMeters)}," +
                        "${"%.2f".format(sample.northMeters)},${"%.2f".format(-sample.downMeters)}",
                )
                lastLoggedSimulatorMotorsOn = sample.motorsOn
                lastLoggedSimulatorFlying = sample.flying
            }
            runCatching { listener.onSimulatorSample(sample) }
                .onFailure { Log.e(TAG, "Simulator sample listener failed", it) }
            if (nowNanos - lastSimulatorSnapshotPublishNanos >= SIMULATOR_UI_INTERVAL_NANOS) {
                lastSimulatorSnapshotPublishNanos = nowNanos
                snapshot = snapshot.copy(
                    simulatorAvailable = true,
                    simulatorActive = simulator.isSimulatorActive,
                    simulatorMotorsOn = sample.motorsOn,
                    simulatorFlying = sample.flying,
                    simulatorX = sample.eastMeters,
                    simulatorY = sample.northMeters,
                    simulatorZ = sample.downMeters,
                    simulatorRoll = sample.rollDegrees,
                    simulatorPitch = sample.pitchDegrees,
                    simulatorYaw = sample.yawDegrees,
                    simulatorStateHz = sample.measuredRateHz,
                    simulatorStateSequence = sample.sequence,
                    updatedAtMs = System.currentTimeMillis(),
                )
                publish()
            }
        }
    }

    fun refreshSimulatorStateCallback(): Boolean {
        val simulator = flightController?.simulator ?: return false
        bindSimulatorStateCallback(simulator)
        Log.i(TAG, "SimulatorState callback rebound active=${simulator.isSimulatorActive}")
        return true
    }

    fun isSimulatorActuallyActive(): Boolean =
        flightController?.simulator?.isSimulatorActive == true

    private fun refreshFlightSafetySettings() {
        val controller = flightController ?: return
        controller.getGoHomeHeightInMeters(object : CommonCallbacks.CompletionCallbackWith<Int> {
            override fun onSuccess(value: Int) {
                if (flightController !== controller) return
                snapshot = snapshot.copy(goHomeHeightMeters = value, updatedAtMs = System.currentTimeMillis())
                publish()
            }
            override fun onFailure(error: DJIError) = Unit
        })
        controller.getMaxFlightHeight(object : CommonCallbacks.CompletionCallbackWith<Int> {
            override fun onSuccess(value: Int) {
                if (flightController !== controller) return
                snapshot = snapshot.copy(maxFlightHeightMeters = value, updatedAtMs = System.currentTimeMillis())
                publish()
            }
            override fun onFailure(error: DJIError) = Unit
        })
        controller.getMaxFlightRadius(object : CommonCallbacks.CompletionCallbackWith<Int> {
            override fun onSuccess(value: Int) {
                if (flightController !== controller) return
                snapshot = snapshot.copy(maxFlightRadiusMeters = value, updatedAtMs = System.currentTimeMillis())
                publish()
            }
            override fun onFailure(error: DJIError) = Unit
        })
        controller.getMaxFlightRadiusLimitationEnabled(
            object : CommonCallbacks.CompletionCallbackWith<Boolean> {
            override fun onSuccess(value: Boolean) {
                if (flightController !== controller) return
                snapshot = snapshot.copy(maxFlightRadiusEnabled = value, updatedAtMs = System.currentTimeMillis())
                publish()
            }
            override fun onFailure(error: DJIError) = Unit
        })
    }

    private fun bindRemoteControllerCallbacks(product: Aircraft) {
        val remoteController = product.remoteController ?: return
        val generation = productSessionGeneration.current()
        remoteController.getChargeMobileMode(object : CommonCallbacks.CompletionCallbackWith<ChargeMobileMode> {
            override fun onSuccess(value: ChargeMobileMode) {
                if (!isCurrentProductSession(generation, product)) return
                snapshot = snapshot.copy(
                    rcPhoneChargingAvailable = true,
                    rcPhoneChargingMode = value.name,
                    updatedAtMs = System.currentTimeMillis(),
                )
                publish()
            }
            override fun onFailure(error: DJIError) {
                if (!isCurrentProductSession(generation, product)) return
                snapshot = snapshot.copy(
                    rcPhoneChargingAvailable = false,
                    rcPhoneChargingMode = "UNSUPPORTED",
                    updatedAtMs = System.currentTimeMillis(),
                )
                publish()
            }
        })
        remoteController.setChargeRemainingCallback { state ->
            if (!isCurrentProductSession(generation, product)) return@setChargeRemainingCallback
            val percent = state.remainingChargeInPercent
            snapshot = snapshot.copy(rcBattery = percent, updatedAtMs = System.currentTimeMillis())
            publish()
        }
        remoteController.setGPSDataCallback { data ->
            if (!isCurrentProductSession(generation, product)) return@setGPSDataCallback
            val location = data.location
            val course = FlightMapPresentation.courseDegrees(
                data.eastSpeed.toDouble(),
                data.northSpeed.toDouble(),
            )
            snapshot = snapshot.copy(
                rcLocationValid = data.isValid && location != null,
                rcLatitude = location?.latitude ?: Double.NaN,
                rcLongitude = location?.longitude ?: Double.NaN,
                rcCourseDegrees = course ?: Double.NaN,
                rcCourseValid = data.isValid && location != null && course != null,
                updatedAtMs = System.currentTimeMillis(),
            )
            publish()
        }
        remoteController.setHardwareStateCallback { state ->
            if (isCurrentProductSession(generation, product)) handleHardwareState(state)
        }
    }

    private fun isCurrentProductSession(generation: Long, product: Aircraft): Boolean =
        productSessionGeneration.accepts(generation) && aircraft === product

    fun setPhoneChargingEnabled(enabled: Boolean, listener: ActionResultListener) {
        val remoteController = aircraft?.remoteController
            ?: return listener.onResult(false, text(R.string.remote_controller_disconnected, "Remote controller disconnected"))
        val mode = if (enabled) ChargeMobileMode.ALWAYS else ChargeMobileMode.NEVER
        remoteController.setChargeMobileMode(mode) { error ->
            val ok = error == null
            val message = error?.description ?: if (enabled) {
                text(R.string.rc_will_charge_phone, "Remote controller will charge the phone")
            } else text(R.string.rc_phone_charging_disabled, "Remote-controller phone charging disabled")
            if (ok) {
                snapshot = snapshot.copy(
                    rcPhoneChargingAvailable = true,
                    rcPhoneChargingMode = mode.name,
                    updatedAtMs = System.currentTimeMillis(),
                )
                publish()
            }
            action(text(R.string.rc_phone_charging, "Remote-controller phone charging"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }

    private fun handleFlightState(state: FlightControllerState) {
        val now = System.currentTimeMillis()
        val location = state.aircraftLocation
        val home = state.homeLocation
        val attitude = state.attitude
        val assessment = state.goHomeAssessment
        val modeText = state.flightModeString?.takeIf { it.isNotBlank() } ?: state.flightMode?.name ?: "--"
        val altitudeTelemetry = AltitudeTelemetryResolver.resolve(
            relativeBarometricMeters = location?.altitude?.toDouble() ?: Double.NaN,
            takeoffAslMeters = state.takeoffLocationAltitude.toDouble(),
            downwardRangeUsed = state.isUltrasonicBeingUsed,
            downwardRangeHasError = state.doesUltrasonicHaveError(),
            downwardRangeMeters = state.ultrasonicHeightInMeters.toDouble(),
            downwardVisionActive = state.isVisionPositioningSensorBeingUsed,
        )
        snapshot = snapshot.copy(
            connected = aircraft?.isConnected == true,
            flying = state.isFlying,
            mode = modeText,
            satellites = state.satelliteCount,
            gpsLevel = state.gpsSignalLevel?.name ?: "--",
            latitude = location?.latitude ?: Double.NaN,
            longitude = location?.longitude ?: Double.NaN,
            homeLatitude = home?.latitude ?: Double.NaN,
            homeLongitude = home?.longitude ?: Double.NaN,
            homeLocationSet = state.isHomeLocationSet,
            imuPreheating = state.isIMUPreheating,
            altitude = altitudeTelemetry.relativeBarometricMeters,
            asl = altitudeTelemetry.estimatedAslMeters,
            groundClearance = altitudeTelemetry.groundClearanceMeters,
            groundClearanceSource = altitudeTelemetry.groundClearanceSource,
            groundClearanceReliableForSafety = altitudeTelemetry.groundClearanceReliableForSafety,
            downwardVisionActive = altitudeTelemetry.downwardVisionActive,
            heading = attitude?.yaw ?: 0.0,
            aircraftPitch = attitude?.pitch ?: 0.0,
            velocityNorth = state.velocityX.toDouble(),
            velocityEast = state.velocityY.toDouble(),
            horizontalSpeed = hypot(state.velocityX.toDouble(), state.velocityY.toDouble()),
            // DJI NED velocity Z is positive down; UI uses positive up.
            verticalSpeed = -state.velocityZ.toDouble(),
            flightTimeSeconds = state.flightTimeInSeconds,
            remainingFlightTimeSeconds = assessment?.remainingFlightTime ?: 0,
            timeNeededToGoHomeSeconds = assessment?.timeNeededToGoHome ?: 0,
            timeNeededToLandSeconds = assessment?.timeNeededToLandFromCurrentHeight ?: 0,
            batteryNeededToGoHomePercent = assessment?.batteryPercentageNeededToGoHome ?: -1,
            batteryNeededToLandPercent = assessment?.batteryPercentageNeededToLandFromCurrentHeight ?: -1,
            goingHome = state.isGoingHome,
            landing = modeText.contains("LAND", ignoreCase = true),
            landingConfirmationNeeded = state.isLandingConfirmationNeeded,
            lowBatteryWarning = state.isLowerThanBatteryWarningThreshold,
            seriousLowBatteryWarning = state.isLowerThanSeriousBatteryWarningThreshold,
            flightStateUpdatedAtMs = now,
            updatedAtMs = now,
        )
        logRcGoHomeFlightResponse(
            nowElapsedMs = SystemClock.elapsedRealtime(),
            mode = modeText,
            goingHome = state.isGoingHome,
            landing = snapshot.landing,
        )
        publish()
    }

    private fun handleHardwareState(state: HardwareState) {
        val left = state.leftStick
        val right = state.rightStick
        val positions = intArrayOf(
            left?.horizontalPosition ?: 0,
            left?.verticalPosition ?: 0,
            right?.horizontalPosition ?: 0,
            right?.verticalPosition ?: 0,
        )
        val active = positions.any { kotlin.math.abs(it) >= STICK_TAKEOVER_THRESHOLD }
        val bothSticksDown = positions[1] <= -CSC_STICK_THRESHOLD &&
            positions[3] <= -CSC_STICK_THRESHOLD
        val horizontalOpposed = positions[0] * positions[2] < 0 &&
            kotlin.math.abs(positions[0]) >= CSC_STICK_THRESHOLD &&
            kotlin.math.abs(positions[2]) >= CSC_STICK_THRESHOLD
        val cscCandidate = bothSticksDown && horizontalOpposed
        if (cscCandidate != lastCscCandidate) {
            Log.i(
                TAG,
                "RC CSC candidate=$cscCandidate sticks=" + positions.joinToString(","),
            )
            lastCscCandidate = cscCandidate
        }
        val goHomeButton = state.goHomeButton
        val goHomePresent = goHomeButton?.isPresent == true
        val goHomePressed = goHomeButton?.isClicked == true
        if (goHomePresent != lastRcGoHomeButtonPresent || goHomePressed != lastRcGoHomeButtonPressed) {
            val current = snapshot
            Log.i(
                TAG,
                "RC_RTH pressed=$goHomePressed present=$goHomePresent " +
                    "flying=${current.flying} goingHome=${current.goingHome} landing=${current.landing} " +
                    "mode=${current.mode} simulator=${current.simulatorActive} " +
                    "vs=${current.virtualStickEnabled} homeSet=${current.homeLocationSet} " +
                    "gps=${current.gpsLevel}/${current.satellites} " +
                    "homeDistance=${formatHomeDistance(current)}",
            )
            if (goHomePressed && lastRcGoHomeButtonPressed != true) {
                rcGoHomePressedAtElapsedMs = SystemClock.elapsedRealtime()
                rcGoHomeFallbackGeneration += 1L
                rcGoHomeFallbackIssued = false
                rcGoHomeObservationUntilElapsedMs = SystemClock.elapsedRealtime() + RC_GO_HOME_OBSERVATION_MS
                lastObservedGoingHome = null
                lastObservedLanding = null
                lastObservedFlightMode = null
            } else if (!goHomePressed && lastRcGoHomeButtonPressed == true) {
                val heldMs = SystemClock.elapsedRealtime() - rcGoHomePressedAtElapsedMs
                scheduleSimulatorRcGoHomeFallback(heldMs, rcGoHomeFallbackGeneration)
            }
            lastRcGoHomeButtonPresent = goHomePresent
            lastRcGoHomeButtonPressed = goHomePressed
        }
        snapshot = snapshot.copy(
            rcMode = state.flightModeSwitch?.name ?: "--",
            rcGoHomeButtonPresent = goHomePresent,
            rcGoHomeButtonPressed = goHomePressed,
            sticksActive = active,
            updatedAtMs = System.currentTimeMillis(),
        )
        publish()
        if (stickTakeoverLatch.update(positions, snapshot.virtualStickEnabled)) {
            main.post { listener.onManualTakeover() }
        }
    }

    private fun scheduleSimulatorRcGoHomeFallback(heldMs: Long, generation: Long) {
        if (heldMs < RC_GO_HOME_LONG_PRESS_MS) {
            Log.i(TAG, "RC_RTH fallback skipped shortPress=${heldMs}ms")
            return
        }
        main.postDelayed({
            if (generation != rcGoHomeFallbackGeneration || rcGoHomeFallbackIssued) return@postDelayed
            val current = snapshot
            if (!current.simulatorActive || !current.flying) {
                Log.i(
                    TAG,
                    "RC_RTH fallback skipped simulator=${current.simulatorActive} flying=${current.flying}",
                )
                return@postDelayed
            }
            if (current.goingHome || current.landing) {
                Log.i(
                    TAG,
                    "RC_RTH native response observed; fallback unnecessary goingHome=${current.goingHome} " +
                        "landing=${current.landing} mode=${current.mode}",
                )
                return@postDelayed
            }
            val controller = flightController
            if (controller == null) {
                Log.w(TAG, "RC_RTH fallback unavailable: flight controller disconnected")
                return@postDelayed
            }
            rcGoHomeFallbackIssued = true
            Log.w(
                TAG,
                "RC_RTH native Simulator response absent after long press ${heldMs}ms; " +
                    "forwarding DJI startGoHome",
            )
            controller.startGoHome { error ->
                val acceptedByState = snapshot.goingHome || snapshot.mode.contains("GO_HOME", ignoreCase = true)
                val ok = error == null || acceptedByState
                val message = when {
                    error == null -> text(R.string.dji_native_rth_accepted, "DJI native RTH command accepted")
                    acceptedByState -> text(R.string.sdk_error_but_rth_entered,
                        "SDK callback ${error.description}, but the flight controller entered RTH",
                        error.description)
                    else -> error.description
                }
                Log.i(
                    TAG,
                    "RC_RTH fallback result ok=$ok goingHome=${snapshot.goingHome} " +
                        "mode=${snapshot.mode} message=$message",
                )
                action(text(R.string.physical_rth_fallback, "Physical RTH fallback"), ok, message)
            }
        }, RC_GO_HOME_NATIVE_GRACE_MS)
    }

    private fun homeDistanceMeters(value: Snapshot): Double {
        if (
            !value.latitude.isFinite() ||
            !value.longitude.isFinite() ||
            !value.homeLatitude.isFinite() ||
            !value.homeLongitude.isFinite()
        ) return Double.NaN
        return FlightMapPresentation.distanceMeters(
            value.latitude,
            value.longitude,
            value.homeLatitude,
            value.homeLongitude,
        )
    }

    private fun formatHomeDistance(value: Snapshot): String {
        val distance = homeDistanceMeters(value)
        return if (distance.isFinite()) "${"%.1f".format(distance)}m" else "--"
    }

    private fun logRcGoHomeFlightResponse(
        nowElapsedMs: Long,
        mode: String,
        goingHome: Boolean,
        landing: Boolean,
    ) {
        if (nowElapsedMs > rcGoHomeObservationUntilElapsedMs) return
        if (
            goingHome == lastObservedGoingHome &&
            landing == lastObservedLanding &&
            mode == lastObservedFlightMode
        ) return
        Log.i(
            TAG,
            "RC_RTH_RESPONSE elapsed=${RC_GO_HOME_OBSERVATION_MS - (rcGoHomeObservationUntilElapsedMs - nowElapsedMs)}ms " +
                "goingHome=$goingHome landing=$landing mode=$mode flying=${snapshot.flying} " +
                "height=${"%.1f".format(snapshot.altitude)} simulator=${snapshot.simulatorActive} " +
                "motors=${snapshot.simulatorMotorsOn}",
        )
        lastObservedGoingHome = goingHome
        lastObservedLanding = landing
        lastObservedFlightMode = mode
    }

    fun takeOff() = complete(text(R.string.action_takeoff, "Takeoff")) {
        flightController?.startTakeoff(it) ?: unavailable(it)
    }
    fun takeOff(listener: ActionResultListener) {
        val fc = flightController ?: return listener.onResult(false, flightControllerDisconnected())
        fc.startTakeoff { error ->
            val ok = error == null
            val message = error?.description ?: commandAccepted()
            action(text(R.string.action_takeoff, "Takeoff"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }
    fun startGoHome() = complete(text(R.string.action_rth, "RTH")) {
        flightController?.startGoHome(it) ?: unavailable(it)
    }
    fun cancelGoHome() = complete(text(R.string.action_cancel_rth, "Cancel RTH")) {
        flightController?.cancelGoHome(it) ?: unavailable(it)
    }
    fun setGoHomeHeightMeters(heightMeters: Int, listener: ActionResultListener) {
        val controller = flightController
            ?: return listener.onResult(false, flightControllerDisconnected())
        controller.setGoHomeHeightInMeters(heightMeters) { error ->
            val ok = error == null
            val message = error?.description ?: text(R.string.rth_height_set_to,
                "Set to $heightMeters m", heightMeters)
            if (ok && flightController === controller) {
                snapshot = snapshot.copy(
                    goHomeHeightMeters = heightMeters,
                    updatedAtMs = System.currentTimeMillis(),
                )
                publish()
                refreshFlightSafetySettings()
            }
            action(text(R.string.rth_height, "RTH height"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }
    fun startLanding() = complete(text(R.string.action_landing, "Landing")) {
        flightController?.startLanding(it) ?: unavailable(it)
    }
    fun cancelLanding() = complete(text(R.string.action_cancel_landing, "Cancel landing")) {
        flightController?.cancelLanding(it) ?: unavailable(it)
    }
    fun confirmLanding() = complete(text(R.string.action_confirm_landing, "Confirm landing")) {
        flightController?.confirmLanding(it) ?: unavailable(it)
    }

    fun setSimulatorEnabled(enabled: Boolean) {
        setSimulatorEnabled(enabled, ActionResultListener { _, _ -> })
    }

    fun setSimulatorEnabled(enabled: Boolean, resultListener: ActionResultListener) {
        val simulator = flightController?.simulator
            ?: return reportSimulatorResult(false,
                text(R.string.flight_controller_or_simulator_unavailable,
                    "Flight controller disconnected or simulator unsupported"), resultListener)
        if (enabled) {
            val latitude = snapshot.latitude.takeIf { it.isFinite() } ?: 31.2304
            val longitude = snapshot.longitude.takeIf { it.isFinite() } ?: 121.4737
            simulatorOriginLatitude = latitude
            simulatorOriginLongitude = longitude
            simulatorSampleSequence = 0L
            simulatorRateWindowStartedNanos = 0L
            simulatorRateWindowSamples = 0L
            measuredSimulatorStateHz = 0.0
            val data = InitializationData.createInstance(
                LocationCoordinate2D(latitude, longitude), simulatorUpdateFrequencyHz, 18,
            )
            Log.i(
                TAG,
                "Simulator init lat=${"%.6f".format(latitude)} lon=${"%.6f".format(longitude)} " +
                    "hz=$simulatorUpdateFrequencyHz satellites=18",
            )
            simulator.start(data) { error ->
                val active = simulator.isSimulatorActive
                val ok = error == null || active
                val message = when {
                    error == null -> text(R.string.started, "Started")
                    active -> text(R.string.sdk_error_but_simulator_active,
                        "SDK returned ${error.description}, but Simulator is active", error.description)
                    else -> error.description
                }
                snapshot = snapshot.copy(
                    simulatorAvailable = true,
                    simulatorActive = active,
                    updatedAtMs = System.currentTimeMillis(),
                )
                publish()
                reportSimulatorResult(ok, message, resultListener)
            }
        } else {
            simulator.stop { error ->
                val active = simulator.isSimulatorActive
                val ok = !active
                val message = when {
                    !active && error == null -> text(R.string.stopped, "Stopped")
                    !active -> text(R.string.sdk_error_but_simulator_stopped,
                        "SDK returned ${error?.description ?: "unknown error"}, but Simulator stopped",
                        error?.description ?: text(R.string.unknown_error, "unknown error"))
                    else -> error?.description ?: text(R.string.simulator_still_active_after_stop,
                        "Simulator remains active after the stop callback")
                }
                snapshot = if (active) {
                    snapshot.copy(
                        simulatorAvailable = true,
                        simulatorActive = true,
                        updatedAtMs = System.currentTimeMillis(),
                    )
                } else {
                    snapshot.copy(
                        simulatorAvailable = true,
                        simulatorActive = false,
                        simulatorMotorsOn = false,
                        simulatorFlying = false,
                        simulatorX = 0.0,
                        simulatorY = 0.0,
                        simulatorZ = 0.0,
                        simulatorRoll = 0.0,
                        simulatorPitch = 0.0,
                        simulatorYaw = 0.0,
                        simulatorStateHz = 0.0,
                        simulatorStateSequence = 0L,
                        updatedAtMs = System.currentTimeMillis(),
                    )
                }
                publish()
                reportSimulatorResult(ok, message, resultListener)
            }
        }
    }

    private fun reportSimulatorResult(
        ok: Boolean,
        message: String,
        resultListener: ActionResultListener,
    ) {
        action(text(R.string.dji_builtin_simulator, "DJI built-in simulator"), ok, message)
        main.post { resultListener.onResult(ok, message) }
    }

    private fun describeDjiError(error: DJIError?, successMessage: String): String =
        error?.let { "${it.description} code=${it.errorCode} type=${it.javaClass.simpleName}" }
            ?: successMessage

    fun takePhoto() = takePhoto(null, null)

    fun takePhoto(listener: PhotoResultListener?) = takePhoto(null, listener)

    fun takePhoto(triggeredListener: PhotoTriggeredListener?, listener: PhotoResultListener?) {
        val target = camera
        if (target == null) {
            action(text(R.string.action_photo, "Photo"), false, cameraDisconnected())
            if (listener != null) main.post { listener.onResult(false, cameraDisconnected()) }
            return
        }
        val shoot = CommonCallbacks.CompletionCallback<DJIError> { modeError ->
            if (modeError != null) {
                action(text(R.string.action_photo, "Photo"), false, modeError.description)
                if (listener != null) main.post { listener.onResult(false, modeError.description) }
            } else main.post {
                if (camera !== target || !target.isConnected) {
                    val message = cameraDisconnected()
                    action(text(R.string.action_photo, "Photo"), false, message)
                    listener?.onResult(false, message)
                    return@post
                }
                triggeredListener?.onTriggered(
                    SystemClock.elapsedRealtimeNanos(),
                    System.currentTimeMillis(),
                )
                target.startShootPhoto {
                    val ok = it == null
                    val message = it?.description ?: text(R.string.triggered, "Triggered")
                    action(text(R.string.action_photo, "Photo"), ok, message)
                    if (listener != null) main.post { listener.onResult(ok, message) }
                }
            }
        }
        if (target.isFlatCameraModeSupported) {
            target.setFlatMode(FlatCameraMode.PHOTO_SINGLE, shoot)
        } else {
            target.setMode(CameraMode.SHOOT_PHOTO, shoot)
        }
    }

    fun prepareSurveyPhotoMode() {
        val target = camera
        if (target == null) {
            action(text(R.string.survey_camera, "Survey camera"), false, cameraDisconnected())
        } else if (target.isFlatCameraModeSupported) {
            target.setFlatMode(FlatCameraMode.PHOTO_SINGLE) {
                action(text(R.string.survey_camera, "Survey camera"), it == null,
                    it?.description ?: text(R.string.single_photo_mode_ready, "Single-photo mode ready"))
            }
        } else {
            target.setMode(CameraMode.SHOOT_PHOTO) {
                action(text(R.string.survey_camera, "Survey camera"), it == null,
                    it?.description ?: text(R.string.photo_mode_ready, "Photo mode ready"))
            }
        }
    }

    @JvmOverloads
    fun setSurveyGimbalPitch(
        gimbalPitchDegrees: Double,
        listener: ActionResultListener? = null,
    ) {
        val gimbal = aircraft?.gimbals?.firstOrNull()
            ?: return text(R.string.gimbal_disconnected, "Gimbal disconnected").let { message ->
                action(text(R.string.survey_gimbal, "Survey gimbal"), false, message)
                listener?.let { main.post { it.onResult(false, message) } }
            }
        val rotation = Rotation.Builder()
            .mode(RotationMode.ABSOLUTE_ANGLE)
            .pitch(gimbalPitchDegrees.coerceIn(-90.0, 30.0).toFloat())
            .time(1.0)
            .ignore(true)
            .build()
        gimbal.rotate(rotation) {
            val ok = it == null
            val message = it?.description ?: text(
                R.string.gimbal_pitch_set_to,
                "Pitch set to ${gimbalPitchDegrees.toInt()}°",
                gimbalPitchDegrees.toInt(),
            )
            action(text(R.string.survey_gimbal, "Survey gimbal"), ok, message)
            listener?.let { callback -> main.post { callback.onResult(ok, message) } }
        }
    }

    fun prepareSurveyCamera(gimbalPitchDegrees: Double) {
        prepareSurveyPhotoMode()
        setSurveyGimbalPitch(gimbalPitchDegrees)
    }

    fun toggleRecording() {
        val target = camera ?: return action(text(R.string.action_video, "Video"), false, cameraDisconnected())
        if (snapshot.recording) {
            target.stopRecordVideo { action(text(R.string.stop_recording, "Stop recording"), it == null,
                it?.description ?: text(R.string.stopped, "Stopped")) }
        } else {
            val record = { modeError: DJIError? ->
                if (modeError != null) action(text(R.string.action_video, "Video"), false, modeError.description)
                else target.startRecordVideo { action(text(R.string.start_recording, "Start recording"),
                    it == null, it?.description ?: text(R.string.started, "Started")) }
            }
            if (target.isFlatCameraModeSupported) {
                target.setFlatMode(FlatCameraMode.VIDEO_NORMAL, record)
            } else {
                target.setMode(CameraMode.RECORD_VIDEO, record)
            }
        }
    }

    fun exitMediaMode() {
        val target = camera ?: return
        val generation = mediaSessionGeneration.next()
        mediaSessionActive = false
        val restoreMode = mediaPreviousMode
        val restoreFlatMode = mediaPreviousFlatMode
        mediaPreviousMode = null
        mediaPreviousFlatMode = null
        target.mediaManager?.exitMediaDownloading()
        if (target.isFlatCameraModeSupported) {
            target.exitPlayback { error ->
                if (!mediaSessionGeneration.accepts(generation) || camera !== target) return@exitPlayback
                if (error != null) {
                    action(text(R.string.exit_gallery, "Exit gallery"), false, error.description)
                } else if (restoreFlatMode != null) {
                    target.setFlatMode(restoreFlatMode) { restoreError ->
                        if (!mediaSessionGeneration.accepts(generation) || camera !== target) return@setFlatMode
                        action(text(R.string.exit_gallery, "Exit gallery"), restoreError == null,
                            restoreError?.description ?: text(R.string.restored_mode,
                                "Restored ${restoreFlatMode.name}", restoreFlatMode.name))
                    }
                } else {
                    action(text(R.string.exit_gallery, "Exit gallery"), true,
                        text(R.string.playback_mode_exited, "Playback mode exited"))
                }
            }
        } else {
            target.setMode(restoreMode ?: CameraMode.SHOOT_PHOTO) { error ->
                if (!mediaSessionGeneration.accepts(generation) || camera !== target) return@setMode
                val name = (restoreMode ?: CameraMode.SHOOT_PHOTO).name
                action(text(R.string.exit_gallery, "Exit gallery"), error == null,
                    error?.description ?: text(R.string.restored_mode, "Restored $name", name))
            }
        }
    }

    fun fetchMediaFiles(listener: MediaFilesListener) {
        val target = camera ?: return listener.onMediaFiles(emptyList(), cameraDisconnected())
        val manager = target.mediaManager
            ?: return listener.onMediaFiles(emptyList(),
                text(R.string.camera_media_manager_unavailable, "This camera has no media manager"))
        val generation = if (mediaSessionActive) mediaSessionGeneration.current() else mediaSessionGeneration.next()
        val firstEntry = !mediaSessionActive
        mediaSessionActive = true

        fun current(): Boolean = mediaSessionGeneration.accepts(generation) && camera === target
        fun resolveStorageAndRefresh() {
            target.getStorageLocation(object : CommonCallbacks.CompletionCallbackWith<StorageLocation> {
                override fun onSuccess(value: StorageLocation) {
                    if (!current()) return
                    refreshMediaStorage(target, manager, generation,
                        if (value == StorageLocation.UNKNOWN) StorageLocation.SDCARD else value,
                        listener)
                }

                override fun onFailure(error: DJIError) {
                    if (!current()) return
                    Log.w(TAG, "Unable to read selected camera storage; falling back to SD card: ${error.description}")
                    refreshMediaStorage(target, manager, generation, StorageLocation.SDCARD, listener)
                }
            })
        }

        fun enterMediaMode() {
            if (target.isFlatCameraModeSupported) {
                // Mini 2 等 Flat 机型：回放模式进入媒体下载（固件拒绝 setMode 切换）。
                target.enterPlayback { modeError ->
                    if (!current()) return@enterPlayback
                    if (modeError != null) {
                        listener.onMediaFiles(emptyList(), text(R.string.enter_playback_failed,
                            "Failed to enter playback mode: ${modeError.description}", modeError.description))
                    } else {
                        resolveStorageAndRefresh()
                    }
                }
            } else {
                target.setMode(CameraMode.MEDIA_DOWNLOAD) { modeError ->
                    if (!current()) return@setMode
                    if (modeError != null) {
                        listener.onMediaFiles(emptyList(), text(R.string.enter_media_mode_failed,
                            "Failed to enter media mode: ${modeError.description}", modeError.description))
                    } else {
                        resolveStorageAndRefresh()
                    }
                }
            }
        }

        if (!firstEntry) {
            resolveStorageAndRefresh()
        } else if (target.isFlatCameraModeSupported) {
            target.getFlatMode(object : CommonCallbacks.CompletionCallbackWith<FlatCameraMode> {
                override fun onSuccess(value: FlatCameraMode) {
                    if (!current()) return
                    mediaPreviousFlatMode = value
                    enterMediaMode()
                }
                override fun onFailure(error: DJIError) {
                    if (current()) enterMediaMode()
                }
            })
        } else {
            target.getMode(object : CommonCallbacks.CompletionCallbackWith<CameraMode> {
                override fun onSuccess(value: CameraMode) {
                    if (!current()) return
                    mediaPreviousMode = value
                    enterMediaMode()
                }
                override fun onFailure(error: DJIError) {
                    if (current()) enterMediaMode()
                }
            })
        }
    }

    private fun refreshMediaStorage(
        target: Camera,
        manager: dji.sdk.media.MediaManager,
        generation: Long,
        storage: StorageLocation,
        listener: MediaFilesListener,
    ) {
        manager.refreshFileListOfStorageLocation(storage) { error ->
            if (!mediaSessionGeneration.accepts(generation) || camera !== target) return@refreshFileListOfStorageLocation
            if (error != null) {
                listener.onMediaFiles(emptyList(), text(R.string.refresh_media_list_failed,
                    "Failed to refresh ${storage.name} media list: ${error.description}",
                    storage.name, error.description))
            } else {
                val files = when (storage) {
                    StorageLocation.INTERNAL_STORAGE -> manager.internalStorageFileListSnapshot
                    else -> manager.sdCardFileListSnapshot
                }.orEmpty().sortedByDescending { it.timeCreated }
                listener.onMediaFiles(files, null)
            }
        }
    }

    fun fetchMediaThumbnail(file: MediaFile, listener: MediaThumbnailListener) {
        file.fetchThumbnail { error ->
            if (error == null && file.thumbnail != null) {
                listener.onThumbnail(file.thumbnail)
            } else {
                action(text(R.string.thumbnail, "Thumbnail"), false,
                    "thumbnail: ${error?.description ?: "bitmap null"}")
                // 部分固件缩略图字段缺失，退回预览图。
                file.fetchPreview { previewError ->
                    if (previewError != null) {
                        action(text(R.string.thumbnail, "Thumbnail"), false,
                            "preview: ${previewError.description}")
                    }
                    listener.onThumbnail(if (previewError == null) file.preview else null)
                }
            }
        }
    }

    fun enableVirtualStick() {
        enableVirtualStickInternal(null)
    }

    fun enableVirtualStickForManual(onReady: Runnable) {
        enableVirtualStickInternal(onReady)
    }

    fun enableSimulatorRegressionVirtualStick(listener: ActionResultListener) {
        val block = simulatorRegressionBlockReason()
        if (block != null) return listener.onResult(false, block)
        val fc = flightController ?: return listener.onResult(false, flightControllerDisconnected())
        fc.rollPitchControlMode = RollPitchControlMode.VELOCITY
        fc.verticalControlMode = VerticalControlMode.VELOCITY
        fc.yawControlMode = YawControlMode.ANGULAR_VELOCITY
        fc.rollPitchCoordinateSystem = FlightCoordinateSystem.BODY
        val generation = virtualStickTransitionGeneration.next()
        fc.setVirtualStickModeEnabled(true) { error ->
            if (!isCurrentVirtualStickTransition(generation, fc)) return@setVirtualStickModeEnabled
            val ok = error == null
            snapshot = snapshot.copy(virtualStickEnabled = ok)
            publish()
            val message = error?.description ?: text(R.string.enabled, "Enabled")
            action(text(R.string.simulator_regression_virtual_stick,
                "Simulator regression Virtual Stick"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }

    fun requestSimulatorRegressionTakeoff(listener: ActionResultListener) {
        val block = simulatorRegressionBlockReason()
        if (block != null) return listener.onResult(false, block)
        val fc = flightController ?: return listener.onResult(false, flightControllerDisconnected())
        fc.startTakeoff { error ->
            val ok = error == null
            val message = describeDjiError(error, commandAccepted())
            action(text(R.string.simulator_regression_takeoff, "Simulator regression takeoff"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }

    fun requestSimulatorRegressionMotorsOn(listener: ActionResultListener) {
        val block = simulatorRegressionBlockReason()
        if (block != null) return listener.onResult(false, block)
        val fc = flightController ?: return listener.onResult(false, flightControllerDisconnected())
        fc.turnOnMotors { error ->
            val acceptedByState = snapshot.simulatorMotorsOn
            val ok = error == null || acceptedByState
            val message = when {
                error == null -> text(R.string.motor_start_command_accepted, "Motor-start command accepted")
                acceptedByState -> text(R.string.sdk_error_but_motors_started,
                    "SDK callback ${describeDjiError(error, "")}, but motors started",
                    describeDjiError(error, ""))
                else -> describeDjiError(error,
                    text(R.string.motor_start_command_accepted, "Motor-start command accepted"))
            }
            action(text(R.string.simulator_regression_start_motors,
                "Simulator regression start motors"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }

    fun requestSimulatorRegressionPrecisionTakeoff(listener: ActionResultListener) {
        val block = simulatorRegressionBlockReason()
        if (block != null) return listener.onResult(false, block)
        val fc = flightController ?: return listener.onResult(false, flightControllerDisconnected())
        fc.startPrecisionTakeoff { error ->
            val ok = error == null
            val message = describeDjiError(error,
                text(R.string.precision_takeoff_command_accepted, "Precision-takeoff command accepted"))
            action(text(R.string.simulator_regression_precision_takeoff,
                "Simulator regression precision takeoff"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }

    fun requestSimulatorRegressionLanding(listener: ActionResultListener) {
        val block = simulatorRegressionBlockReason()
        if (block != null) return listener.onResult(false, block)
        val fc = flightController ?: return listener.onResult(false, flightControllerDisconnected())
        fc.startLanding { error ->
            val ok = error == null
            val message = describeDjiError(error,
                text(R.string.landing_command_accepted, "Landing command accepted"))
            action(text(R.string.simulator_regression_landing,
                "Simulator regression landing"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }

    fun requestSimulatorRegressionLandingConfirmation(listener: ActionResultListener) {
        val block = simulatorRegressionBlockReason()
        if (block != null) return listener.onResult(false, block)
        val fc = flightController ?: return listener.onResult(false, flightControllerDisconnected())
        fc.confirmLanding { error ->
            val ok = error == null
            val message = describeDjiError(error,
                text(R.string.confirm_landing_command_accepted, "Confirm-landing command accepted"))
            action(text(R.string.simulator_regression_confirm_landing,
                "Simulator regression confirm landing"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }

    fun sendSimulatorRegressionVelocity(
        forward: Float,
        right: Float,
        up: Float,
        yawRate: Float,
    ): Boolean {
        val block = simulatorRegressionBlockReason()
        if (block != null) {
            Log.w(TAG, "Simulator regression command blocked: $block")
            return false
        }
        val fc = flightController ?: return false
        if (!snapshot.virtualStickEnabled) return false
        lastCommandForward = forward.toDouble()
        lastCommandRight = right.toDouble()
        lastCommandUp = up.toDouble()
        lastCommandYawRate = yawRate.toDouble()
        refreshCommandLease(forward, right, up, yawRate)
        val axes = DjiVirtualStickAxes.fromFru(forward, right, up, yawRate)
        fc.sendVirtualStickFlightControlData(FlightControlData(
            axes.pitch, axes.roll, axes.yaw, axes.verticalThrottle,
        )) { error ->
            if (error != null) Log.w(TAG, "Simulator regression VS send failed: ${error.description}")
        }
        return true
    }

    fun sendSimulatorRegressionSticks(
        leftHorizontal: Int,
        leftVertical: Int,
        rightHorizontal: Int,
        rightVertical: Int,
    ): Boolean {
        val command = RcVirtualStickMapper.map(
            leftHorizontal, leftVertical, rightHorizontal, rightVertical,
        )
        return sendSimulatorRegressionVelocity(
            command.forwardMetersPerSecond,
            command.rightMetersPerSecond,
            command.upMetersPerSecond,
            command.yawRateDegreesPerSecond,
        )
    }

    private fun simulatorRegressionBlockReason(): String? {
        val controller = flightController ?: return flightControllerDisconnected()
        val simulator = controller.simulator
            ?: return text(R.string.simulator_unavailable, "Simulator unavailable")
        if (!simulator.isSimulatorActive || !snapshot.simulatorActive) {
            return text(R.string.simulator_not_active, "Simulator is not active")
        }
        val sample = latestSimulatorSample
            ?: return text(R.string.raw_simulator_state_missing, "No raw SimulatorState")
        val moving = sample.motorsOn || sample.flying
        if (!edu.playground.djivln.hil.HilPoseFreshnessPolicy.isSampleUsable(
                sample.elapsedRealtimeNanos, SystemClock.elapsedRealtimeNanos(), moving,
            )
        ) {
            return text(R.string.simulator_state_older_than_500ms, "SimulatorState is older than 500 ms")
        }
        return null
    }

    private fun enableVirtualStickInternal(onReady: Runnable?) {
        val fc = flightController ?: return action("Virtual Stick", false, flightControllerDisconnected())
        fc.rollPitchControlMode = RollPitchControlMode.VELOCITY
        fc.verticalControlMode = VerticalControlMode.VELOCITY
        fc.yawControlMode = YawControlMode.ANGULAR_VELOCITY
        fc.rollPitchCoordinateSystem = FlightCoordinateSystem.BODY
        val generation = virtualStickTransitionGeneration.next()
        fc.setVirtualStickModeEnabled(true) { error ->
            if (!isCurrentVirtualStickTransition(generation, fc)) return@setVirtualStickModeEnabled
            snapshot = snapshot.copy(virtualStickEnabled = error == null)
            publish()
            action("Virtual Stick", error == null, error?.description ?: text(R.string.enabled, "Enabled"))
            if (error == null && onReady != null) main.post(onReady)
        }
    }

    fun disableVirtualStick(reason: String = text(R.string.reason_user_stopped, "User stopped")) {
        disableVirtualStickInternal(reason, null)
    }

    fun disableVirtualStickThen(reason: String, onDisabled: Runnable) {
        disableVirtualStickInternal(reason, onDisabled)
    }

    fun disableSimulatorRegressionVirtualStick(listener: ActionResultListener) {
        sendBodyVelocity(0f, 0f, 0f, 0f)
        val fc = flightController ?: return listener.onResult(false, flightControllerDisconnected())
        val generation = virtualStickTransitionGeneration.next()
        snapshot = snapshot.copy(virtualStickEnabled = false)
        publish()
        fc.setVirtualStickModeEnabled(false) { error ->
            if (!isCurrentVirtualStickTransition(generation, fc)) return@setVirtualStickModeEnabled
            val ok = error == null
            val message = error?.description ?: text(R.string.released_hil_regression_complete,
                "Released: HIL automatic regression complete")
            snapshot = snapshot.copy(virtualStickEnabled = false)
            publish()
            action(text(R.string.simulator_regression_virtual_stick,
                "Simulator regression Virtual Stick"), ok, message)
            main.post { listener.onResult(ok, message) }
        }
    }

    private fun disableVirtualStickInternal(reason: String, onDisabled: Runnable?) {
        sendBodyVelocity(0f, 0f, 0f, 0f)
        val fc = flightController ?: run {
            virtualStickTransitionGeneration.next()
            snapshot = snapshot.copy(virtualStickEnabled = false)
            publish()
            action("Virtual Stick", false, flightControllerDisconnected())
            return
        }
        val generation = virtualStickTransitionGeneration.next()
        snapshot = snapshot.copy(virtualStickEnabled = false)
        publish()
        fc.setVirtualStickModeEnabled(false) { error ->
            if (!isCurrentVirtualStickTransition(generation, fc)) return@setVirtualStickModeEnabled
            action("Virtual Stick", error == null, error?.description
                ?: text(R.string.released_with_reason, "Released: $reason", reason))
            if (error == null && onDisabled != null) main.post(onDisabled)
        }
    }

    fun sendBodyVelocity(forward: Float, right: Float, up: Float, yawRate: Float) {
        lastCommandForward = forward.toDouble()
        lastCommandRight = right.toDouble()
        lastCommandUp = up.toDouble()
        lastCommandYawRate = yawRate.toDouble()
        refreshCommandLease(forward, right, up, yawRate)
        val fc = flightController ?: return
        if (!snapshot.virtualStickEnabled) return
        val generation = virtualStickTransitionGeneration.current()
        val axes = DjiVirtualStickAxes.fromFru(forward, right, up, yawRate)
        fc.sendVirtualStickFlightControlData(FlightControlData(
            axes.pitch, axes.roll, axes.yaw, axes.verticalThrottle,
        )) { error ->
            if (error != null) main.post {
                if (isCurrentVirtualStickTransition(generation, fc) && snapshot.virtualStickEnabled) {
                    listener.onAction(text(R.string.control_send, "Control send"), false, error.description)
                }
            }
        }
    }

    private fun isCurrentVirtualStickTransition(generation: Long, controller: FlightController): Boolean =
        virtualStickTransitionGeneration.accepts(generation) && flightController === controller

    private fun refreshCommandLease(forward: Float, right: Float, up: Float, yawRate: Float) {
        commandLeaseGeneration.next()
        val nonZero = forward != 0f || right != 0f || up != 0f || yawRate != 0f
        commandLeaseRefreshedAtMillis = if (nonZero) SystemClock.elapsedRealtime() else 0L
        main.removeCallbacks(commandLeaseRunnable)
        if (nonZero) main.postDelayed(
            commandLeaseRunnable,
            VirtualStickCommandLeasePolicy.MAXIMUM_AGE_MILLIS,
        )
    }

    private fun expireCommandLeaseIfNeeded() {
        val refreshedAt = commandLeaseRefreshedAtMillis
        val nonZero = lastCommandForward != 0.0 || lastCommandRight != 0.0 ||
            lastCommandUp != 0.0 || lastCommandYawRate != 0.0
        val now = SystemClock.elapsedRealtime()
        if (!VirtualStickCommandLeasePolicy.shouldExpire(nonZero, refreshedAt, now)) {
            if (nonZero && refreshedAt > 0L) {
                main.postDelayed(commandLeaseRunnable, maxOf(1L,
                    VirtualStickCommandLeasePolicy.MAXIMUM_AGE_MILLIS - (now - refreshedAt)))
            }
            return
        }
        commandLeaseRefreshedAtMillis = 0L
        lastCommandForward = 0.0
        lastCommandRight = 0.0
        lastCommandUp = 0.0
        lastCommandYawRate = 0.0
        val fc = flightController ?: return
        if (!snapshot.virtualStickEnabled) return
        val generation = virtualStickTransitionGeneration.current()
        val expiredLeaseGeneration = commandLeaseGeneration.current()
        val zero = DjiVirtualStickAxes.fromFru(0f, 0f, 0f, 0f)
        fc.sendVirtualStickFlightControlData(FlightControlData(
            zero.pitch, zero.roll, zero.yaw, zero.verticalThrottle,
        )) { error ->
            main.post {
                if (isCurrentVirtualStickTransition(generation, fc) &&
                    commandLeaseGeneration.accepts(expiredLeaseGeneration)) {
                    listener.onAction(
                        text(R.string.virtual_stick_command_lease_expired,
                            "Virtual Stick command lease expired; zero sent"),
                        error == null,
                        error?.description ?: text(R.string.zero_command_sent, "Zero command sent"),
                    )
                }
            }
        }
    }

    fun currentSnapshot(): Snapshot = snapshot

    fun currentSimulatorSample(): SimulatorSample? = latestSimulatorSample

    fun isSimulatorRegressionReady(): Boolean = simulatorRegressionBlockReason() == null

    fun isSimulatorNavigationReady(): Boolean =
        snapshot.satellites >= 6 &&
            snapshot.latitude.isFinite() && snapshot.longitude.isFinite() &&
            snapshot.homeLatitude.isFinite() && snapshot.homeLongitude.isFinite() &&
            snapshot.homeLocationSet && !snapshot.imuPreheating &&
            flightController?.compass?.hasError() != true &&
            flightController?.compass?.isCalibrating != true &&
            snapshot.mode.contains("GPS", ignoreCase = true) &&
            snapshot.gpsLevel !in setOf("--", "NONE", "LEVEL_0", "LEVEL_1")

    fun isSimulatorTakeoffTransitionComplete(): Boolean =
        snapshot.simulatorFlying &&
            !snapshot.mode.contains("TAKEOFF", ignoreCase = true)

    fun simulatorRegressionDiagnostics(): String =
        "connected=${snapshot.connected} mode=${snapshot.mode} gps=${snapshot.gpsLevel} " +
            "satellites=${snapshot.satellites} location=${snapshot.latitude},${snapshot.longitude} " +
            "home=${snapshot.homeLatitude},${snapshot.homeLongitude} homeSet=${snapshot.homeLocationSet} " +
            "imuPreheating=${snapshot.imuPreheating} compassError=${flightController?.compass?.hasError()} " +
            "compassCalibrating=${flightController?.compass?.isCalibrating} " +
            "battery=${snapshot.aircraftBattery}% lowBattery=${snapshot.lowBatteryWarning} " +
            "seriousLowBattery=${snapshot.seriousLowBatteryWarning} " +
            "simulator=${snapshot.simulatorActive} motors=${snapshot.simulatorMotorsOn} " +
            "flying=${snapshot.simulatorFlying} rawSeq=${snapshot.simulatorStateSequence}"

    fun setSimulatorUpdateFrequencyHz(value: Int) {
        require(value in 2..150) { "Simulator state frequency must be within 2..150 Hz" }
        simulatorUpdateFrequencyHz = value
        action(
            text(R.string.simulator_state_frequency, "Simulator state frequency"),
            true,
            if (snapshot.simulatorActive) text(R.string.frequency_set_restart_required,
                "Set to $value Hz; takes effect after restarting Simulator", value)
            else text(R.string.frequency_set, "Set to $value Hz", value),
        )
    }

    fun simulatorUpdateFrequencyHz(): Int = simulatorUpdateFrequencyHz

    private fun complete(label: String, call: (dji.common.util.CommonCallbacks.CompletionCallback<DJIError>) -> Unit) {
        call { error -> action(label, error == null, error?.description ?: commandAccepted()) }
    }

    private fun unavailable(callback: dji.common.util.CommonCallbacks.CompletionCallback<DJIError>) {
        action(text(R.string.flight_command, "Flight command"), false, flightControllerDisconnected())
    }

    private fun flightControllerDisconnected(): String =
        text(R.string.flight_controller_disconnected, "Flight controller disconnected")

    private fun cameraDisconnected(): String =
        text(R.string.camera_disconnected, "Camera disconnected")

    private fun commandAccepted(): String =
        text(R.string.command_accepted, "Command accepted")

    private fun text(resourceId: Int, fallback: String, vararg arguments: Any): String =
        appContext?.getString(resourceId, *arguments) ?: fallback

    private fun action(label: String, ok: Boolean, message: String) {
        main.post { listener.onAction(label, ok, message) }
    }

    private fun publish() {
        val value = snapshot
        main.post { listener.onSnapshot(value) }
    }

    private fun unbindCallbacks() {
        mediaSessionGeneration.next()
        mediaSessionActive = false
        mediaPreviousMode = null
        mediaPreviousFlatMode = null
        val previousController = flightController
        if (snapshot.virtualStickEnabled && previousController != null) {
            val zero = DjiVirtualStickAxes.fromFru(0f, 0f, 0f, 0f)
            runCatching {
                previousController.sendVirtualStickFlightControlData(FlightControlData(
                    zero.pitch, zero.roll, zero.yaw, zero.verticalThrottle,
                ), null)
            }
        }
        virtualStickTransitionGeneration.next()
        lastCommandForward = 0.0
        lastCommandRight = 0.0
        lastCommandUp = 0.0
        lastCommandYawRate = 0.0
        commandLeaseRefreshedAtMillis = 0L
        commandLeaseGeneration.next()
        main.removeCallbacks(commandLeaseRunnable)
        snapshot = snapshot.copy(virtualStickEnabled = false)
        productSessionGeneration.next()
        runCatching { flightController?.setStateCallback(null) }
        runCatching { flightController?.simulator?.setStateCallback(null) }
        runCatching { aircraft?.battery?.setStateCallback(null) }
        runCatching { aircraft?.airLink?.setUplinkSignalQualityCallback(null) }
        runCatching { aircraft?.remoteController?.setChargeRemainingCallback(null) }
        runCatching { aircraft?.remoteController?.setGPSDataCallback(null) }
        runCatching { aircraft?.remoteController?.setHardwareStateCallback(null) }
        runCatching { aircraft?.gimbals?.firstOrNull()?.setStateCallback(null) }
        runCatching { camera?.setSystemStateCallback(null) }
    }

    /** Invalidates every callback owned by the current DJI product session. */
    fun invalidateCurrentProduct() {
        unbindCallbacks()
        aircraft = null
        flightController = null
        camera = null
        latestSimulatorSample = null
        measuredSimulatorStateHz = 0.0
        snapshot = Snapshot(connected = false, product = text(R.string.unrecognized, "Unrecognized"))
        publish()
    }

    override fun close() {
        invalidateCurrentProduct()
    }

    private companion object {
        const val TAG = "DjiVln"
        const val STICK_TAKEOVER_THRESHOLD = 50
        const val CSC_STICK_THRESHOLD = 300
        const val DEFAULT_SIMULATOR_STATE_HZ = 100
        const val SIMULATOR_UI_INTERVAL_NANOS = 20_000_000L
        const val RC_GO_HOME_OBSERVATION_MS = 10_000L
        const val RC_GO_HOME_LONG_PRESS_MS = 900L
        const val RC_GO_HOME_NATIVE_GRACE_MS = 500L
    }
}
