package phonedown.feature.focus

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode
import phonedown.feature.focus.state.FocusPresentationState
import phonedown.feature.focus.state.FocusUiState

class FocusScreenScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            theme = "android:Theme.Material.NoActionBar",
        )

    private val baseState =
        FocusUiState(
            selectedDurationSeconds = 25 * 60,
            remainingSeconds = 25 * 60,
            todayTotalFocusSeconds = 80 * 60,
            todaySessionsCount = 3,
            todayCleanCount = 2,
        )

    @Test
    fun idleState_Light() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(uiState = baseState.copy(presentationState = FocusPresentationState.Idle), onEvent = {})
            }
        }
    }

    @Test
    fun idleState_Dark() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Dark) {
                FocusScreen(uiState = baseState.copy(presentationState = FocusPresentationState.Idle), onEvent = {})
            }
        }
    }

    @Test
    fun activeState_Dark() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Dark) {
                FocusScreen(
                    uiState =
                        baseState.copy(
                            presentationState = FocusPresentationState.Active,
                            remainingSeconds = 14 * 60 + 38,
                        ),
                    onEvent = {},
                )
            }
        }
    }

    @Test
    fun pausedState_Dark() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Dark) {
                FocusScreen(
                    uiState =
                        baseState.copy(
                            presentationState = FocusPresentationState.PausedByPickup,
                            remainingSeconds = 24 * 60 + 12,
                            penaltySeconds = 60,
                        ),
                    onEvent = {},
                )
            }
        }
    }
}
