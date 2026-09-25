package edu.playground.djivln.reconstruction

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.xml.parsers.DocumentBuilderFactory

class CloudReleaseConfigurationTest {
    @Test fun userConfiguredHttpWorkstationsAreNotLimitedToDebugBuilds() {
        val current = File(requireNotNull(System.getProperty("user.dir")))
        val root = listOf(current, current.parentFile).first { File(it, "app/build.gradle").isFile }
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val document = factory.newDocumentBuilder().parse(File(root, "app/src/main/AndroidManifest.xml"))
        val application = document.getElementsByTagName("application").item(0)
        assertEquals("true", application.attributes.getNamedItemNS(
            "http://schemas.android.com/apk/res/android", "usesCleartextTraffic").nodeValue)
    }
}
