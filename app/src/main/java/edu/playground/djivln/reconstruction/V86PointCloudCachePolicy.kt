package edu.playground.djivln.reconstruction

import java.io.File

object V86PointCloudCachePolicy {
    fun revision(snapshot: V86StreamingController.Snapshot): String {
        require(snapshot.sessionId != null)
        val completed = snapshot.result?.completed == true || snapshot.session?.completed == true
        return if (completed) "final" else "preview-${snapshot.session?.imageCount ?: 0}-" +
            "${snapshot.session?.scal3rLaneCompletedWindows ?: 0}"
    }

    fun fileStem(snapshot: V86StreamingController.Snapshot): String {
        val id = requireNotNull(snapshot.sessionId)
        require(id.matches(Regex("[a-z0-9][a-z0-9_-]{5,63}")))
        return "$id-${revision(snapshot)}"
    }

    fun isUsablePly(file: File): Boolean = file.isFile && file.length() >= MINIMUM_PLY_BYTES &&
        runCatching {
            file.inputStream().buffered().use { input ->
                val prefix = ByteArray(PLY_PREFIX_BYTES)
                val count = input.read(prefix)
                count > 0 && prefix.copyOf(count).toString(Charsets.US_ASCII).startsWith("ply\n")
            }
        }.getOrDefault(false)

    fun prune(root: File, currentStem: String, maximumPlyFiles: Int = 3,
              maximumTotalBytes: Long = 768L * 1024L * 1024L) {
        if (!root.isDirectory) return
        val clouds = root.listFiles { file -> file.isFile && file.name.endsWith(".ply") }
            .orEmpty().sortedByDescending(File::lastModified).toMutableList()
        fun remove(cloud: File) {
            val stem = cloud.name.removeSuffix(".ply")
            cloud.delete()
            File(root, "$stem-viewer.json").delete()
            clouds.remove(cloud)
        }
        clouds.filter { it.name.removeSuffix(".ply") != currentStem }
            .drop((maximumPlyFiles - 1).coerceAtLeast(0))
            .toList().forEach(::remove)
        while (clouds.sumOf(File::length) > maximumTotalBytes) {
            val oldest = clouds.asReversed().firstOrNull {
                it.name.removeSuffix(".ply") != currentStem
            } ?: break
            remove(oldest)
        }
    }

    private const val MINIMUM_PLY_BYTES = 16L
    private const val PLY_PREFIX_BYTES = 16
}
