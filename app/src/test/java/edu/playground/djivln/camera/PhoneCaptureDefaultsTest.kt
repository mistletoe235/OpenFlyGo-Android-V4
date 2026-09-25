package edu.playground.djivln.camera

import edu.playground.djivln.mini2.BuildConfig
import org.junit.Assert.assertFalse
import org.junit.Test

class PhoneCaptureDefaultsTest {
    @Test fun installationBuildDoesNotArchiveDownlinkFrames() {
        assertFalse(BuildConfig.SAVE_TRIGGER_FRAMES_TO_PHONE)
    }
}
