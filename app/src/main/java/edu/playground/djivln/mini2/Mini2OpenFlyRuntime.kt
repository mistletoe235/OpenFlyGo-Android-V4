package edu.playground.djivln.mini2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import edu.playground.djivln.vln.InferenceControlClient

/** Public-build stub. The unreleased on-device inference runtime is intentionally absent. */
class Mini2OpenFlyRuntime @JvmOverloads constructor(
    @Suppress("UNUSED_PARAMETER") context: Context,
    @Suppress("UNUSED_PARAMETER") legacyDenoiseSteps: Int = 5,
) : AutoCloseable {
    private val unavailable = InferenceControlClient.Result(
        ok = false,
        statusCode = 501,
        message = "On-device inference is not included in this public release",
    )

    val controlClient: InferenceControlClient = object : InferenceControlClient {
        override fun health() = unavailable
        override fun load() = unavailable
        override fun preflight() = unavailable
        override fun reset() = unavailable
        override fun start(prompt: String) = unavailable
        override fun stop() = unavailable
    }

    fun activeModelDescription(): String = "Not included"
    fun denoiseSteps(): Int = 0
    fun setDenoiseSteps(@Suppress("UNUSED_PARAMETER") steps: Int): String = unavailable.message

    fun importModel(@Suppress("UNUSED_PARAMETER") uri: Uri): String =
        throw UnsupportedOperationException(unavailable.message)

    fun importLatestCloudModel(@Suppress("UNUSED_PARAMETER") progress: CloudModelProgress): String =
        throw UnsupportedOperationException(unavailable.message)

    fun infer(
        @Suppress("UNUSED_PARAMETER") bitmap: Bitmap,
        @Suppress("UNUSED_PARAMETER") requestedPrompt: String,
        @Suppress("UNUSED_PARAMETER") telemetry: Mini2AircraftBridge.Snapshot,
        @Suppress("UNUSED_PARAMETER") useVelocityEstimate: Boolean,
        @Suppress("UNUSED_PARAMETER") executedPrefix: Int = UAVFlowPolicyContract.DEFAULT_EXECUTED_PREFIX,
    ): InferenceControlClient.Result = unavailable

    fun resetEstimatedModelState() = Unit
    fun updateTelemetry(@Suppress("UNUSED_PARAMETER") telemetry: Mini2AircraftBridge.Snapshot) = Unit
    override fun close() = Unit
}

fun interface CloudModelProgress {
    fun onProgress(phase: String, completedBytes: Long, totalBytes: Long)
}
