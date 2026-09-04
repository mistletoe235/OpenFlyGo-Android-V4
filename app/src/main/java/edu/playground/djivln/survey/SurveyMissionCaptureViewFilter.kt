package edu.playground.djivln.survey

import android.content.Context
import edu.playground.djivln.mini2.R
import java.util.UUID
import kotlin.math.asin
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/** Reuses already terrain-adjusted waypoints when only oblique capture groups change. */
object SurveyMissionCaptureViewFilter {
    @JvmStatic
    @JvmOverloads
    fun select(mission: SurveyMission, enabledViews: Set<SurveyCaptureView>, context: Context? = null): SurveyMission {
        require(mission.activeMapping == null) {
            context?.getString(R.string.recapture_groups_fixed) ?: "Active-recapture groups are fixed by the mission file and cannot be filtered again"
        }
        require(mission.constraints.collectionMode == SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION)
        require(enabledViews.isNotEmpty()) {
            context?.getString(R.string.survey_keep_at_least_one_group) ?: "Keep at least one route group"
        }
        require(enabledViews.all { it in mission.constraints.enabledCaptureViews }) {
            context?.getString(R.string.survey_selected_group_not_in_mission)
                ?: "The mission does not contain the newly selected group; import the DSM and generate it again"
        }
        val selectedPasses = mission.surveyPasses()
            .filter { it.start.captureView in enabledViews }
        val waypoints = selectedPasses.flatMapIndexed { newPassIndex, pass ->
            pass.waypoints.map { it.copy(passIndex = newPassIndex) }
        }
        require(waypoints.isNotEmpty()) {
            context?.getString(R.string.survey_selected_group_no_waypoints) ?: "The selected route groups have no executable waypoints"
        }
        val pathMeters = waypoints.zipWithNext().sumOf { (a, b) ->
            distanceMeters(a.point, b.point)
        }
        val photos = selectedPasses.sumOf { pass ->
                if (pass.isPointCapture) return@sumOf 1
                val interval = requireNotNull(pass.start.captureIntervalMeters)
                val passMeters = pass.waypoints.zipWithNext().sumOf { (a, b) ->
                    distanceMeters(a.point, b.point)
                }
                max(2, ceil(passMeters / interval).toInt() + 1)
            }
        val selectedConstraints = mission.constraints.copy(enabledCaptureViews = enabledViews.toSet())
        return mission.copy(
            id = UUID.randomUUID().toString(),
            createdAtEpochMillis = System.currentTimeMillis(),
            constraints = selectedConstraints,
            waypoints = waypoints,
            estimatedPathMeters = pathMeters,
            estimatedPhotoCount = photos,
            estimatedFlightSeconds = SurveyPlanner.estimateRouteSeconds(waypoints, selectedConstraints),
        )
    }

    private fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val earthRadius = 6_371_000.0
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val haversine = sin(dLat / 2.0) * sin(dLat / 2.0) +
            cos(lat1) * cos(lat2) * sin(dLon / 2.0) * sin(dLon / 2.0)
        return 2.0 * earthRadius * asin(sqrt(haversine.coerceIn(0.0, 1.0)))
    }
}
