package edu.playground.djivln.mini2

/**
 * Normalises the altitude signals exposed by MSDK4 without relying on an
 * aircraft model name. Not every aircraft reports a downward range value.
 */
internal object AltitudeTelemetryResolver {
    const val SOURCE_NONE = "NONE"
    const val SOURCE_DOWNWARD_RANGE = "DOWNWARD_RANGE"
    const val MAX_SAFETY_RANGE_METERS = 5.0

    data class Result(
        val relativeBarometricMeters: Double,
        val estimatedAslMeters: Double,
        val groundClearanceMeters: Double,
        val groundClearanceSource: String,
        val groundClearanceReliableForSafety: Boolean,
        val downwardVisionActive: Boolean,
    )

    fun resolve(
        relativeBarometricMeters: Double,
        takeoffAslMeters: Double,
        downwardRangeUsed: Boolean,
        downwardRangeHasError: Boolean,
        downwardRangeMeters: Double,
        downwardVisionActive: Boolean,
    ): Result {
        val relative = relativeBarometricMeters.takeIf(Double::isFinite) ?: Double.NaN
        val takeoffAsl = takeoffAslMeters.takeIf {
            it.isFinite() && it in -500.0..10_000.0
        }
        val estimatedAsl = if (relative.isFinite() && takeoffAsl != null) {
            relative + takeoffAsl
        } else {
            Double.NaN
        }

        // Trust the MSDK capability/state flags instead of a product-name list.
        // The API name is historical: newer aircraft may implement it with IR
        // or another downward ranging sensor rather than literal ultrasound.
        val groundClearance = downwardRangeMeters.takeIf {
            downwardRangeUsed && !downwardRangeHasError && it.isFinite() && it >= 0.0
        } ?: Double.NaN

        return Result(
            relativeBarometricMeters = relative,
            estimatedAslMeters = estimatedAsl,
            groundClearanceMeters = groundClearance,
            groundClearanceSource = if (groundClearance.isFinite()) {
                SOURCE_DOWNWARD_RANGE
            } else {
                SOURCE_NONE
            },
            // DJI documents the downward height as safety-relevant below 5 m.
            groundClearanceReliableForSafety = groundClearance.isFinite() &&
                groundClearance <= MAX_SAFETY_RANGE_METERS,
            downwardVisionActive = downwardVisionActive,
        )
    }
}
