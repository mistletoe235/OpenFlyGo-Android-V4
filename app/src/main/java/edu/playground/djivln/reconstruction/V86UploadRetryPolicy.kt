package edu.playground.djivln.reconstruction

object V86UploadRetryPolicy {
    fun isAutomaticallyRetryable(error: Throwable): Boolean =
        error !is V86QueueIntegrityException && error !is V86HttpBodyLimitException &&
            (error !is V86HttpException || error.statusCode == 408 ||
            error.statusCode == 429 || error.statusCode >= 500
            )

    fun delayMillis(attempt: Int): Long {
        val exponent = attempt.coerceIn(1, 6) - 1
        return (2_000L shl exponent).coerceAtMost(60_000L)
    }
}

class V86QueueIntegrityException(message: String) : Exception(message)
