package phonedown.feature.settings

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode

class SettingsScreenScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun settingsScreenLight() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                SettingsScreen(
                    onAccountClick = {},
                    onProClick = {},
                    selectedThemeMode = ThemeMode.Light,
                )
            }
        }
    }

    @Test
    fun settingsScreenDark() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Dark) {
                SettingsScreen(
                    onAccountClick = {},
                    onProClick = {},
                    selectedThemeMode = ThemeMode.Dark,
                )
            }
        }
    }
}
