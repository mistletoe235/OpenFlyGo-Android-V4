package edu.playground.djivln.survey

import android.content.Context
import edu.playground.djivln.mini2.R
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class TerrainCoverageReport(
    val boundsCoverRoi: Boolean,
    val sampledPointCount: Int,
    val missingSampleCount: Int,
) {
    val complete: Boolean get() = boundsCoverRoi && sampledPointCount > 0 && missingSampleCount == 0
}

object TerrainImportSafety {
    const val MAX_IMPORT_BYTES = 96L * 1024L * 1024L
    private const val GRID_COLUMNS = 16
    private const val GRID_ROWS = 16
    private const val EDGE_SAMPLES = 8

    fun readBounded(
        input: InputStream,
        maximumBytes: Long = MAX_IMPORT_BYTES,
        context: Context? = null,
    ): ByteArray {
        require(maximumBytes > 0L)
        val output = ByteArrayOutputStream(minOf(maximumBytes, 64L * 1024L).toInt())
        val buffer = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (count == 0) continue
            total += count
            require(total <= maximumBytes) {
                context?.getString(R.string.terrain_file_mobile_memory_limit,
                    maximumBytes / (1024L * 1024L))
                    ?: "Elevation file exceeds the mobile memory safety limit"
            }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    fun requireAggregateWithinLimit(
        currentBytes: Long,
        nextBytes: Long,
        maximumBytes: Long = MAX_IMPORT_BYTES,
        message: () -> String = { "Elevation tiles exceed the mobile memory safety limit" },
    ): Long {
        require(currentBytes >= 0L && nextBytes >= 0L && maximumBytes > 0L)
        require(currentBytes <= maximumBytes - nextBytes) { message() }
        return currentBytes + nextBytes
    }

    fun inspectCoverage(source: TerrainElevationSource, roi: List<GeoPoint>): TerrainCoverageReport {
        require(roi.size >= 3)
        val info = source.info
        fun inBounds(point: GeoPoint) = point.latitude >= info.minimumLatitude - 1e-9 &&
            point.latitude <= info.maximumLatitude + 1e-9 &&
            point.longitude >= info.minimumLongitude - 1e-9 &&
            point.longitude <= info.maximumLongitude + 1e-9
        if (!roi.all(::inBounds)) return TerrainCoverageReport(false, 0, 0)

        val samples = LinkedHashSet<Pair<Double, Double>>()
        roi.forEach { samples += it.latitude to it.longitude }
        roi.indices.forEach { index ->
            val from = roi[index]
            val to = roi[(index + 1) % roi.size]
            for (step in 1 until EDGE_SAMPLES) {
                val ratio = step.toDouble() / EDGE_SAMPLES
                samples += (from.latitude + (to.latitude - from.latitude) * ratio) to
                    (from.longitude + (to.longitude - from.longitude) * ratio)
            }
        }
        val minLat = roi.minOf { it.latitude }
        val maxLat = roi.maxOf { it.latitude }
        val minLon = roi.minOf { it.longitude }
        val maxLon = roi.maxOf { it.longitude }
        for (row in 0 until GRID_ROWS) for (column in 0 until GRID_COLUMNS) {
            val latitude = minLat + (maxLat - minLat) * (row + 0.5) / GRID_ROWS
            val longitude = minLon + (maxLon - minLon) * (column + 0.5) / GRID_COLUMNS
            if (insidePolygon(latitude, longitude, roi)) samples += latitude to longitude
        }
        val missing = samples.count { (latitude, longitude) ->
            runCatching { source.elevationMeters(latitude, longitude) }
                .getOrNull()?.isFinite() != true
        }
        return TerrainCoverageReport(true, samples.size, missing)
    }

    fun requireCompleteCoverage(
        source: TerrainElevationSource,
        roi: List<GeoPoint>,
        context: Context? = null,
    ): TerrainCoverageReport {
        val report = inspectCoverage(source, roi)
        require(report.boundsCoverRoi) {
            context?.getString(R.string.terrain_roi_not_fully_covered)
                ?: "Elevation data does not fully cover the survey area"
        }
        require(report.sampledPointCount > 0 && report.missingSampleCount == 0) {
            context?.getString(R.string.terrain_roi_nodata_gaps,
                report.missingSampleCount, report.sampledPointCount)
                ?: "Survey elevation contains NoData or unreadable gaps"
        }
        return report
    }

    private fun insidePolygon(latitude: Double, longitude: Double, polygon: List<GeoPoint>): Boolean {
        var inside = false
        var previous = polygon.lastIndex
        polygon.indices.forEach { index ->
            val a = polygon[index]
            val b = polygon[previous]
            if ((a.latitude > latitude) != (b.latitude > latitude) &&
                longitude < (b.longitude - a.longitude) * (latitude - a.latitude) /
                (b.latitude - a.latitude) + a.longitude
            ) inside = !inside
            previous = index
        }
        return inside
    }
}
