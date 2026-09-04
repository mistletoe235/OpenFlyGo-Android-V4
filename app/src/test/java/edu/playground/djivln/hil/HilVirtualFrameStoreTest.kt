package edu.playground.djivln.hil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HilVirtualFrameStoreTest {
    @Test
    fun rejectsDuplicateAndOutOfOrderFrameIds() {
        val store = HilVirtualFrameStore()

        store.offer(frame(5L, 100L))
        store.offer(frame(5L, 110L))
        store.offer(frame(4L, 120L))

        val snapshot = store.snapshot(200L)!!
        assertEquals(5L, snapshot.frameId)
        assertEquals(1L, snapshot.receivedFrames)
        assertEquals(2L, snapshot.rejectedFrames)
    }

    @Test
    fun roleTakeoverClearAllowsNewStreamToRestartFrameIds() {
        val store = HilVirtualFrameStore()

        store.offer(frame(50L, 100L))
        store.clear()
        assertNull(store.snapshot(110L))
        store.offer(frame(1L, 120L))

        val snapshot = store.snapshot(200L)!!
        assertEquals(1L, snapshot.frameId)
        assertEquals(1L, snapshot.receivedFrames)
        assertEquals(0L, snapshot.rejectedFrames)
    }

    private fun frame(id: Long, receivedAt: Long) = HilFrameProtocol.Frame(
        frameId = id,
        poseSequence = id,
        capturePeerMonotonicNanos = receivedAt,
        receivedAndroidMonotonicNanos = receivedAt,
        width = 2,
        height = 2,
        format = HilFrameProtocol.FORMAT_JPEG,
        flags = 0,
        encoded = byteArrayOf(1, 2, 3),
    )
}
