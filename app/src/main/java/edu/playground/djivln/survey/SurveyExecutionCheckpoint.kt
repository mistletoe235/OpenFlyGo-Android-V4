package edu.playground.djivln.survey

import org.json.JSONObject

data class SurveyExecutionCheckpoint(
    val missionId: String,
    val waypointIndex: Int,
    val state: SurveyExecutionState,
    val updatedAtEpochMillis: Long,
    val executionLegIndex: Int = waypointIndex,
    val phase: SurveyExecutionPhase = SurveyExecutionPhase.SURVEY,
    val recoveryPoint: GeoPoint? = null,
) {
    init {
        require(missionId.isNotBlank())
        require(waypointIndex >= 0)
        require(executionLegIndex >= 0)
    }
}

object SurveyExecutionCheckpointJson {
    private const val SCHEMA_VERSION = 3

    fun encode(value: SurveyExecutionCheckpoint): String = JSONObject()
        .put("schema_version", SCHEMA_VERSION)
        .put("mission_id", value.missionId)
        .put("waypoint_index", value.waypointIndex)
        .put("state", value.state.name)
        .put("updated_at_epoch_ms", value.updatedAtEpochMillis)
        .put("execution_leg_index", value.executionLegIndex)
        .put("phase", value.phase.name)
        .apply {
            value.recoveryPoint?.let {
                put("recovery_latitude", it.latitude)
                put("recovery_longitude", it.longitude)
                put("recovery_altitude_m", it.altitudeMeters)
            }
        }
        .toString()

    fun decode(raw: String): SurveyExecutionCheckpoint {
        val root = JSONObject(raw)
        val schemaVersion = root.getInt("schema_version")
        require(schemaVersion in 1..SCHEMA_VERSION) { "unsupported checkpoint schema" }
        return SurveyExecutionCheckpoint(
            missionId = root.getString("mission_id"),
            waypointIndex = root.getInt("waypoint_index"),
            state = SurveyExecutionState.valueOf(root.getString("state")),
            updatedAtEpochMillis = root.getLong("updated_at_epoch_ms"),
            executionLegIndex = if (schemaVersion >= 2) root.getInt("execution_leg_index")
                // Sentinel forces the state machine to remap the legacy
                // mission waypoint index after safe-transit legs are built.
                else Int.MAX_VALUE,
            phase = if (schemaVersion >= 2) SurveyExecutionPhase.valueOf(root.getString("phase"))
                else SurveyExecutionPhase.SURVEY,
            recoveryPoint = if (schemaVersion >= 3 && root.has("recovery_latitude")) {
                GeoPoint(
                    root.getDouble("recovery_latitude"),
                    root.getDouble("recovery_longitude"),
                    root.getDouble("recovery_altitude_m"),
                )
            } else null,
        )
    }
}

data class SurveyRecoveryPosition(
    val waypointIndex: Int,
    val executionLegIndex: Int,
)

object SurveyCheckpointRecoveryPolicy {
    fun position(
        waypointIndex: Int,
        executionLegIndex: Int,
        state: SurveyExecutionState,
        phase: SurveyExecutionPhase,
        targetCaptureAction: CaptureAction,
        hasRecoveryPoint: Boolean,
    ): SurveyRecoveryPosition {
        val interruptedStripEnd = state == SurveyExecutionState.PAUSED &&
            phase == SurveyExecutionPhase.SURVEY &&
            targetCaptureAction == CaptureAction.STOP_DISTANCE_INTERVAL &&
            !hasRecoveryPoint &&
            waypointIndex > 0 && executionLegIndex > 0
        return if (interruptedStripEnd) {
            SurveyRecoveryPosition(waypointIndex - 1, executionLegIndex - 1)
        } else {
            SurveyRecoveryPosition(waypointIndex, executionLegIndex)
        }
    }
}
