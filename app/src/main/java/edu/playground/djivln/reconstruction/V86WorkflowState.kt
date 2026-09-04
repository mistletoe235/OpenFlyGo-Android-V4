package edu.playground.djivln.reconstruction

enum class V86WorkflowStage {
    NOT_READY, READY, STREAMING, UPLOAD_RETRY, FINALIZING,
    PROCESSING, PLY_READY, RESULT_READY, ERROR,
}

data class V86WorkflowState(
    val stage: V86WorkflowStage,
    val title: String,
    val detail: String,
    val progress: Double?,
    val plyReady: Boolean,
    val canFinalize: Boolean,
    val canDiscardEmpty: Boolean,
    val needsAttention: Boolean,
)

object V86WorkflowPolicy {
    fun from(snapshot: V86StreamingController.Snapshot): V86WorkflowState {
        val session = snapshot.session
        val result = snapshot.result
        val plyReady = !result?.pointCloudUrl.isNullOrBlank()
        val error = snapshot.error
        val captured = snapshot.capturedCount
        val uploaded = snapshot.uploadedCount
        val pending = snapshot.pendingCount
        return when {
            snapshot.sessionId == null -> V86WorkflowState(
                if (snapshot.endpoint.isBlank()) V86WorkflowStage.NOT_READY else V86WorkflowStage.READY,
                "V86 ready", snapshot.message, null, false, false, false, error != null)
            error != null && pending > 0 -> V86WorkflowState(
                V86WorkflowStage.UPLOAD_RETRY, "Upload deferred", error,
                if (captured > 0) uploaded.toDouble() / captured else null,
                plyReady, false, false, true)
            error != null -> V86WorkflowState(V86WorkflowStage.ERROR, "V86 needs attention", error,
                session?.progress, plyReady, false, false, true)
            session?.sealed != true -> V86WorkflowState(V86WorkflowStage.STREAMING, "Capturing",
                "$captured captured · $uploaded uploaded · $pending pending",
                if (captured > 0) uploaded.toDouble() / captured else 0.0,
                false, pending == 0 && captured > 0 && !snapshot.uploading,
                pending == 0 && captured == 0 && !snapshot.uploading,
                snapshot.rejectedCount > 0)
            plyReady && !result?.missionUrl.isNullOrBlank() -> V86WorkflowState(
                V86WorkflowStage.RESULT_READY, "Result ready", result?.message.orEmpty(),
                1.0, true, false, false, false)
            plyReady -> V86WorkflowState(V86WorkflowStage.PLY_READY, "Point cloud ready",
                result?.message.orEmpty(), 1.0, true, false, false, false)
            session?.running == true || session?.phase !in setOf(null, "idle", "complete", "completed") ->
                V86WorkflowState(V86WorkflowStage.PROCESSING, session?.phase ?: "Processing",
                    session?.message.orEmpty(), processingProgress(session), false, false, false, false)
            else -> V86WorkflowState(V86WorkflowStage.FINALIZING, "Capture frozen",
                session?.message.orEmpty(), processingProgress(session), false, false, false, false)
        }
    }

    private fun processingProgress(session: V86SessionState?): Double? = when {
        session == null -> null
        session.scal3rLaneTargetWindows > 0 ->
            session.scal3rLaneCompletedWindows.toDouble() / session.scal3rLaneTargetWindows
        session.fastSfmTargetImages > 0 ->
            session.fastSfmCompletedImages.toDouble() / session.fastSfmTargetImages
        session.progress > 0 -> session.progress
        else -> null
    }
}
