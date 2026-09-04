package edu.playground.djivln.survey

import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class SurveyUeBridgeClient : AutoCloseable {
    fun interface Callback {
        fun onComplete(ok: Boolean, message: String)
    }

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val statePostInFlight = AtomicBoolean(false)

    fun postMission(endpoint: String, mission: SurveyMission, callback: Callback) {
        post(endpoint, "/v1/survey/mission", SurveyUeBridgeContract.encodeMission(mission), callback)
    }

    fun postTelemetry(endpoint: String, telemetry: SurveyUeTelemetry, callback: Callback): Boolean {
        if (!statePostInFlight.compareAndSet(false, true)) return false
        post(endpoint, "/v1/survey/telemetry", SurveyUeBridgeContract.encodeTelemetry(telemetry)) { ok, message ->
            statePostInFlight.set(false)
            callback.onComplete(ok, message)
        }
        return true
    }

    fun postTarget(endpoint: String, target: SurveyUeTarget, callback: Callback) {
        post(endpoint, "/v1/survey/target", SurveyUeBridgeContract.encodeTarget(target), callback)
    }

    fun postCapture(endpoint: String, capture: SurveyUeCapture, callback: Callback) {
        post(endpoint, "/v1/survey/capture", SurveyUeBridgeContract.encodeCapture(capture), callback)
    }

    private fun post(endpoint: String, path: String, json: String, callback: Callback) {
        executor.execute {
            try {
                val base = endpoint.trim().removeSuffix("/")
                require(base.startsWith("http://") || base.startsWith("https://")) {
                    "endpoint must start with http:// or https://"
                }
                val connection = URL(base + path).openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = 2_000
                    connection.readTimeout = 2_000
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    val bytes = json.toByteArray(StandardCharsets.UTF_8)
                    connection.setFixedLengthStreamingMode(bytes.size)
                    connection.outputStream.use { it.write(bytes) }
                    val code = connection.responseCode
                    callback.onComplete(code in 200..299, "HTTP $code")
                } finally {
                    connection.disconnect()
                }
            } catch (error: Throwable) {
                callback.onComplete(false, error.message ?: error.javaClass.simpleName)
            }
        }
    }

    override fun close() {
        executor.shutdownNow()
    }
}
