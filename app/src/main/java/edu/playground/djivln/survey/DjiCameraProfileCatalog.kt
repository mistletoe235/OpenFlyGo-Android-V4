package edu.playground.djivln.survey

import android.content.Context
import edu.playground.djivln.mini2.R
import kotlin.math.atan
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Survey-camera geometry derived from DJI's published product specifications.
 * DJI publishes one FOV; horizontal/vertical values are derived from that FOV
 * and the published still-image aspect ratio.
 */
object DjiCameraProfileCatalog {
    data class Resolution(
        val profile: CameraProfile,
        val displayName: String,
        val officialSourceUrl: String?,
        val verifiedProfile: Boolean,
    )

    private data class Entry(
        val aliases: Set<String>,
        val displayNameRes: Int,
        val defaultDisplayName: String,
        val profile: CameraProfile,
        val officialSourceUrl: String,
    )

    private const val MINI_2_SOURCE = "https://www.dji.com/support/product/mini-2"
    private const val AIR_2_SOURCE = "https://www.dji.com/uk/mavic-air-2/specs"
    private const val AIR_2S_SOURCE = "https://www.dji.com/support/product/air-2s"
    private const val MINI_3_SOURCE = "https://www.dji.com/mini-3/specs"
    private const val MINI_3_PRO_SOURCE = "https://www.dji.com/support/product/mini-3-pro"
    private const val MINI_4_PRO_SOURCE = "https://www.dji.com/mini-4-pro/specs"
    private const val M30_SOURCE = "https://enterprise.dji.com/matrice-30/specs"
    private const val M3_ENTERPRISE_SOURCE = "https://enterprise.dji.com/mavic-3-enterprise/specs"
    private const val M3M_SOURCE = "https://enterprise.dji.com/mavic-3-m/specs"
    private const val MATRICE_4_SOURCE = "https://enterprise.dji.com/matrice-4-series/specs"
    private const val MAVIC_2_SOURCE = "https://www.dji.com/mavic-2/info"
    private const val PHANTOM_4_PRO_SOURCE = "https://www.dji.com/support/product/phantom-4-pro-v2"

    private val m30WideEntry = entry(
        setOf("M30", "M30T", "M30SERIES"),
        R.string.camera_matrice_30_wide,
        "DJI Matrice 30 Series Wide",
        "dji-matrice-30-wide-12mp",
        4000, 3000, 84.0, 2.0, M30_SOURCE,
    )

    private val entries = listOf(
        entry(setOf("M4E"), R.string.camera_matrice_4e_wide, "DJI Matrice 4E Wide", "dji-matrice-4e-wide-20mp", 5280, 3956, 84.0, 0.5, MATRICE_4_SOURCE),
        entry(setOf("M4T"), R.string.camera_matrice_4t_wide, "DJI Matrice 4T Wide", "dji-matrice-4t-wide-48mp", 8064, 6048, 82.0, 0.7, MATRICE_4_SOURCE),
        entry(setOf("M3E"), R.string.camera_mavic_3e_wide, "DJI Mavic 3E Wide", "dji-mavic-3e-wide-20mp", 5280, 3956, 84.0, 0.7, M3_ENTERPRISE_SOURCE),
        entry(setOf("M3T", "M3TA"), R.string.camera_mavic_3t_wide, "DJI Mavic 3T/3TA Wide", "dji-mavic-3t-wide-12mp", 4000, 3000, 84.0, 2.0, M3_ENTERPRISE_SOURCE),
        entry(setOf("M3M"), R.string.camera_mavic_3m_rgb, "DJI Mavic 3M RGB", "dji-mavic-3m-rgb-20mp", 5280, 3956, 84.0, 0.7, M3M_SOURCE),
        entry(setOf("DJIMINI4PRO"), R.string.camera_mini_4_pro_12mp, "DJI Mini 4 Pro 12MP", "dji-mini-4-pro-photo-12mp", 4032, 3024, 82.1, 2.0, MINI_4_PRO_SOURCE),
        entry(setOf("DJIMINI3PRO"), R.string.camera_mini_3_pro_12mp, "DJI Mini 3 Pro 12MP", "dji-mini-3-pro-photo-12mp", 4032, 3024, 82.1, 2.0, MINI_3_PRO_SOURCE),
        entry(setOf("DJIMINI3"), R.string.camera_mini_3_12mp, "DJI Mini 3 12MP", "dji-mini-3-photo-12mp", 4032, 3024, 82.1, 2.0, MINI_3_SOURCE),
        entry(setOf("DJIAIR2S", "MAVICAIR2S"), R.string.camera_air_2s_20mp, "DJI Air 2S 20MP", "dji-air-2s-photo-20mp", 5472, 3648, 88.0, 2.0, AIR_2S_SOURCE),
        entry(setOf("MAVIC2PRO"), R.string.camera_mavic_2_pro, "DJI Mavic 2 Pro", "dji-mavic-2-pro-photo-20mp", 5472, 3648, 77.0, 2.0, MAVIC_2_SOURCE),
        entry(setOf("MAVIC2ZOOM"), R.string.camera_mavic_2_zoom_wide, "DJI Mavic 2 Zoom Wide", "dji-mavic-2-zoom-wide-photo-12mp", 4000, 3000, 83.0, 2.0, MAVIC_2_SOURCE),
        entry(setOf("PHANTOM4PRO", "PHANTOM4ADVANCED", "P4PV2CAMERA"), R.string.camera_phantom_4_pro_advanced, "DJI Phantom 4 Pro/Advanced", "dji-phantom-4-pro-photo-20mp", 5472, 3648, 84.0, 2.0, PHANTOM_4_PRO_SOURCE),
        entry(setOf("MAVICAIR2"), R.string.camera_mavic_air_2_12mp, "DJI Mavic Air 2 12MP", "dji-mavic-air-2-photo-12mp", 4000, 3000, 84.0, 2.0, AIR_2_SOURCE),
        entry(setOf("DJIMINI2", "MAVICMINI2", "DJIMINISE", "MAVICMINI"), R.string.camera_mini_series_12mp, "DJI Mini Series 12MP", "dji-mini-2-photo-4x3", 4000, 3000, 83.0, 2.0, MINI_2_SOURCE),
    )

