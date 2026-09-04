package edu.playground.djivln.survey

import android.content.Context
import edu.playground.djivln.mini2.R

object SurveyParameterPolicy {
    @JvmOverloads
    fun createConstraints(
        altitudeMetersAgl: Double,
        routeHeadingDegrees: Double,
        forwardOverlapPercent: Double,
        sideOverlapPercent: Double,
        speedMetersPerSecond: Double,
        gimbalPitchDegrees: Double,
        boundaryMarginMeters: Double,
        obliqueFiveDirection: Boolean,
        targetSurfaceToTakeoffMeters: Double = 0.0,
        safeTakeoffAltitudeMeters: Double = 30.0,
        takeoffSpeedMetersPerSecond: Double = 3.0,
        obliqueForwardOverlapPercent: Double = 70.0,
        obliqueSideOverlapPercent: Double = 60.0,
        altitudeMode: SurveyAltitudeMode = SurveyAltitudeMode.ABOVE_TARGET_SURFACE,
        startPointMode: SurveyStartPointMode = SurveyStartPointMode.AUTO_NEAREST,
        completionAction: SurveyCompletionAction = SurveyCompletionAction.RETURN_TO_HOME,
        captureTriggerMode: SurveyCaptureTriggerMode = SurveyCaptureTriggerMode.DISTANCE,
        timedCaptureIntervalSeconds: Double = 1.0,
        takeoffMode: SurveyTakeoffMode = SurveyTakeoffMode.MANUAL,
        enabledCaptureViews: Set<SurveyCaptureView> = SurveyCaptureView.values().toSet(),
        obliqueHeadingMode: SurveyObliqueHeadingMode = SurveyObliqueHeadingMode.TRACK_ROUTE,
        obliqueSpeedMetersPerSecond: Double = speedMetersPerSecond,
        context: Context? = null,
    ): SurveyConstraints {
        fun message(resourceId: Int, fallback: String) = context?.getString(resourceId) ?: fallback
        require(altitudeMetersAgl in 10.0..120.0) {
            message(R.string.survey_altitude_range_error, "Altitude must be 10–120 m")
        }
        require(routeHeadingDegrees.isFinite()) {
            message(R.string.survey_heading_number_error, "Heading must be a valid number")
        }
        require(forwardOverlapPercent in 50.0..90.0) {
            message(R.string.survey_forward_overlap_range_error, "Forward overlap must be 50–90%")
        }
        require(sideOverlapPercent in 40.0..90.0) {
            message(R.string.survey_side_overlap_range_error, "Side overlap must be 40–90%")
        }
        require(speedMetersPerSecond in 0.5..10.0) {
            message(R.string.survey_speed_range_error, "Planning speed must be 0.5–10.0 m/s")
        }
        require(obliqueSpeedMetersPerSecond in 0.5..10.0) {
            message(R.string.survey_oblique_speed_range_error, "Oblique speed must be 0.5–10.0 m/s")
        }
        if (obliqueFiveDirection) {
            require(gimbalPitchDegrees in -80.0..-30.0) {
                message(R.string.survey_five_direction_pitch_range_error, "Five-direction oblique pitch must be -80° to -30°")
            }
        } else {
            require(gimbalPitchDegrees in -90.0..-30.0) {
                message(R.string.survey_gimbal_pitch_range_error, "Gimbal pitch must be -90° to -30°")
            }
        }
        require(boundaryMarginMeters in 0.0..30.0) { message(R.string.survey_boundary_margin_range_error, "Boundary margin must be 0–30 m") }
        require(targetSurfaceToTakeoffMeters in -500.0..500.0) { message(R.string.survey_target_surface_range_error, "Target surface offset must be -500–500 m") }
        require(safeTakeoffAltitudeMeters in 5.0..120.0) { message(R.string.survey_safe_takeoff_altitude_range_error, "Safe takeoff altitude must be 5–120 m") }
        require(takeoffSpeedMetersPerSecond in 0.5..10.0) { message(R.string.survey_takeoff_speed_range_error, "Takeoff speed must be 0.5–10.0 m/s") }
        require(obliqueForwardOverlapPercent in 50.0..90.0) { message(R.string.survey_oblique_forward_overlap_range_error, "Oblique forward overlap must be 50–90%") }
        require(obliqueSideOverlapPercent in 40.0..90.0) { message(R.string.survey_oblique_side_overlap_range_error, "Oblique side overlap must be 40–90%") }
        require(timedCaptureIntervalSeconds in 1.0..60.0) { message(R.string.survey_capture_interval_range_error, "Timed capture interval must be 1–60 s") }
        val effectiveFlightAltitudeMeters = if (altitudeMode == SurveyAltitudeMode.ABOVE_TARGET_SURFACE) {
            altitudeMetersAgl + targetSurfaceToTakeoffMeters
        } else {
            altitudeMetersAgl
        }
        require(effectiveFlightAltitudeMeters in 5.0..120.0) {
            message(R.string.survey_waypoint_relative_altitude_range_error, "Waypoint altitude relative to takeoff must be 5–120 m")
        }
        val normalizedHeading = ((routeHeadingDegrees % 360.0) + 360.0) % 360.0
        return SurveyConstraints(
            altitudeMetersAgl = altitudeMetersAgl,
            forwardOverlap = forwardOverlapPercent / 100.0,
            sideOverlap = sideOverlapPercent / 100.0,
            speedMetersPerSecond = speedMetersPerSecond,
            obliqueSpeedMetersPerSecond = obliqueSpeedMetersPerSecond,
            gimbalPitchDegrees = -90.0,
            routeHeadingDegrees = normalizedHeading,
            crosshatch = false,
            collectionMode = if (obliqueFiveDirection) {
                SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION
            } else {
                SurveyCollectionMode.ORTHO
            },
            obliqueGimbalPitchDegrees = gimbalPitchDegrees,
            boundaryMarginMeters = boundaryMarginMeters,
            altitudeMode = altitudeMode,
            targetSurfaceToTakeoffMeters = targetSurfaceToTakeoffMeters,
            safeTakeoffAltitudeMeters = safeTakeoffAltitudeMeters,
            takeoffSpeedMetersPerSecond = takeoffSpeedMetersPerSecond,
            takeoffMode = takeoffMode,
            obliqueForwardOverlap = obliqueForwardOverlapPercent / 100.0,
            obliqueSideOverlap = obliqueSideOverlapPercent / 100.0,
            startPointMode = startPointMode,
            completionAction = completionAction,
            captureTriggerMode = captureTriggerMode,
            timedCaptureIntervalSeconds = timedCaptureIntervalSeconds,
            enabledCaptureViews = enabledCaptureViews,
            obliqueHeadingMode = obliqueHeadingMode,
        )
    }
}
