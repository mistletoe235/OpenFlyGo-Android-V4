package edu.playground.djivln.hil

/** Accepts each sequence once while tolerating up to 63 packets of reordering. */
internal class HilReplayWindow {
    private var initialized = false
    private var highest = 0L
    private var seen = 0L

    @Synchronized
    fun accept(sequence: Long): Boolean {
        if (sequence <= 0L) return false
        if (!initialized) {
            initialized = true
            highest = sequence
            seen = 1L
            return true
        }
        if (sequence > highest) {
            val shift = sequence - highest
            seen = if (shift >= WINDOW_BITS) 1L else (seen shl shift.toInt()) or 1L
            highest = sequence
            return true
        }
        val distance = highest - sequence
        if (distance >= WINDOW_BITS) return false
        val mask = 1L shl distance.toInt()
        if (seen and mask != 0L) return false
        seen = seen or mask
        return true
    }

    @Synchronized
    fun reset() {
        initialized = false
        highest = 0L
        seen = 0L
    }

    private companion object {
        const val WINDOW_BITS = 64L
    }
}
