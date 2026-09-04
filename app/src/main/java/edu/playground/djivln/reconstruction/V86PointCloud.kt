package edu.playground.djivln.reconstruction

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class V86PointCloud(
    val xyz: FloatArray,
    val colors: IntArray,
    val candidates: List<V86Candidate> = emptyList(),
) { val size: Int get() = colors.size }

object V86PlyDecoder {
    fun decode(file: File, candidates: List<V86Candidate> = emptyList(),
               maxPoints: Int = 30_000): V86PointCloud {
        require(file.isFile && file.length() >= 64L) { "PLY file is missing or too small" }
        RandomAccessFile(file, "r").use { input ->
            require(input.readHeaderLine() == "ply") { "Not a PLY file" }
            require(input.readHeaderLine() == "format binary_little_endian 1.0") {
                "Only binary_little_endian PLY is supported"
            }
            var vertexCount: Int? = null
            val properties = mutableListOf<List<String>>()
            var readingVertex = false
            while (true) {
                val line = input.readHeaderLine() ?: error("PLY is missing end_header")
                if (line == "end_header") break
                if (line.startsWith("element vertex ")) {
                    vertexCount = line.substringAfterLast(' ').toIntOrNull()
                    readingVertex = true
                } else if (line.startsWith("element ")) readingVertex = false
                else if (readingVertex && line.startsWith("property ")) {
                    properties += line.trim().split(Regex("\\s+"))
                }
            }
            val count = vertexCount ?: error("PLY is missing vertex count")
            require(count > 0) { "PLY contains no vertices" }
            require(properties == SUPPORTED_PROPERTIES) { "PLY fields do not match XYZ+RGB contract" }
            val dataOffset = input.filePointer
            require(dataOffset + count.toLong() * VERTEX_STRIDE <= file.length()) {
                "PLY vertex data is incomplete"
            }
            val target = minOf(count, maxPoints.coerceAtLeast(1))
            val step = maxOf(1, (count + target - 1) / target)
            val capacity = (count + step - 1) / step
            val xyz = FloatArray(capacity * 3)
            val colors = IntArray(capacity)
            var output = 0
            var source = 0
            input.seek(dataOffset)
            while (source < count && output < capacity) {
                xyz[output * 3] = input.readLittleEndianFloat()
                xyz[output * 3 + 1] = input.readLittleEndianFloat()
                xyz[output * 3 + 2] = input.readLittleEndianFloat()
                val red = input.readUnsignedByte()
                val green = input.readUnsignedByte()
                val blue = input.readUnsignedByte()
                colors[output] = 0xff000000.toInt() or (red shl 16) or (green shl 8) or blue
                output++
                val skip = minOf(step - 1, count - source - 1)
                if (skip > 0) input.seek(input.filePointer + skip.toLong() * VERTEX_STRIDE)
                source += step
            }
            return V86PointCloud(
                if (output == capacity) xyz else xyz.copyOf(output * 3),
                if (output == capacity) colors else colors.copyOf(output), candidates)
        }
    }

    fun decode(bytes: ByteArray, candidates: List<V86Candidate> = emptyList(),
               maxPoints: Int = 30_000): V86PointCloud {
        require(bytes.size >= 64) { "PLY file is too small" }
        val marker = "end_header\n".toByteArray(Charsets.US_ASCII)
        val headerEnd = bytes.indexOf(marker)
        require(headerEnd >= 0) { "PLY is missing end_header" }
        val dataOffset = headerEnd + marker.size
        val header = bytes.copyOfRange(0, dataOffset).toString(Charsets.US_ASCII)
        require(header.startsWith("ply\n")) { "Not a PLY file" }
        require("format binary_little_endian 1.0" in header) {
            "Only binary_little_endian PLY is supported"
        }
        val count = Regex("element vertex (\\d+)").find(header)
            ?.groupValues?.get(1)?.toIntOrNull() ?: error("PLY is missing vertex count")
        require(count > 0) { "PLY contains no vertices" }
        val properties = header.lineSequence().dropWhile { !it.startsWith("element vertex ") }
            .drop(1).takeWhile { !it.startsWith("element ") && it != "end_header" }
            .filter { it.startsWith("property ") }.map { it.trim().split(Regex("\\s+")) }.toList()
        require(properties == SUPPORTED_PROPERTIES) { "PLY fields do not match XYZ+RGB contract" }
        require(dataOffset + count.toLong() * VERTEX_STRIDE <= bytes.size.toLong()) {
            "PLY vertex data is incomplete"
        }
        val target = minOf(count, maxPoints.coerceAtLeast(1))
        val step = maxOf(1, (count + target - 1) / target)
        val capacity = (count + step - 1) / step
        val xyz = FloatArray(capacity * 3)
        val colors = IntArray(capacity)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        var output = 0
        var source = 0
        while (source < count && output < capacity) {
            buffer.position(dataOffset + source * VERTEX_STRIDE)
            xyz[output * 3] = buffer.float
            xyz[output * 3 + 1] = buffer.float
            xyz[output * 3 + 2] = buffer.float
            val red = buffer.get().toInt() and 0xff
            val green = buffer.get().toInt() and 0xff
            val blue = buffer.get().toInt() and 0xff
            colors[output] = 0xff000000.toInt() or (red shl 16) or (green shl 8) or blue
            output++
            source += step
        }
        return V86PointCloud(if (output == capacity) xyz else xyz.copyOf(output * 3),
            if (output == capacity) colors else colors.copyOf(output), candidates)
    }

    private fun ByteArray.indexOf(needle: ByteArray): Int {
        outer@ for (index in 0..minOf(size, MAX_HEADER_BYTES) - needle.size) {
            for (offset in needle.indices) if (this[index + offset] != needle[offset]) continue@outer
            return index
        }
        return -1
    }
    private fun RandomAccessFile.readLittleEndianFloat() = Float.fromBits(Integer.reverseBytes(readInt()))
    private fun RandomAccessFile.readHeaderLine(): String? {
        val line = StringBuilder()
        while (true) {
            require(filePointer < MAX_HEADER_BYTES) { "PLY header exceeds size limit" }
            val byte = read()
            if (byte < 0) return if (line.isEmpty()) null else line.toString().removeSuffix("\r")
            if (byte == 10) return line.toString().removeSuffix("\r")
            require(line.length < 4096) { "PLY header line exceeds size limit" }
            line.append(byte.toChar())
        }
    }
    private const val MAX_HEADER_BYTES = 64 * 1024
    private const val VERTEX_STRIDE = 15
    private val SUPPORTED_PROPERTIES = listOf(
        listOf("property", "float", "x"), listOf("property", "float", "y"),
        listOf("property", "float", "z"), listOf("property", "uchar", "red"),
        listOf("property", "uchar", "green"), listOf("property", "uchar", "blue"),
    )
}
