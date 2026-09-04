package edu.playground.djivln.reconstruction

import org.json.JSONArray
import org.json.JSONObject

data class V86SessionConfig(
    val name: String,
    val horizontalFovDegrees: Double,
    val takeoffAbsoluteAltitudeMeters: Double,
    val cameraModel: String,
    val enableScal3r: Boolean = true,
    val autoPreview: Boolean = true,
    val maximumTasks: Int = 10,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("name", name).put("horizontal_fov_deg", horizontalFovDegrees)
        .put("takeoff_absolute_altitude_m", takeoffAbsoluteAltitudeMeters)
        .put("camera_model", cameraModel).put("enable_scal3r", enableScal3r)
        .put("auto_preview", autoPreview).put("maximum_tasks", maximumTasks)
}

data class V86SessionState(
    val id: String,
    val phase: String,
    val progress: Double,
    val message: String,
    val imageCount: Int,
    val sealed: Boolean,
    val running: Boolean,
    val completed: Boolean,
    val previewReady: Boolean,
    val error: String?,
    val fastSfmCompletedImages: Int,
    val fastSfmTargetImages: Int,
    val fastSfmResumeFrom: Int,
    val sfmLanePhase: String,
    val sfmLaneMessage: String,
    val scal3rLanePhase: String,
    val scal3rLaneMessage: String,
    val scal3rLaneSnapshotImages: Int,
    val scal3rLaneCompletedWindows: Int,
    val scal3rLaneTargetWindows: Int,
    val scal3rLaneCacheHits: Int,
    val takeoffAbsoluteAltitudeMeters: Double?,
) {
    companion object {
        fun decode(value: JSONObject) = V86SessionState(
            id = value.getString("id"), phase = value.optString("phase", "unknown"),
            progress = value.optDouble("progress", 0.0), message = value.optString("message", ""),
            imageCount = value.optInt("image_count", 0), sealed = value.optBoolean("sealed", false),
            running = value.optBoolean("running", false), completed = value.optBoolean("completed", false),
            previewReady = value.optBoolean("preview_ready", false), error = value.optNullableString("error"),
            fastSfmCompletedImages = value.optInt("fast_sfm_completed_images", 0),
            fastSfmTargetImages = value.optInt("fast_sfm_target_images", 0),
            fastSfmResumeFrom = value.optInt("fast_sfm_resume_from", 0),
            sfmLanePhase = value.optString("sfm_lane_phase", "idle"),
            sfmLaneMessage = value.optString("sfm_lane_message", ""),
            scal3rLanePhase = value.optString("scal3r_lane_phase", "idle"),
            scal3rLaneMessage = value.optString("scal3r_lane_message", ""),
            scal3rLaneSnapshotImages = value.optInt("scal3r_lane_snapshot_images", 0),
            scal3rLaneCompletedWindows = value.optInt("scal3r_lane_completed_windows", 0),
            scal3rLaneTargetWindows = value.optInt("scal3r_lane_target_windows", 0),
            scal3rLaneCacheHits = value.optInt("scal3r_lane_cache_hits", 0),
            takeoffAbsoluteAltitudeMeters = value.optJSONObject("config")
                ?.takeIf { !it.isNull("takeoff_absolute_altitude_m") }
                ?.optDouble("takeoff_absolute_altitude_m")?.takeIf(Double::isFinite),
        )
    }
}

data class V86DetectorCounts(
    val v50TierA: Int = 0, val v50TierB: Int = 0,
    val v78: Int = 0, val v78Selected: Int = 0,
)

data class V86Result(
    val sessionId: String, val phase: String, val completed: Boolean, val message: String,
    val geometryKind: String?, val pointCloudUrl: String?, val viewerDataUrl: String?,
    val missionUrl: String?, val safeToExecute: Boolean, val detectorCounts: V86DetectorCounts,
    val error: String?, val missionError: String?,
) {
    companion object {
        fun decode(value: JSONObject): V86Result {
            val pointCloud = value.optJSONObject("point_cloud")
            val candidates = value.optJSONObject("candidates")
            val mission = value.optJSONObject("openfly_v5_mission")
            val detectors = value.optJSONObject("detectors")
            val v50 = detectors?.optJSONObject("v50")
            val v78 = detectors?.optJSONObject("v78")
            return V86Result(
                value.getString("session_id"), value.optString("phase", "unknown"),
                value.optBoolean("completed", false), value.optString("message", ""),
                value.optNullableString("geometry_kind"), pointCloud?.optNullableString("url"),
                candidates?.optNullableString("url"), mission?.optNullableString("url"),
                mission?.optBoolean("safe_to_execute", false) ?: false,
                V86DetectorCounts(
                    v50?.optInt("tier_a_geometry_gaps", 0) ?: 0,
                    v50?.optInt("tier_b_review", 0) ?: 0,
                    v78?.optInt("rgb_risk_candidates", 0) ?: 0,
                    v78?.optInt("selected_after_union", 0) ?: 0,
                ),
                value.optNullableString("error"), value.optNullableString("mission_error"),
            )
        }
    }
}

data class V86Candidate(
    val detector: String, val tier: String, val score: Double,
    val x: Float, val y: Float, val z: Float, val kind: String = "CANDIDATE",
)

fun decodeV86Candidates(raw: String): List<V86Candidate> {
    val root = JSONObject(raw)
    val rows = root.optJSONArray("candidates") ?: JSONArray()
    return buildList {
        for (index in 0 until rows.length()) {
            val row = rows.optJSONObject(index) ?: continue
            val center = row.optJSONArray("center_xyz_m") ?: continue
            if (center.length() < 3) continue
            add(V86Candidate(row.optString("detector", "UNKNOWN"), row.optString("tier", "UNKNOWN"),
                row.optDouble("score", 0.0), center.optDouble(0).toFloat(),
                center.optDouble(1).toFloat(), center.optDouble(2).toFloat()))
        }
        val tasks = root.optJSONArray("tasks") ?: JSONArray()
        for (taskIndex in 0 until tasks.length()) {
            val task = tasks.optJSONObject(taskIndex) ?: continue
            val cameras = task.optJSONArray("camera_xyz_m") ?: continue
            for (cameraIndex in 0 until cameras.length()) {
                val camera = cameras.optJSONArray(cameraIndex) ?: continue
                if (camera.length() < 3) continue
                add(V86Candidate("CAMERA", "RECAPTURE_POSITION",
                    task.optDouble("max_online_risk_score", 0.0),
                    camera.optDouble(0).toFloat(), camera.optDouble(1).toFloat(),
                    camera.optDouble(2).toFloat(), "CAMERA"))
            }
        }
    }
}

internal fun JSONObject.optNullableString(name: String): String? =
    if (isNull(name)) null else optString(name).takeIf(String::isNotBlank)
