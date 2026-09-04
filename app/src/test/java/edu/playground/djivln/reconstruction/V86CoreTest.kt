package edu.playground.djivln.reconstruction

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V86CoreTest {
    @Test fun `empty and oversized ply headers fail before allocating point arrays`() {
        val empty = ("ply\nformat binary_little_endian 1.0\nelement vertex 0\n" +
            "property float x\nproperty float y\nproperty float z\n" +
            "property uchar red\nproperty uchar green\nproperty uchar blue\nend_header\n").toByteArray()
        val oversized = ("ply\nformat binary_little_endian 1.0\ncomment " +
            "x".repeat(70_000) + "\nend_header\n").toByteArray()
        for (bytes in listOf(empty, oversized)) {
            assertTrue(runCatching { V86PlyDecoder.decode(bytes) }.exceptionOrNull() is IllegalArgumentException)
            val file = Files.createTempFile("v86-invalid-", ".ply").toFile()
            try {
                file.writeBytes(bytes)
                assertTrue(runCatching { V86PlyDecoder.decode(file) }.exceptionOrNull() is IllegalArgumentException)
            } finally { file.delete() }
        }
    }

    @Test fun `workflow and point cloud cache separate preview from final`() {
        val preview = V86StreamingController.Snapshot(
            endpoint = "http://localhost", sessionId = "s-preview", session = V86SessionState(
                "s-preview", "receiving", 0.0, "ready", 12, false, false, false, true,
                null, 0, 0, 0, "idle", "", "idle", "", 0, 3, 5, 0, 10.0),
            result = null, capturedCount = 12, uploadedCount = 10, pendingCount = 2,
            rejectedCount = 0, uploading = true, retryAtEpochMillis = null,
            message = "uploading", error = null)
        assertEquals(V86WorkflowStage.STREAMING, V86WorkflowPolicy.from(preview).stage)
        assertTrue(V86PointCloudCachePolicy.fileStem(preview).contains("preview-12-3"))

        val final = preview.copy(session = preview.session!!.copy(completed = true))
        assertTrue(V86PointCloudCachePolicy.fileStem(final).endsWith("-final"))
    }

    @Test fun `session and result keep fail closed semantics`() {
        val state = V86SessionState.decode(JSONObject("""
            {"id":"s-test","phase":"fast_sfm","progress":0.1,"message":"running",
             "image_count":12,"fast_sfm_completed_images":8,"fast_sfm_target_images":12,
             "scal3r_lane_completed_windows":3,"scal3r_lane_target_windows":5}
        """))
        assertEquals(8, state.fastSfmCompletedImages)
        val result = V86Result.decode(JSONObject("""
            {"session_id":"s-test","phase":"complete","completed":true,
             "openfly_v5_mission":{"url":"/mission.json","safe_to_execute":false},
             "detectors":{"v50":{"tier_a_geometry_gaps":2},"v78":{"rgb_risk_candidates":5}}}
        """))
        assertFalse(result.safeToExecute)
        assertEquals(2, result.detectorCounts.v50TierA)
        assertNull(result.error)
    }

    @Test fun `retry and altitude policies are bounded`() {
        assertTrue(V86UploadRetryPolicy.isAutomaticallyRetryable(V86HttpException(503, "busy")))
        assertFalse(V86UploadRetryPolicy.isAutomaticallyRetryable(V86HttpException(401, "denied")))
        assertFalse(V86UploadRetryPolicy.isAutomaticallyRetryable(
            V86QueueIntegrityException("damaged")))
        assertFalse(V86UploadRetryPolicy.isAutomaticallyRetryable(
            V86HttpBodyLimitException("too large")))
        assertEquals(60_000L, V86UploadRetryPolicy.delayMillis(10))
        assertEquals(105.0, V86CaptureAltitudePolicy.resolve(null, 5.0, 100.0)!!
            .absoluteAltitudeMeters, 0.0)
        assertNull(V86CaptureAltitudePolicy.resolve(null, 5.0, null))
    }

    @Test fun `binary xyz rgb ply decodes and downsamples`() {
        val header = """
            ply
            format binary_little_endian 1.0
            element vertex 4
            property float x
            property float y
            property float z
            property uchar red
            property uchar green
            property uchar blue
            end_header
        """.trimIndent() + "\n"
        val bytes = ByteArrayOutputStream().apply {
            write(header.toByteArray(Charsets.US_ASCII))
            val body = ByteBuffer.allocate(4 * 15).order(ByteOrder.LITTLE_ENDIAN)
            repeat(4) { index ->
                body.putFloat(index.toFloat()).putFloat((index + 1).toFloat())
                    .putFloat((index + 2).toFloat())
                body.put((10 + index).toByte()).put((20 + index).toByte()).put((30 + index).toByte())
            }
            write(body.array())
        }.toByteArray()
        val cloud = V86PlyDecoder.decode(bytes, maxPoints = 2)
        assertEquals(2, cloud.size)
        assertEquals(0f, cloud.xyz[0])
        assertEquals(2f, cloud.xyz[3])
    }

    @Test fun `point cloud cache reuses valid ply and prunes old revisions`() {
        val root = Files.createTempDirectory("v86-cache-").toFile()
        try {
            fun cloud(stem: String, modified: Long) = java.io.File(root, "$stem.ply").apply {
                writeText("ply\nformat binary_little_endian 1.0\n")
                setLastModified(modified)
                java.io.File(root, "$stem-viewer.json").writeText("{}")
            }
            val current = cloud("session-final", 4L)
            val newest = cloud("other-new", 3L)
            val old = cloud("other-old", 2L)
            cloud("other-oldest", 1L)
            assertTrue(V86PointCloudCachePolicy.isUsablePly(current))
            V86PointCloudCachePolicy.prune(root, "session-final", maximumPlyFiles = 2)
            assertTrue(current.isFile)
            assertTrue(newest.isFile)
            assertFalse(old.exists())
            assertFalse(java.io.File(root, "other-old-viewer.json").exists())
        } finally { root.deleteRecursively() }
    }
}
