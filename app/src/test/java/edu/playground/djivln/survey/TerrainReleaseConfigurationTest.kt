package edu.playground.djivln.survey

import edu.playground.djivln.mini2.BuildConfig
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TerrainReleaseConfigurationTest {
    @Test fun terrainIsAvailableOnlyInDebugBuilds() {
        assertEquals(BuildConfig.DEBUG, BuildConfig.ENABLE_TERRAIN_FOLLOWING)
    }

    @Test fun releaseHidesTerrainAndDoesNotRestoreEnabledPreference() {
        val current = File(requireNotNull(System.getProperty("user.dir")))
        val root = listOf(current, current.parentFile).first { File(it, "app/build.gradle").isFile }
        val source = File(root, "app/src/main/java/edu/playground/djivln/mini2/Mini2CameraActivity.java").readText()
        assertTrue(source.contains("terrainTab.setVisibility(BuildConfig.ENABLE_TERRAIN_FOLLOWING ? View.VISIBLE : View.GONE)"))
        assertTrue(source.contains("surveyTerrainFollowingEnabled = BuildConfig.ENABLE_TERRAIN_FOLLOWING"))
        assertTrue(source.contains("!BuildConfig.ENABLE_TERRAIN_FOLLOWING && mission.getTerrainPlan() != null"))
        assertTrue(source.contains("!BuildConfig.ENABLE_TERRAIN_FOLLOWING && restoredMission.getTerrainPlan() != null"))
    }
}
