package phonedown.feature.insights

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode

class InsightsScreenScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun insightsScreenLight() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                InsightsScreen()
            }
        }
    }

    @Test
    fun insightsScreenDark() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Dark) {
                InsightsScreen()
            }
        }
    }
}
