package edu.playground.djivln.camera

/** Conservative MSDK4 camera selection and primary-feed association policy. */
object SurveyCameraSourcePolicy {
    data class Candidate(val connected: Boolean, val thermal: Boolean)

    fun selectedIndex(candidates: List<Candidate>): Int? =
        candidates.indices.firstOrNull { candidates[it].connected && !candidates[it].thermal }
            ?: candidates.indices.firstOrNull { candidates[it].connected }

    fun canAssociatePrimaryFeed(
        candidates: List<Candidate>,
        selectedIndex: Int?,
        profileVerified: Boolean,
        vararg productIdentities: String?,
    ): Boolean {
        if (!profileVerified || selectedIndex == null) return false
        val connected = candidates.indices.filter { candidates[it].connected }
        if (connected.size != 1 || connected.single() != selectedIndex) return false
        if (candidates[selectedIndex].thermal) return false
        return productIdentities.none { isKnownEnterpriseMultiLensProduct(it) }
    }

    private fun isKnownEnterpriseMultiLensProduct(value: String?): Boolean {
        val normalized = value.orEmpty().uppercase().filter(Char::isLetterOrDigit)
        return normalized in setOf(
            "M30", "M30T", "M30SERIES", "DJIM30", "DJIM30T",
            "MATRICE30", "MATRICE30SERIES", "DJIMATRICE30SERIES",
            "M3E", "M3T", "M3TA", "M3M",
            "M4E", "M4T",
        ) || normalized.contains("MAVIC3ENTERPRISE") ||
            normalized.contains("MAVIC3THERMAL") ||
            normalized.contains("MAVIC3MULTISPECTRAL") ||
            normalized.contains("MATRICE4")
    }
}
