package phonedown.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun systemModeUsesSystemTheme() {
        assertTrue(ThemeMode.System.shouldUseDarkTheme(systemInDarkTheme = true))
        assertFalse(ThemeMode.System.shouldUseDarkTheme(systemInDarkTheme = false))
    }

    @Test
    fun lightModeAlwaysUsesLightTheme() {
        assertFalse(ThemeMode.Light.shouldUseDarkTheme(systemInDarkTheme = true))
        assertFalse(ThemeMode.Light.shouldUseDarkTheme(systemInDarkTheme = false))
    }

    @Test
    fun darkModeAlwaysUsesDarkTheme() {
        assertTrue(ThemeMode.Dark.shouldUseDarkTheme(systemInDarkTheme = true))
        assertTrue(ThemeMode.Dark.shouldUseDarkTheme(systemInDarkTheme = false))
    }
}
