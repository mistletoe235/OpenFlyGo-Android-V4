package edu.playground.djivln.survey

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportedMissionCameraCompatibilityPolicyTest {
    @Test fun unlistedCameraAndDifferentGeometryOnlyWarn() {
        val mission = fixture()
        val result = ImportedMissionCameraCompatibilityPolicy.evaluate(
            mission,
            mission.cameraProfile.copy(id = "unlisted-camera", imageHeightPixels = 2250),
            cameraConnected = true,
            profileVerified = false,
        )
        assertTrue(result.reasons.joinToString(), result.compatible)
        assertTrue(result.warnings.isNotEmpty())
    }

    private fun fixture(): SurveyMission {
        val root = File(requireNotNull(System.getProperty("user.dir")))
        val file = listOf(
            File(root, "testdata/active-recapture/two-buildings/openfly-active-recapture-two-buildings-v1.json"),
            File(requireNotNull(root.parentFile), "testdata/active-recapture/two-buildings/openfly-active-recapture-two-buildings-v1.json"),
        ).firstOrNull(File::isFile) ?: error("active recapture fixture not found")
        return SurveyMissionJson.decode(file.readText())
    }

    @Test fun `verified compatible camera can execute imported mission`() {
        val mission = fixture()
        val current = mission.cameraProfile.copy(
            id = "compatible-camera",
            imageWidthPixels = 4032,
            imageHeightPixels = 3024,
            minimumCaptureIntervalSeconds = mission.cameraProfile.minimumCaptureIntervalSeconds,
        )
        val result = ImportedMissionCameraCompatibilityPolicy.evaluate(
            mission, current, cameraConnected = true, profileVerified = true)
        assertTrue(result.reasons.joinToString(), result.compatible)
    }

    @Test fun `all camera incompatibilities are reported together`() {
        val mission = fixture()
        val result = ImportedMissionCameraCompatibilityPolicy.evaluate(
            mission,
            mission.cameraProfile.copy(
                imageWidthPixels = 4000,
                imageHeightPixels = 2250,
                minimumCaptureIntervalSeconds = 30.0,
            ),
            cameraConnected = false,
            profileVerified = false,
        )
        assertFalse(result.compatible)
        assertTrue(result.reasons.any { it.contains("disconnected") })
        assertTrue(result.warnings.any { it.contains("estimated") })
        assertTrue(result.warnings.any { it.contains("aspect ratio") })
        assertTrue(result.reasons.any { it.contains("minimum capture interval") })
    }
}