    fun resolve(vararg identities: String?): Resolution = resolveLocalized(null, *identities)

    fun resolveLocalized(context: Context?, vararg identities: String?): Resolution {
        val meaningfulIdentities = identities.filterNotNull()
            .map(String::trim)
            .filter(String::isNotBlank)
            .filterNot { normalize(it) in SDK_SENTINEL_IDENTITIES }
        val normalized = meaningfulIdentities.joinToString(" ") { normalize(it) }
        val normalizedIdentities = meaningfulIdentities.map(::normalize)
        if (normalizedIdentities.any(M30_ALIASES::contains)) {
            return resolveM30(normalized, context)
        }
        val match = entries.firstOrNull { entry -> entry.aliases.any(normalized::contains) }
        return if (match != null) {
            Resolution(
                match.profile,
                context?.getString(match.displayNameRes) ?: match.defaultDisplayName,
                match.officialSourceUrl,
                true,
            )
        } else {
            Resolution(
                CameraProfile.GENERIC_4_BY_3,
                meaningfulIdentities.joinToString(" / ").ifBlank {
                    context?.getString(R.string.camera_unrecognized) ?: "Unrecognized camera"
                },
                null,
                false,
            )
        }
    }

    fun allVerified(context: Context? = null): List<Resolution> = entries.map {
        Resolution(it.profile, context?.getString(it.displayNameRes) ?: it.defaultDisplayName, it.officialSourceUrl, true)
    } + Resolution(
        m30WideEntry.profile,
        context?.getString(m30WideEntry.displayNameRes) ?: m30WideEntry.defaultDisplayName,
        m30WideEntry.officialSourceUrl,
        true,
    )

    private fun resolveM30(normalized: String, context: Context?): Resolution = when {
        normalized.contains("WIDECAMERA") -> Resolution(
            m30WideEntry.profile,
            context?.getString(m30WideEntry.displayNameRes) ?: m30WideEntry.defaultDisplayName,
            m30WideEntry.officialSourceUrl,
            true,
        )
        normalized.contains("ZOOMCAMERA") -> unverifiedM30(
            context?.getString(R.string.camera_matrice_30_zoom_uncalibrated)
                ?: "DJI Matrice 30 Series Zoom (variable focal length; calibration required)",
        )
        normalized.contains("INFRAREDCAMERA") || normalized.contains("THERMAL") -> unverifiedM30(
            context?.getString(R.string.camera_matrice_30t_thermal_unverified)
                ?: "DJI Matrice 30T Thermal (verify resolution mode)",
        )
        else -> unverifiedM30(
            context?.getString(R.string.camera_matrice_30_lens_unverified)
                ?: "DJI Matrice 30 Series (select and verify the lens)",
        )
    }

    private fun unverifiedM30(label: String) = Resolution(
        CameraProfile.GENERIC_4_BY_3,
        label,
        M30_SOURCE,
        false,
    )

    private fun entry(
        aliases: Set<String>, nameRes: Int, defaultName: String, id: String, width: Int, height: Int,
        diagonalFovDegrees: Double, minimumIntervalSeconds: Double, source: String,
    ): Entry {
        val diagonalTangent = tan(Math.toRadians(diagonalFovDegrees / 2.0))
        val aspect = width.toDouble() / height
        val verticalHalf = atan(diagonalTangent / sqrt(aspect * aspect + 1.0))
        val horizontalHalf = atan(aspect * tan(verticalHalf))
        return Entry(
            aliases.map(::normalize).toSet(),
            nameRes,
            defaultName,
            CameraProfile(
                id, width, height,
                Math.toDegrees(horizontalHalf * 2.0),
                Math.toDegrees(verticalHalf * 2.0),
                minimumIntervalSeconds,
            ),
            source,
        )
    }

    private fun normalize(value: String): String = value.uppercase().filter(Char::isLetterOrDigit)

    private val SDK_SENTINEL_IDENTITIES = setOf(
        "UNKNOWN",
        "NOTSUPPORTED",
        "NONE",
        "DEFAULT",
        "OTHER",
    )

    private val M30_ALIASES = setOf(
        "M30", "M30T", "M30SERIES", "DJIM30", "DJIM30T",
        "MATRICE30", "MATRICE30SERIES", "DJIMATRICE30SERIES",
    )
}
