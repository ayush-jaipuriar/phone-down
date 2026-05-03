package phonedown.feature.focus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode
import phonedown.feature.focus.state.FocusEvent
import phonedown.feature.focus.state.FocusPresentationState
import phonedown.feature.focus.state.FocusUiState

class FocusScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun focusScreenIdleStateShowsStartButton() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(presentationState = FocusPresentationState.Idle),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithTag(FocusTestTags.SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(FocusTestTags.START_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithText("Start Focus").assertIsDisplayed()
    }

    @Test
    fun focusScreenWaitingStateShowsGuidance() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(presentationState = FocusPresentationState.WaitingForPhoneDown),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("Place phone down to begin.").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun focusScreenActiveStateShowsTimerAndEndButton() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(
                        presentationState = FocusPresentationState.Active,
                        remainingSeconds = 1200,
                        elapsedSeconds = 300,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithTag(FocusTestTags.TIMER).assertIsDisplayed()
        composeRule.onNodeWithText("End Session").assertIsDisplayed()
    }

    @Test
    fun focusScreenCompletedCleanStateShowsSuccess() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(
                        presentationState = FocusPresentationState.CompletedClean,
                        elapsedSeconds = 1500,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("Clean session completed").assertIsDisplayed()
        composeRule.onNodeWithText("Back to Home").assertIsDisplayed()
    }

    @Test
    fun focusScreenBrokenStateShowsBrokenMessage() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(
                        presentationState = FocusPresentationState.Broken,
                        penaltySeconds = 60,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("Session broken").assertIsDisplayed()
    }

    @Test
    fun focusScreenStartButtonTriggersEvent() {
        var eventReceived: FocusEvent? = null
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(presentationState = FocusPresentationState.Idle),
                    onEvent = { eventReceived = it },
                )
            }
        }

        composeRule.onNodeWithTag(FocusTestTags.START_BUTTON).performClick()
        assert(eventReceived is FocusEvent.StartClicked)
    }

    @Test
    fun focusScreenEndButtonShowsConfirmation() {
        var eventReceived: FocusEvent? = null
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(
                        presentationState = FocusPresentationState.Active,
                        showEndConfirmation = true,
                    ),
                    onEvent = { eventReceived = it },
                )
            }
        }

        composeRule.onNodeWithText("End session now?").assertIsDisplayed()
        composeRule.onNodeWithText("End").performClick()
        assert(eventReceived is FocusEvent.EndConfirmed)
    }

    @Test
    fun focusScreenDurationSelectorShowsPresets() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(
                        presentationState = FocusPresentationState.Idle,
                        showDurationSelector = true,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("10 min").assertIsDisplayed()
        composeRule.onNodeWithText("25 min").assertIsDisplayed()
        composeRule.onNodeWithText("60 min").assertIsDisplayed()
    }

    @Test
    fun focusScreenSensorUnavailableShowsRetry() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(presentationState = FocusPresentationState.SensorUnavailable),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("Sensors unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun focusScreenPausedStateShowsPenalty() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(
                        presentationState = FocusPresentationState.PausedByPickup,
                        penaltySeconds = 60,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("Focus paused").assertIsDisplayed()
        composeRule.onNodeWithText("+1:00").assertIsDisplayed()
    }

    @Test
    fun focusScreenTodayMetricsDisplay() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(
                        presentationState = FocusPresentationState.Idle,
                        todayTotalFocusSeconds = 3600,
                        todaySessionsCount = 2,
                        todayCleanCount = 1,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithTag(FocusTestTags.TODAY_METRICS).assertIsDisplayed()
        composeRule.onNodeWithText("1h 0m").assertIsDisplayed()
        composeRule.onNodeWithText("2").assertIsDisplayed()
    }
}
