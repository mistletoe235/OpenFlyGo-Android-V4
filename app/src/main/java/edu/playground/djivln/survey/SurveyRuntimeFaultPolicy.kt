package edu.playground.djivln.survey

object SurveyRuntimeFaultPolicy {
    @JvmStatic
    fun isTimeout(message: String?): Boolean {
        val normalized = message?.trim()?.lowercase().orEmpty()
        return normalized.contains("timeout") ||
            normalized.contains("timed out") ||
            normalized.contains("\u8d85\u65f6")
    }

    @JvmStatic
    @JvmOverloads
    fun shouldPauseCameraAction(
        label: String?,
        ok: Boolean,
        message: String?,
        expectedCameraLabel: String = "Survey camera",
    ): Boolean {
        if (ok || label != expectedCameraLabel) return false
        return isTimeout(message)
    }
}
