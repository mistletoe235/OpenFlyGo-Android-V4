package edu.playground.djivln.survey

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Boundary between DJI/WGS-84 telemetry and mainland-China GCJ-02 map coordinates.
 *
 * All persisted missions stay in WGS-84. Conversion is only applied while
 * rendering map coordinates or accepting taps from the Baidu map view configured for GCJ-02.
 */
object ChinaCoordinateTransform {
    private const val SEMI_MAJOR_AXIS = 6378245.0
    private const val ECCENTRICITY_SQUARED = 0.00669342162296594323

    fun wgs84ToGcj02(point: GeoPoint): GeoPoint {
        if (outsideMainlandChina(point.latitude, point.longitude)) return point
        val delta = delta(point.latitude, point.longitude)
        return point.copy(
            latitude = point.latitude + delta.first,
            longitude = point.longitude + delta.second,
        )
    }

    fun gcj02ToWgs84(point: GeoPoint): GeoPoint {
        if (outsideMainlandChina(point.latitude, point.longitude)) return point
        // Fixed-point refinement is more accurate than subtracting one forward delta.
        var estimateLatitude = point.latitude
        var estimateLongitude = point.longitude
        repeat(6) {
            val projected = wgs84ToGcj02(GeoPoint(estimateLatitude, estimateLongitude, point.altitudeMeters))
            estimateLatitude -= projected.latitude - point.latitude
            estimateLongitude -= projected.longitude - point.longitude
        }
        return GeoPoint(estimateLatitude, estimateLongitude, point.altitudeMeters)
    }

    fun outsideMainlandChina(latitude: Double, longitude: Double): Boolean =
        longitude < 72.004 || longitude > 137.8347 || latitude < 0.8293 || latitude > 55.8271

    private fun delta(latitude: Double, longitude: Double): Pair<Double, Double> {
        var latitudeOffset = transformLatitude(longitude - 105.0, latitude - 35.0)
        var longitudeOffset = transformLongitude(longitude - 105.0, latitude - 35.0)
        val latitudeRadians = Math.toRadians(latitude)
        var magic = sin(latitudeRadians)
        magic = 1.0 - ECCENTRICITY_SQUARED * magic * magic
        val sqrtMagic = sqrt(magic)
        latitudeOffset = latitudeOffset * 180.0 /
            ((SEMI_MAJOR_AXIS * (1.0 - ECCENTRICITY_SQUARED)) / (magic * sqrtMagic) * Math.PI)
        longitudeOffset = longitudeOffset * 180.0 /
            (SEMI_MAJOR_AXIS / sqrtMagic * cos(latitudeRadians) * Math.PI)
        return latitudeOffset to longitudeOffset
    }

    private fun transformLatitude(x: Double, y: Double): Double {
        var result = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y +
            0.2 * sqrt(abs(x))
        result += (20.0 * sin(6.0 * x * Math.PI) + 20.0 * sin(2.0 * x * Math.PI)) * 2.0 / 3.0
        result += (20.0 * sin(y * Math.PI) + 40.0 * sin(y / 3.0 * Math.PI)) * 2.0 / 3.0
        result += (160.0 * sin(y / 12.0 * Math.PI) + 320.0 * sin(y * Math.PI / 30.0)) * 2.0 / 3.0
        return result
    }

    private fun transformLongitude(x: Double, y: Double): Double {
        var result = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y +
            0.1 * sqrt(abs(x))
        result += (20.0 * sin(6.0 * x * Math.PI) + 20.0 * sin(2.0 * x * Math.PI)) * 2.0 / 3.0
        result += (20.0 * sin(x * Math.PI) + 40.0 * sin(x / 3.0 * Math.PI)) * 2.0 / 3.0
        result += (150.0 * sin(x / 12.0 * Math.PI) + 300.0 * sin(x / 30.0 * Math.PI)) * 2.0 / 3.0
        return result
    }
}
