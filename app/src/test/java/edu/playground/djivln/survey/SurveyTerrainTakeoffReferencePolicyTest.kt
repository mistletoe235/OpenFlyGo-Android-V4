package edu.playground.djivln.survey

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveyTerrainTakeoffReferencePolicyTest {
    private val reference = GeoPoint(31.0, 121.0, 0.0)
    private val terrain = object : TerrainElevationSource {
        override val info = TerrainRasterInfo("test", 10, 10, 4326, null, 1.0, 1.0,
            30.9, 31.1, 120.9, 121.1)
        override fun elevationMeters(latitude: Double, longitude: Double) =
            10.0 + (latitude - 31.0) * 100.0
    }

    @Test fun `nearby home with matching elevation is valid`() {
        val result = SurveyTerrainTakeoffReferencePolicy.verify(
            plan(reference), GeoPoint(31.00001, 121.0, 0.0), terrain)
        assertTrue(result.reason.orEmpty(), result.valid)
    }

    @Test fun `missing reference and distant home fail closed`() {
        assertFalse(SurveyTerrainTakeoffReferencePolicy.verify(
            plan = plan(reference).copy(takeoffReference = null),
            currentHome = reference,
            terrain = terrain,
        ).valid)
        assertFalse(SurveyTerrainTakeoffReferencePolicy.verify(
            plan(reference), GeoPoint(31.001, 121.0, 0.0), terrain).valid)
    }

    @Test fun `changed terrain datum beyond three metres fails closed`() {
        val shifted = object : TerrainElevationSource {
            override val info = terrain.info
            override fun elevationMeters(latitude: Double, longitude: Double) = 14.0
        }
        assertFalse(SurveyTerrainTakeoffReferencePolicy.verify(
            plan(reference), reference, shifted).valid)
    }

    private fun plan(point: GeoPoint) = SurveyTerrainPlan(
        sourceName = "test", sourceSha256 = "a".repeat(64), epsg = 4326,
        targetAglMeters = 30.0, takeoffTerrainElevationMeters = 10.0,
        sampleSpacingMeters = 3.0, minimumTerrainElevationMeters = 9.0,
        maximumTerrainElevationMeters = 12.0, minimumWaypointAltitudeMeters = 29.0,
        maximumWaypointAltitudeMeters = 32.0,
        takeoffReference = SurveyTerrainTakeoffReference(
            point, SurveyTerrainTakeoffReferenceSource.HOME_LOCATION, 1L),
    )
}
