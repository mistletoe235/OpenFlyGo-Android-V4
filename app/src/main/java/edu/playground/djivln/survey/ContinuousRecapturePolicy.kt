package edu.playground.djivln.survey

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot

object ContinuousRecapturePolicy {
    fun eligible(mission: SurveyMission, index: Int): Boolean {
        if (mission.recaptureFlightMode != RecaptureFlightMode.CONTINUOUS_EXPERIMENTAL ||
            mission.activeMapping == null || index <= 0 || index >= mission.waypoints.lastIndex) return false
        val previous = mission.waypoints[index - 1]
        val target = mission.waypoints[index]
        val next = mission.waypoints[index + 1]
        if (listOf(previous, target, next).any {
                it.kind != SurveyWaypointKind.CAPTURE_POINT || it.captureAction != CaptureAction.CAPTURE_ON_REACH ||
                    it.captureView != target.captureView
            }) return false
        val metadata = mission.activeMapping.passes.associateBy { it.passIndex }
        if (metadata.isNotEmpty()) {
            val group = metadata[target.passIndex] ?: return false
            if (listOf(previous, target, next).any { waypoint ->
                    val entry = metadata[waypoint.passIndex]
                    entry == null || entry.regionId != group.regionId || entry.captureRole != "SURVEY" ||
                        entry.requiredForReconstructionBridge
                }) return false
        }
        return distance(previous.point, target.point) + 1e-6 >= 3.0 && distance(target.point, next.point) + 1e-6 >= 3.0 &&
            angleDifference(bearing(previous.point, target.point), bearing(target.point, next.point)) <= 30.0 &&
            listOf(previous, next).all {
                angleDifference(it.headingDegrees, target.headingDegrees) <= 5.0 &&
                    abs(it.gimbalPitchDegrees - target.gimbalPitchDegrees) <= 3.0 &&
                    abs(it.point.altitudeMeters - target.point.altitudeMeters) <= 0.5
            }
    }

    fun poseReady(
        connected: Boolean, pose: SurveyFollowerPose, target: SurveyWaypoint,
        actualGimbalPitch: Double, nowMillis: Long, flightUpdatedAtMillis: Long, gimbalUpdatedAtMillis: Long,
    ): Boolean {
        fun fresh(stamp: Long) = stamp > 0L && nowMillis >= stamp && nowMillis - stamp <= 1_000L
        return connected && fresh(flightUpdatedAtMillis) && fresh(gimbalUpdatedAtMillis) &&
            angleDifference(pose.headingDegrees, target.headingDegrees) <= StoppedCapturePosePolicy.MAX_HEADING_ERROR_DEGREES &&
            abs(pose.altitudeMeters - target.point.altitudeMeters) <= SurveyWaypointFollower.VERTICAL_TOLERANCE_METERS &&
            SurveyGimbalSettlePolicy.isSettled(target.gimbalPitchDegrees, actualGimbalPitch)
    }

    fun missedWindow(mission: SurveyMission, index: Int, pose: SurveyFollowerPose): Boolean {
        val previous = mission.waypoints[index - 1].point
        val target = mission.waypoints[index].point
        val incoming = offset(previous, target)
        val current = offset(previous, GeoPoint(pose.latitude, pose.longitude, pose.altitudeMeters))
        val length = hypot(incoming.first, incoming.second)
        return (current.first * incoming.first + current.second * incoming.second) / length >
            length + SurveyWaypointFollower.HORIZONTAL_TOLERANCE_METERS
    }

    fun command(
        mission: SurveyMission, index: Int, pose: SurveyFollowerPose,
        maximumSpeed: Double, maximumVerticalSpeed: Double,
    ): SurveyFollowerCommand {
        require(eligible(mission, index)) { "waypoint is not eligible for continuous capture" }
        val previous = mission.waypoints[index - 1].point
        val target = mission.waypoints[index]
        val next = mission.waypoints[index + 1].point
        val incoming = offset(previous, target.point)
        val outgoing = offset(target.point, next)
        val incomingLength = hypot(incoming.first, incoming.second)
        val outgoingLength = hypot(outgoing.first, outgoing.second)
        val position = GeoPoint(pose.latitude, pose.longitude, pose.altitudeMeters)
        val before = offset(previous, position)
        val after = offset(target.point, position)
        val incomingProgress = (before.first * incoming.first + before.second * incoming.second) / incomingLength
        val progress = if (incomingProgress <= incomingLength) incomingProgress else incomingLength +
            (after.first * outgoing.first + after.second * outgoing.second) / outgoingLength
        val speed = minOf(maximumSpeed, minOf(incomingLength, outgoingLength) /
            mission.cameraProfile.minimumCaptureIntervalSeconds * 0.7).coerceAtLeast(0.1)
        val lookahead = maxOf(2.5, speed / 0.55)
        val goalProgress = (progress + lookahead).coerceIn(0.0, incomingLength + outgoingLength * 0.5)
        val fraction = if (goalProgress <= incomingLength) goalProgress / incomingLength else
            (goalProgress - incomingLength) / outgoingLength
        val start = if (goalProgress <= incomingLength) previous else target.point
        val end = if (goalProgress <= incomingLength) target.point else next
        val goal = target.copy(point = GeoPoint(
            start.latitude + (end.latitude - start.latitude) * fraction,
            start.longitude + (end.longitude - start.longitude) * fraction,
            target.point.altitudeMeters,
        ))
        val capture = SurveyWaypointFollower.command(pose, target, speed, maximumVerticalSpeed)
        val moving = SurveyWaypointFollower.command(pose, goal, speed, maximumVerticalSpeed)
        return moving.copy(reached = capture.reached, horizontalErrorMeters = capture.horizontalErrorMeters,
            verticalErrorMeters = capture.verticalErrorMeters)
    }

    private fun offset(first: GeoPoint, second: GeoPoint): Pair<Double, Double> =
        (second.latitude - first.latitude) * 111_132.0 to
            (second.longitude - first.longitude) * 111_320.0 * cos(Math.toRadians(first.latitude))

    private fun distance(first: GeoPoint, second: GeoPoint): Double =
        offset(first, second).let { hypot(it.first, it.second) }

    private fun bearing(first: GeoPoint, second: GeoPoint): Double =
        offset(first, second).let { Math.toDegrees(atan2(it.second, it.first)) }

    private fun angleDifference(first: Double, second: Double): Double =
        abs(((second - first) % 360.0 + 540.0) % 360.0 - 180.0)
}
