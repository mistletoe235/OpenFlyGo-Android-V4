package edu.playground.djivln.survey

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DjiCameraProfileCatalogTest {
    @Test fun resolvesConsumerAndEnterpriseSdkNames() {
        val mini = DjiCameraProfileCatalog.resolve("DJI_MINI_2", "Mavic Mini 2 Camera")
        val air = DjiCameraProfileCatalog.resolve("DJI_AIR_2S")

        assertTrue(mini.verifiedProfile)
        assertEquals(4000, mini.profile.imageWidthPixels)
        assertTrue(air.verifiedProfile)
        assertEquals(5472, air.profile.imageWidthPixels)
    }

    @Test fun unknownPayloadIsExplicitlyUnverified() {
        val unknown = DjiCameraProfileCatalog.resolve("MATRICE_300_RTK", "ZENMUSE_CUSTOM")

        assertFalse(unknown.verifiedProfile)
        assertEquals(CameraProfile.GENERIC_4_BY_3.id, unknown.profile.id)
    }

    @Test fun sdkSentinelsAreNotShownAsCameraNames() {
        val unknown = DjiCameraProfileCatalog.resolve("NOT_SUPPORTED", "UNKNOWN", null)

        assertFalse(unknown.verifiedProfile)
        assertEquals("Unrecognized camera", unknown.displayName)
    }

    @Test fun m30RequiresAnExplicitWideLensIdentity() {
        val ambiguous = DjiCameraProfileCatalog.resolve("M30T")
        val zoom = DjiCameraProfileCatalog.resolve("M30T", "Zoom Camera")
        val thermal = DjiCameraProfileCatalog.resolve("M30T", "Infrared Camera")
        val wide = DjiCameraProfileCatalog.resolve("M30T", "Wide Camera")

        assertFalse(ambiguous.verifiedProfile)
        assertFalse(zoom.verifiedProfile)
        assertFalse(thermal.verifiedProfile)
        assertTrue(wide.verifiedProfile)
        assertEquals("dji-matrice-30-wide-12mp", wide.profile.id)
    }
}
