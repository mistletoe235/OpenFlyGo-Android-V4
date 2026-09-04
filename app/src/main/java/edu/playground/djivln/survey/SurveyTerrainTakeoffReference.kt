package edu.playground.djivln.survey

import android.content.Context
import edu.playground.djivln.mini2.R
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class SurveyTerrainTakeoffReferenceSource { HOME_LOCATION, AIRCRAFT_LOCATION }

data class SurveyTerrainTakeoffReference(
    val point: GeoPoint,
    val source: SurveyTerrainTakeoffReferenceSource,
    val capturedAtEpochMillis: Long,
) {
    init { require(capturedAtEpochMillis > 0L) }
}

data class SurveyTerrainTakeoffVerification(
    val valid: Boolean,
    val distanceMeters: Double?,
    val terrainElevationDifferenceMeters: Double?,
    val reason: String?,
)

object SurveyTerrainTakeoffReferencePolicy {
    const val MAX_REFERENCE_DISTANCE_METERS = 15.0
    const val MAX_TERRAIN_ELEVATION_DIFFERENCE_METERS = 3.0

    fun verify(
        plan: SurveyTerrainPlan,
        currentHome: GeoPoint?,
        terrain: TerrainElevationSource?,
        context: Context? = null,
    ): SurveyTerrainTakeoffVerification {
        val reference = plan.takeoffReference ?: return invalid(text(
            context, R.string.terrain_legacy_mission_takeoff_reference_missing,
            "Terrain mission is missing a takeoff reference; regenerate the route"))
        val home = currentHome ?: return invalid(text(
            context, R.string.terrain_home_gps_unavailable,
            "Current Home/GPS is unavailable; cannot verify terrain takeoff reference"))
        val source = terrain ?: return invalid(text(
            context, R.string.terrain_generation_elevation_source_missing,
            "The elevation data used to generate the route is not loaded"))
        val distance = distanceMeters(reference.point, home)
        if (distance > MAX_REFERENCE_DISTANCE_METERS) {
            return SurveyTerrainTakeoffVerification(false, distance, null,
                context?.getString(R.string.terrain_home_reference_too_far, distance,
                    MAX_REFERENCE_DISTANCE_METERS)
                    ?: "Current Home is %.1f m from the generation reference; regenerate the route".format(distance))
        }
        val elevation = runCatching { source.elevationMeters(home.latitude, home.longitude) }
            .getOrElse { return SurveyTerrainTakeoffVerification(false, distance, null,
                context?.getString(R.string.terrain_home_outside_elevation_coverage,
                    it.message.orEmpty()) ?: "Current Home is outside elevation coverage") }
        if (!elevation.isFinite()) return SurveyTerrainTakeoffVerification(
            false, distance, null, text(context, R.string.terrain_home_elevation_invalid,
                "Current Home elevation is invalid"))
        val difference = kotlin.math.abs(elevation - plan.takeoffTerrainElevationMeters)
        if (difference > MAX_TERRAIN_ELEVATION_DIFFERENCE_METERS) {
            return SurveyTerrainTakeoffVerification(false, distance, difference,
                context?.getString(R.string.terrain_home_elevation_difference_too_large,
                    difference, MAX_TERRAIN_ELEVATION_DIFFERENCE_METERS)
                    ?: "Current Home terrain elevation differs by %.1f m; regenerate the route".format(difference))
        }
        return SurveyTerrainTakeoffVerification(true, distance, difference, null)
    }

    private fun invalid(reason: String) = SurveyTerrainTakeoffVerification(false, null, null, reason)
    private fun text(context: Context?, id: Int, fallback: String) = context?.getString(id) ?: fallback

    private fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val latitudeA = Math.toRadians(a.latitude)
        val latitudeB = Math.toRadians(b.latitude)
        val dLat = latitudeB - latitudeA
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).let { it * it } + cos(latitudeA) * cos(latitudeB) *
            sin(dLon / 2).let { it * it }
        return 2 * 6_371_000.0 * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }
}
