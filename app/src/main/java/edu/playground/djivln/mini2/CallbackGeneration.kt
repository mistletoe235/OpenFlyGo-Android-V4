package edu.playground.djivln.mini2

import java.util.concurrent.atomic.AtomicLong

/** Thread-safe generation token used to reject callbacks from replaced DJI sessions. */
class CallbackGeneration(initialValue: Long = 0L) {
    private val value = AtomicLong(initialValue)

    fun current(): Long = value.get()

    fun next(): Long = value.incrementAndGet()

    fun accepts(candidate: Long): Boolean = candidate == value.get()
}
