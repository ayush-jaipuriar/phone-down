@file:Suppress("MagicNumber")

package phonedown.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import phonedown.core.model.ThemeMode
import phonedown.core.model.shouldUseDarkTheme

data class PhoneDownColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val progress: Color,
    val progressTrack: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val toggle: Color,
    val inactive: Color,
)

private val LightPhoneDownColors =
    PhoneDownColors(
        background = Color(0xFFF8F8F7),
        surface = Color(0xFFFFFFFF),
        surfaceRaised = Color(0xFFFDFDFD),
        borderSubtle = Color(0xFFE7E7EA),
        textPrimary = Color(0xFF090A0D),
        textSecondary = Color(0xFF565B66),
        textTertiary = Color(0xFF8B9099),
        progress = Color(0xFF6878F6),
        progressTrack = Color(0xFFE3E5EA),
        success = Color(0xFF36A852),
        warning = Color(0xFFB17800),
        danger = Color(0xFFFF3B30),
        toggle = Color(0xFF4E6BFF),
        inactive = Color(0xFFAEB3BC),
    )

private val DarkPhoneDownColors =
    PhoneDownColors(
        background = Color(0xFF080B10),
        surface = Color(0xFF12161D),
        surfaceRaised = Color(0xFF181D25),
        borderSubtle = Color(0xFF252B35),
        textPrimary = Color(0xFFF7F8FA),
        textSecondary = Color(0xFFC1C7D0),
        textTertiary = Color(0xFF858D9A),
        progress = Color(0xFF9088FF),
        progressTrack = Color(0xFF3B424D),
        success = Color(0xFF7DDC8B),
        warning = Color(0xFFFFCA6A),
        danger = Color(0xFFFF5A52),
        toggle = Color(0xFF5D7BFF),
        inactive = Color(0xFF646B77),
    )

private val LocalPhoneDownColors = staticCompositionLocalOf { LightPhoneDownColors }

object PhoneDownDesign {
    val colors: PhoneDownColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPhoneDownColors.current
}

private val LightMaterialColors =
    lightColorScheme(
        background = LightPhoneDownColors.background,
        surface = LightPhoneDownColors.surface,
        primary = LightPhoneDownColors.textPrimary,
        secondary = LightPhoneDownColors.textSecondary,
        tertiary = LightPhoneDownColors.progress,
        error = LightPhoneDownColors.danger,
        onBackground = LightPhoneDownColors.textPrimary,
        onSurface = LightPhoneDownColors.textPrimary,
        onPrimary = LightPhoneDownColors.surface,
    )

private val DarkMaterialColors =
    darkColorScheme(
        background = DarkPhoneDownColors.background,
        surface = DarkPhoneDownColors.surface,
        primary = DarkPhoneDownColors.textPrimary,
        secondary = DarkPhoneDownColors.textSecondary,
        tertiary = DarkPhoneDownColors.progress,
        error = DarkPhoneDownColors.danger,
        onBackground = DarkPhoneDownColors.textPrimary,
        onSurface = DarkPhoneDownColors.textPrimary,
        onPrimary = DarkPhoneDownColors.background,
    )

@Composable
@Suppress("FunctionName")
fun PhoneDownTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val darkTheme = themeMode.shouldUseDarkTheme(isSystemInDarkTheme())
    val phoneDownColors = if (darkTheme) DarkPhoneDownColors else LightPhoneDownColors

    CompositionLocalProvider(LocalPhoneDownColors provides phoneDownColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkMaterialColors else LightMaterialColors,
            typography = PhoneDownTypography,
            shapes = PhoneDownShapes,
            content = content,
        )
    }
}
