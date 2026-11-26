package com.musetool.superapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.musetool.superapp.R

val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

// Restrained, editorial financial palette (no neon/cyber clashes)
val CyberCyan = Color(0xFF64748B)
val ElectricViolet = Color(0xFF475569)
val EmeraldCredit = Color(0xFF10B981)
val AmberWarning = Color(0xFFD97706)
val RoseDebt = Color(0xFFE11D48)

enum class MuseThemePreset(
    val id: String,
    val titleVi: String,
    val modeLabel: String,
    val isDark: Boolean,
    val previewBgHex: Long,
    val previewCardHex: Long,
    val previewAccentHex: Long
) {
    OBSIDIAN_MONO(
        id = "OBSIDIAN_MONO",
        titleVi = "Obsidian Mono",
        modeLabel = "Tối · Đen Tuyền",
        isDark = true,
        previewBgHex = 0xFF0B0C0E,
        previewCardHex = 0xFF131519,
        previewAccentHex = 0xFFF4F4F5
    ),
    MYSTIC_BRONZE_DARK(
        id = "MYSTIC_BRONZE_DARK",
        titleVi = "Mystic Bronze",
        modeLabel = "Tối · Note20U",
        isDark = true,
        previewBgHex = 0xFF100E0D,
        previewCardHex = 0xFF1A1615,
        previewAccentHex = 0xFFD4A373
    ),
    NORDIC_SLATE_DARK(
        id = "NORDIC_SLATE_DARK",
        titleVi = "Nordic Titanium",
        modeLabel = "Tối · Xám Đá",
        isDark = true,
        previewBgHex = 0xFF0D1117,
        previewCardHex = 0xFF161B22,
        previewAccentHex = 0xFFE2E8F0
    ),
    CARBON_SAGE_DARK(
        id = "CARBON_SAGE_DARK",
        titleVi = "Carbon Forest",
        modeLabel = "Tối · Xanh Rêu",
        isDark = true,
        previewBgHex = 0xFF0B0F0E,
        previewCardHex = 0xFF131A17,
        previewAccentHex = 0xFF6EE7B7
    ),
    ALABASTER_LIGHT(
        id = "ALABASTER_LIGHT",
        titleVi = "Alabaster Pure",
        modeLabel = "Sáng · Trắng Tinh",
        isDark = false,
        previewBgHex = 0xFFF8F9FA,
        previewCardHex = 0xFFFFFFFF,
        previewAccentHex = 0xFF111317
    ),
    WARM_IVORY_LIGHT(
        id = "WARM_IVORY_LIGHT",
        titleVi = "Warm Ivory",
        modeLabel = "Sáng · Giấy Kem",
        isDark = false,
        previewBgHex = 0xFFF6F4F0,
        previewCardHex = 0xFFFFFFFF,
        previewAccentHex = 0xFF292524
    ),
    PLATINUM_ICE_LIGHT(
        id = "PLATINUM_ICE_LIGHT",
        titleVi = "Platinum Silver",
        modeLabel = "Sáng · Xám Bạc",
        isDark = false,
        previewBgHex = 0xFFF1F5F9,
        previewCardHex = 0xFFFFFFFF,
        previewAccentHex = 0xFF0F172A
    ),
    SAGE_DAY_LIGHT(
        id = "SAGE_DAY_LIGHT",
        titleVi = "Sage Daylight",
        modeLabel = "Sáng · Dịu Mắt",
        isDark = false,
        previewBgHex = 0xFFF2F6F4,
        previewCardHex = 0xFFFFFFFF,
        previewAccentHex = 0xFF134E4A
    );

    companion object {
        fun fromId(id: String?): MuseThemePreset =
            entries.firstOrNull { it.id == id } ?: OBSIDIAN_MONO
    }
}

