package edu.playground.djivln.camera

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveyFrameWorkBudgetTest {
    @Test
    fun stalledWriterRetainsOnlyTwoFramesAndRecoversAfterCompletion() {
        val budget = SurveyFrameWorkBudget(2)
        val queued = ArrayDeque<SurveyFrameWorkBudget.Lease>()
        repeat(100000) { budget.tryAcquire()?.let(queued::addLast) }
        assertEquals(2, queued.size)
        assertEquals(2, budget.pendingCount())
        val completed = queued.removeFirst()
        completed.close()
        completed.close()
        assertEquals(1, budget.pendingCount())
        val next = budget.tryAcquire()
        assertNotNull(next)
        assertNull(budget.tryAcquire())
        queued.removeFirst().close()
        next!!.close()
        assertEquals(0, budget.pendingCount())
    }

    @Test
    fun captureAndWriterThreadsCannotExceedCapacity() {
        val budget = SurveyFrameWorkBudget(2)
        val workers = Executors.newFixedThreadPool(4)
        val concurrent = AtomicInteger()
        val maximum = AtomicInteger()
        try {
            val jobs = (0 until 4).map {
                workers.submit {
                    repeat(10000) {
                        val lease = budget.tryAcquire() ?: return@repeat
                        val active = concurrent.incrementAndGet()
                        maximum.accumulateAndGet(active, ::maxOf)
                        concurrent.decrementAndGet()
                        lease.close()
                    }
                }
            }
            jobs.forEach { it.get(10, TimeUnit.SECONDS) }
            assertTrue(maximum.get() in 1..2)
            assertEquals(0, budget.pendingCount())
        } finally {
            workers.shutdownNow()
        }
    }

    @Test
    fun activityHoldsBudgetAcrossDecoderWriterAndLateCallbacks() {
        val current = File(requireNotNull(System.getProperty("user.dir")))
        val project = listOf(current, current.parentFile).first { File(it, "app/build.gradle").isFile }
        val source = File(project, "app/src/main/java/edu/playground/djivln/mini2/Mini2CameraActivity.java").readText()
        val capture = source.substringAfter("private long beginTriggerAlignedFrameCapture(")
            .substringBefore("private void awaitFreshPostTriggerFrame(")
        assertTrue(capture.contains("triggerFrameBudget.tryAcquire()"))
        assertTrue(capture.indexOf("triggerFrameBudget.tryAcquire()") < capture.indexOf("new PendingTriggerFrame("))
        val writer = source.substringAfter("private void saveTriggerAlignedFrame(")
            .substringBefore("private void captureSurveyUeFrame(")
        assertTrue(writer.contains("finally {\n            bitmap.recycle();\n            lease.close();"))
        assertTrue(source.contains("if (!pending.bitmapRequested || pending.bitmapReturned) pending.lease.close();"))
        assertTrue(source.contains("bitmap, pending.metadata, pending.resultMessage, pending.lease"))
        val hil = source.substringAfter("private void pollSurveyUeFrame(")
            .substringBefore("private void finishSurveyPhotoRequest(")
        assertTrue(hil.contains("triggerFrameBudget.tryAcquire()"))
        assertTrue(hil.contains("lease.close()"))
    }
}
