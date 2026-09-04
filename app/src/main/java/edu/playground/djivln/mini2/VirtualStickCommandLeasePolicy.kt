package edu.playground.djivln.mini2

object VirtualStickCommandLeasePolicy {
    const val MAXIMUM_AGE_MILLIS = 1_000L

    fun shouldExpire(nonZero: Boolean, refreshedAtMillis: Long, nowMillis: Long): Boolean =
        nonZero && refreshedAtMillis > 0L && nowMillis - refreshedAtMillis >= MAXIMUM_AGE_MILLIS
}
