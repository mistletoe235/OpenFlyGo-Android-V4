package edu.playground.djivln.camera

class SurveyFrameWorkBudget(private val capacity: Int) {
    init { require(capacity > 0) }

    private var pending = 0

    @Synchronized
    fun pendingCount(): Int = pending

    @Synchronized
    fun tryAcquire(): Lease? {
        if (pending >= capacity) return null
        pending++
        return Lease()
    }

    inner class Lease internal constructor() : AutoCloseable {
        private var closed = false

        override fun close() = synchronized(this@SurveyFrameWorkBudget) {
            if (!closed) {
                closed = true
                pending--
            }
        }
    }
}
