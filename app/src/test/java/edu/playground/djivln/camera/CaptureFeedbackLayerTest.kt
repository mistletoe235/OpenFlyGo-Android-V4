package edu.playground.djivln.camera

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class CaptureFeedbackLayerTest {
    @Test fun captureFeedbackStaysAboveFullscreenMapPlannerAndVideo() {
        val current = File(requireNotNull(System.getProperty("user.dir")))
        val root = listOf(current, current.parentFile).first { File(it, "app/build.gradle").isFile }
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val document = factory.newDocumentBuilder().parse(
            File(root, "app/src/main/res/layout/activity_mini2_camera.xml"),
        )
        val namespace = "http://schemas.android.com/apk/res/android"
        val elements = document.getElementsByTagName("*")
        val views = (0 until elements.length).map { elements.item(it) as Element }
        val feedback = views.single {
            it.getAttributeNS(namespace, "id") == "@+id/survey_photo_capture_feedback"
        }
        val elevation = feedback.getAttributeNS(namespace, "elevation").removeSuffix("dp").toFloatOrNull() ?: 0f
        val activity = File(root, "app/src/main/java/edu/playground/djivln/mini2/Mini2CameraActivity.java").readText()
        val runtimeElevations = Regex("setElevation\\(dp\\((\\d+)\\)\\)")
            .findAll(activity).map { it.groupValues[1].toFloat() }.toList()
        val layoutElevations = views.filter { it != feedback }.mapNotNull {
            it.getAttributeNS(namespace, "elevation").removeSuffix("dp").toFloatOrNull()
        }
        assertTrue(runtimeElevations.isNotEmpty())
        assertTrue(elevation > (runtimeElevations + layoutElevations).maxOrNull()!!)
        assertEquals(document.documentElement, feedback.parentNode)
        assertEquals("center", feedback.getAttributeNS(namespace, "layout_gravity"))
        assertEquals("gone", feedback.getAttributeNS(namespace, "visibility"))
        assertTrue(feedback.getAttributeNS(namespace, "clickable") != "true")
    }
}
