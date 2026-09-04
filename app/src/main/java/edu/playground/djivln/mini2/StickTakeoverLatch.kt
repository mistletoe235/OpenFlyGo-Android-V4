package edu.playground.djivln.mini2

import kotlin.math.abs

/**
 * Edge-triggered RC takeover detector with hysteresis.
 *
 * One deliberate stick deflection emits exactly one takeover event.  All sticks
 * must return close to centre before another event can be emitted, preventing a
 * held stick or noisy threshold crossings from repeatedly aborting control.
 */
class StickTakeoverLatch(
    private val triggerThreshold: Int = 50,
    private val rearmThreshold: Int = 20,
) {
    init {
        require(triggerThreshold > rearmThreshold)
        require(rearmThreshold >= 0)
    }

    private var armed = true

    fun update(positions: IntArray, virtualStickEnabled: Boolean): Boolean {
        val maximum = positions.maxOfOrNull { abs(it) } ?: 0
        if (!armed) {
            if (maximum <= rearmThreshold) armed = true
            return false
        }
        if (virtualStickEnabled && maximum >= triggerThreshold) {
            armed = false
            return true
        }
        return false
    }

    fun reset() {
        armed = true
    }
}
