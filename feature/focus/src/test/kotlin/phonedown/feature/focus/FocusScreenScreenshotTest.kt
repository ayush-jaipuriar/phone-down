package phonedown.feature.focus

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode

class FocusScreenScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun focusScreenLight() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen()
            }
        }
    }

    @Test
    fun focusScreenDark() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Dark) {
                FocusScreen()
            }
        }
    }
}
