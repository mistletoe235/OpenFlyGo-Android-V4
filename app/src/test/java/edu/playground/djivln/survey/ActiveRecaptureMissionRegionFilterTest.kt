package edu.playground.djivln.survey

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveRecaptureMissionRegionFilterTest {
    private fun fixture(): SurveyMission {
        val root = File(System.getProperty("user.dir"))
        val file = listOf(
            File(root, "testdata/active-recapture/two-buildings/openfly-active-recapture-two-buildings-v1.json"),
            File(root.parentFile, "testdata/active-recapture/two-buildings/openfly-active-recapture-two-buildings-v1.json"),
        ).firstOrNull(File::isFile) ?: error("active recapture fixture not found")
        val decoded = SurveyMissionJson.decode(file.readText())
        return decoded.copy(
            activeMapping = decoded.activeMapping!!.copy(
                selectionMethod = "scene-agnostic GPS + fast SfM + Scal3R filter fixture",
            ),
        )
    }

    @Test
    fun `legacy mission exposes one selectable group per region`() {
        val mission = fixture()
        val groups = ActiveRecaptureMissionGroupCatalog.groups(mission)

        assertEquals(mission.activeMapping!!.regions.size, groups.size)
        assertEquals(groups.map { it.groupId }.toSet(),
            ActiveRecaptureMissionGroupCatalog.selectedGroupIds(mission, mission))
    }

    @Test
    fun `four known region kinds collapse into four ordered user groups`() {
        val mission = fixture()
        val kinds = listOf(
            "HIGH_RISE_FIVE_DIRECTION",
            "LARGE_FIVE_DIRECTION",
            "SMALL_CROSS",
            "V26_RISK_SCAN",
        )
        val sourceRegions = mission.activeMapping!!.regions
        val regions = (0 until 8).map { index ->
            sourceRegions[index % sourceRegions.size].copy(
                regionId = "V30_R${index + 1}",
                priority = index + 1,
                kind = kinds[index % kinds.size],
                suggestedSurveyPhotos = index + 1,
            )
        }
        val grouped = mission.copy(activeMapping = mission.activeMapping.copy(regions = regions))

        val groups = ActiveRecaptureMissionGroupCatalog.groups(grouped)

        assertEquals(listOf("V30_HIGH_RISE", "V30_LARGE", "V30_SMALL_CROSS", "V30_RISK_SCAN"),
            groups.map { it.groupId })
        assertEquals(listOf(1, 2, 3, 4), groups.map { it.order })
        assertEquals(regions.sumOf { it.suggestedSurveyPhotos },
            groups.sumOf { it.suggestedSurveyPhotos })
    }

    @Test
    fun `filter keeps intervening route as non capturing transit and is deterministic`() {
        val mission = fixture()
        val groups = ActiveRecaptureMissionGroupCatalog.groups(mission)
        val selectedId = groups.first().groupId

        val filtered = ActiveRecaptureMissionRegionFilter.selectGroups(mission, setOf(selectedId))
        val repeated = ActiveRecaptureMissionRegionFilter.selectGroups(mission, setOf(selectedId))

        assertNotEquals(mission.id, filtered.id)
        assertEquals(filtered.id, repeated.id)
        assertEquals(setOf(selectedId),
            ActiveRecaptureMissionGroupCatalog.selectedGroupIds(mission, filtered))
        assertTrue(filtered.activeMapping!!.regions.all { it.regionId in groups.first().regionIds })
        assertTrue(filtered.waypoints.any { it.captureAction != CaptureAction.NONE })
        val transitPasses = filtered.surveyPasses().filter { it.isTransitOnly }
        assertTrue(transitPasses.all { pass ->
            pass.waypoints.all { it.captureAction == CaptureAction.NONE && it.captureIntervalMeters == null }
        })
        val transitPassIndices = transitPasses.mapTo(hashSetOf()) { it.start.passIndex }
        assertFalse(SurveyCaptureSchedule.build(filtered).any { it.passIndex in transitPassIndices })
        ActiveRecaptureMissionValidator.validate(filtered)
    }

    @Test
    fun `all groups returns source and empty or unknown selections fail closed`() {
        val mission = fixture()
        val all = ActiveRecaptureMissionGroupCatalog.groups(mission).mapTo(linkedSetOf()) { it.groupId }

        assertSame(mission, ActiveRecaptureMissionRegionFilter.selectGroups(mission, all))
        assertThrows(IllegalArgumentException::class.java) {
            ActiveRecaptureMissionRegionFilter.selectGroups(mission, emptySet())
        }
        assertThrows(IllegalArgumentException::class.java) {
            ActiveRecaptureMissionRegionFilter.selectGroups(mission, setOf("UNKNOWN"))
        }
    }
}
