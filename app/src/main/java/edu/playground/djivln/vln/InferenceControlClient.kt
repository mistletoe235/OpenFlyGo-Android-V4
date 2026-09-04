package edu.playground.djivln.vln

interface InferenceControlClient {
    data class Result(val ok: Boolean, val statusCode: Int, val message: String)

    fun health(): Result
    fun load(): Result
    fun preflight(): Result
    fun reset(): Result
    fun start(prompt: String): Result
    fun stop(): Result
}
