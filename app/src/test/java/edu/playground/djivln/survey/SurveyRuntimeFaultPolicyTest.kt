package edu.playground.djivln.survey

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveyRuntimeFaultPolicyTest {
    @Test
    fun `recognizes DJI process timeout`() {
        assertTrue(SurveyRuntimeFaultPolicy.isTimeout("Execution of this process has timed out"))
    }

    @Test
    fun `recognizes localized timeout`() {
        assertTrue(SurveyRuntimeFaultPolicy.isTimeout("相机指令超时"))
    }

    @Test
    fun `undefined camera error is not classified as timeout`() {
        assertFalse(SurveyRuntimeFaultPolicy.isTimeout("Undefined Error"))
    }

    @Test
    fun `only survey camera timeout pauses from generic action callback`() {
        assertTrue(SurveyRuntimeFaultPolicy.shouldPauseCameraAction(
            "航测相机", false, "Execution of this process has timed out", "航测相机",
        ))
        assertFalse(SurveyRuntimeFaultPolicy.shouldPauseCameraAction(
            "拍照", false, "Execution of this process has timed out", "航测相机",
        ))
        assertFalse(SurveyRuntimeFaultPolicy.shouldPauseCameraAction(
            "航测相机", true, "单拍模式已就绪", "航测相机",
        ))
    }
}