fun MuseThemePreset.toColorScheme(): ColorScheme {
    return when (this) {
        MuseThemePreset.OBSIDIAN_MONO -> darkColorScheme(
            primary = Color(0xFFF4F4F5),
            onPrimary = Color(0xFF0B0C0E),
            primaryContainer = Color(0xFF22252A),
            onPrimaryContainer = Color(0xFFFAFAFA),
            secondary = Color(0xFFA1A1AA),
            onSecondary = Color(0xFF0B0C0E),
            secondaryContainer = Color(0xFF1A1C20),
            onSecondaryContainer = Color(0xFFE4E4E7),
            tertiary = Color(0xFF10B981),
            onTertiary = Color(0xFF052E16),
            error = Color(0xFFF43F5E),
            background = Color(0xFF0B0C0E),
            onBackground = Color(0xFFF4F4F5),
            surface = Color(0xFF121418),
            onSurface = Color(0xFFF4F4F5),
            surfaceVariant = Color(0xFF181B20),
            onSurfaceVariant = Color(0xFF8E95A2),
            outline = Color(0xFF242830)
        )

        MuseThemePreset.MYSTIC_BRONZE_DARK -> darkColorScheme(
            primary = Color(0xFFD4A373),
            onPrimary = Color(0xFF17120E),
            primaryContainer = Color(0xFF2D231E),
            onPrimaryContainer = Color(0xFFF7EDE6),
            secondary = Color(0xFFB5A49B),
            onSecondary = Color(0xFF17120E),
            secondaryContainer = Color(0xFF221C19),
            onSecondaryContainer = Color(0xFFEAE0DA),
            tertiary = Color(0xFF10B981),
            onTertiary = Color(0xFF052E16),
            error = Color(0xFFF43F5E),
            background = Color(0xFF0F0D0C),
            onBackground = Color(0xFFF7F4F1),
            surface = Color(0xFF171412),
            onSurface = Color(0xFFF7F4F1),
            surfaceVariant = Color(0xFF201B19),
            onSurfaceVariant = Color(0xFFA3968F),
            outline = Color(0xFF302825)
        )

        MuseThemePreset.NORDIC_SLATE_DARK -> darkColorScheme(
            primary = Color(0xFFE2E8F0),
            onPrimary = Color(0xFF0D1117),
            primaryContainer = Color(0xFF212832),
            onPrimaryContainer = Color(0xFFF8FAFC),
            secondary = Color(0xFF94A3B8),
            onSecondary = Color(0xFF0D1117),
            secondaryContainer = Color(0xFF1A2029),
            onSecondaryContainer = Color(0xFFE2E8F0),
            tertiary = Color(0xFF10B981),
            onTertiary = Color(0xFF052E16),
            error = Color(0xFFF43F5E),
            background = Color(0xFF0D1117),
            onBackground = Color(0xFFF1F5F9),
            surface = Color(0xFF141921),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF1B222C),
            onSurfaceVariant = Color(0xFF8B98A9),
            outline = Color(0xFF28313E)
        )

        MuseThemePreset.CARBON_SAGE_DARK -> darkColorScheme(
            primary = Color(0xFFA7F3D0),
            onPrimary = Color(0xFF081C15),
            primaryContainer = Color(0xFF182C25),
            onPrimaryContainer = Color(0xFFECFDF5),
            secondary = Color(0xFF95A8A0),
            onSecondary = Color(0xFF081C15),
            secondaryContainer = Color(0xFF17221E),
            onSecondaryContainer = Color(0xFFD8E6E0),
            tertiary = Color(0xFF10B981),
            onTertiary = Color(0xFF052E16),
            error = Color(0xFFF43F5E),
            background = Color(0xFF0B0F0D),
            onBackground = Color(0xFFF0F7F4),
            surface = Color(0xFF121816),
            onSurface = Color(0xFFF0F7F4),
            surfaceVariant = Color(0xFF18201D),
            onSurfaceVariant = Color(0xFF899B93),
            outline = Color(0xFF25322D)
        )

        MuseThemePreset.ALABASTER_LIGHT -> lightColorScheme(
            primary = Color(0xFF18181B),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE4E4E7),
            onPrimaryContainer = Color(0xFF18181B),
            secondary = Color(0xFF52525B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF4F4F5),
            onSecondaryContainer = Color(0xFF27272A),
            tertiary = Color(0xFF059669),
            onTertiary = Color(0xFFFFFFFF),
            error = Color(0xFFE11D48),
            background = Color(0xFFF6F7F9),
            onBackground = Color(0xFF111317),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF111317),
            surfaceVariant = Color(0xFFEEF0F4),
            onSurfaceVariant = Color(0xFF64748B),
            outline = Color(0xFFE2E6ED)
        )

        MuseThemePreset.WARM_IVORY_LIGHT -> lightColorScheme(
            primary = Color(0xFF292524),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE7E2DA),
            onPrimaryContainer = Color(0xFF1C1917),
            secondary = Color(0xFF78716C),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF1EDE7),
            onSecondaryContainer = Color(0xFF292524),
            tertiary = Color(0xFF059669),
            onTertiary = Color(0xFFFFFFFF),
            error = Color(0xFFBE123C),
            background = Color(0xFFF6F4F0),
            onBackground = Color(0xFF1C1917),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1C1917),
            surfaceVariant = Color(0xFFEDE9E2),
            onSurfaceVariant = Color(0xFF6B635D),
            outline = Color(0xFFDFD9D0)
        )

        MuseThemePreset.PLATINUM_ICE_LIGHT -> lightColorScheme(
            primary = Color(0xFF0F172A),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE2E8F0),
            onPrimaryContainer = Color(0xFF0F172A),
            secondary = Color(0xFF475569),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF1F5F9),
            onSecondaryContainer = Color(0xFF1E293B),
            tertiary = Color(0xFF059669),
            onTertiary = Color(0xFFFFFFFF),
            error = Color(0xFFE11D48),
            background = Color(0xFFF1F5F9),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE7EDF4),
            onSurfaceVariant = Color(0xFF526277),
            outline = Color(0xFFD5DFEB)
        )

        MuseThemePreset.SAGE_DAY_LIGHT -> lightColorScheme(
            primary = Color(0xFF134E4A),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD6EBE5),
            onPrimaryContainer = Color(0xFF0B2E2B),
            secondary = Color(0xFF4B635B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE8F1EE),
            onSecondaryContainer = Color(0xFF1C2E28),
            tertiary = Color(0xFF059669),
            onTertiary = Color(0xFFFFFFFF),
            error = Color(0xFFBE123C),
            background = Color(0xFFF2F6F4),
            onBackground = Color(0xFF111C18),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF111C18),
            surfaceVariant = Color(0xFFE6EEEB),
            onSurfaceVariant = Color(0xFF556961),
            outline = Color(0xFFD4E2DC)
        )
    }
}

/**
 * Clean Vietnamese-First Typography:
 * - PlusJakartaFontFamily for all Vietnamese text, headings, labels, and buttons
 *   (impeccable diacritics, crisp modern geometric proportions).
 * - JetBrainsMonoFontFamily reserved strictly for currency numbers and version hashes.
 */
val MuseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.15).sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
)

@Composable
fun MuseToolTheme(
    themePreset: MuseThemePreset = MuseThemePreset.OBSIDIAN_MONO,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = themePreset.toColorScheme(),
        typography = MuseTypography,
        content = content
    )
}
