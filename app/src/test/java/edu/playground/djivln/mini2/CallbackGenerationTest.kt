package edu.playground.djivln.mini2

import org.junit.Assert.assertEquals

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CallbackGenerationTest {
    @Test
    fun `queued control callback is rejected after a newer transition`() {
        val generation = CallbackGeneration()
        var callbacks = 0
        val delayed = generation.guarded(generation.next(), { true }, Runnable { callbacks++ })
        generation.next()
        delayed.run()
        assertEquals(0, callbacks)
    }

    @Test
    fun `queued control callback checks controller identity when dispatched`() {
        val generation = CallbackGeneration()
        var sameController = true
        var callbacks = 0
        val delayed = generation.guarded(generation.next(), { sameController }, Runnable { callbacks++ })
        sameController = false
        delayed.run()
        assertEquals(0, callbacks)
        generation.guarded(generation.current(), { true }, Runnable { callbacks++ }).run()
        assertEquals(1, callbacks)
    }

    @Test
    fun `advancing a generation rejects every older callback`() {
        val gate = CallbackGeneration()
        val first = gate.current()
        assertTrue(gate.accepts(first))

        val second = gate.next()
        assertFalse(gate.accepts(first))
        assertTrue(gate.accepts(second))
    }

    @Test
    fun `concurrent invalidations never reuse a generation`() {
        val gate = CallbackGeneration()
        val pool = Executors.newFixedThreadPool(8)
        val done = CountDownLatch(100)
        val values = java.util.concurrent.ConcurrentHashMap.newKeySet<Long>()
        repeat(100) {
            pool.execute {
                values += gate.next()
                done.countDown()
            }
        }
        assertTrue(done.await(5, TimeUnit.SECONDS))
        pool.shutdownNow()

        assertTrue(values.size == 100)
        assertTrue(gate.accepts(gate.current()))
    }
}
