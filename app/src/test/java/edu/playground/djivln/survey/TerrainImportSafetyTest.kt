package edu.playground.djivln.survey

import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TerrainImportSafetyTest {
    private val roi = listOf(
        GeoPoint(31.00, 121.00), GeoPoint(31.01, 121.00),
        GeoPoint(31.01, 121.01), GeoPoint(31.00, 121.01),
    )

    @Test fun `bounded reader rejects one byte beyond limit`() {
        assertThrows(IllegalArgumentException::class.java) {
            TerrainImportSafety.readBounded(CountingInput(1_025), maximumBytes = 1_024)
        }
        assertEquals(1_024, TerrainImportSafety.readBounded(
            CountingInput(1_024), maximumBytes = 1_024).size)
    }

    @Test fun `coverage checks bounds and nodata gaps`() {
        val valid = fakeTerrain { _, _ -> 8.0 }
        assertTrue(TerrainImportSafety.requireCompleteCoverage(valid, roi).complete)

        val gap = fakeTerrain { latitude, _ -> if (latitude > 31.005) Double.NaN else 8.0 }
        assertThrows(IllegalArgumentException::class.java) {
            TerrainImportSafety.requireCompleteCoverage(gap, roi)
        }
    }

    @Test fun `aggregate byte accounting rejects the first byte beyond the shared cap`() {
        assertEquals(1_024L, TerrainImportSafety.requireAggregateWithinLimit(512, 512, 1_024))
        assertThrows(IllegalArgumentException::class.java) {
            TerrainImportSafety.requireAggregateWithinLimit(512, 513, 1_024)
        }
    }

    private fun fakeTerrain(elevation: (Double, Double) -> Double) = object : TerrainElevationSource {
        override val info = TerrainRasterInfo("test", 10, 10, 4326, null, 0.001, 0.001,
            30.99, 31.02, 120.99, 121.02)
        override fun elevationMeters(latitude: Double, longitude: Double) = elevation(latitude, longitude)
    }

    private class CountingInput(private val length: Int) : InputStream() {
        private var offset = 0
        override fun read(): Int = if (offset++ < length) 0 else -1
        override fun read(buffer: ByteArray, off: Int, len: Int): Int {
            if (offset >= length) return -1
            val count = minOf(len, length - offset)
            java.util.Arrays.fill(buffer, off, off + count, 0.toByte())
            offset += count
            return count
        }
    }
}
