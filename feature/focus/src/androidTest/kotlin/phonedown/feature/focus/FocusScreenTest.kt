package phonedown.feature.focus

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode
import phonedown.feature.focus.state.FocusEvent
import phonedown.feature.focus.state.FocusPresentationState
import phonedown.feature.focus.state.FocusUiState

class FocusScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

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
                    uiState =
                        FocusUiState(
                            presentationState = FocusPresentationState.WaitingForPhoneDown,
                        ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("Place phone down to begin.").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun focusScreenWaitingCancelTriggersEndEvent() {
        var eventReceived: FocusEvent? = null
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState = FocusUiState(presentationState = FocusPresentationState.WaitingForPhoneDown),
                    onEvent = { eventReceived = it },
                )
            }
        }

        composeRule.onNodeWithText("Cancel").performClick()

        assert(eventReceived is FocusEvent.EndClicked)
    }

    @Test
    fun focusScreenActiveStateShowsTimerAndEndButton() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState =
                        FocusUiState(
                            presentationState = FocusPresentationState.Active,
                            remainingSeconds = 1200,
                            elapsedSeconds = 300,
                        ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithTag(FocusTestTags.TIMER).assertIsDisplayed()
        composeRule.onNodeWithText("End").assertIsDisplayed()
        composeRule.onNodeWithText("Focused").assertIsDisplayed()
        composeRule.onNodeWithText("05:00").assertIsDisplayed()
        composeRule.onAllNodesWithText("Remaining")[0].assertIsDisplayed()
        composeRule.onAllNodesWithText("20:00")[0].assertIsDisplayed()
    }

    @Test
    fun focusScreenCompletedCleanStateShowsSuccess() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState =
                        FocusUiState(
                            presentationState = FocusPresentationState.CompletedClean,
                            selectedDurationSeconds = 1500,
                            focusedSeconds = 1500,
                            elapsedSeconds = 1500,
                        ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("Great focus!").assertIsDisplayed()
        composeRule.onNodeWithText("Focus Time").assertIsDisplayed()
        composeRule.onNodeWithText("Elapsed Time").assertIsDisplayed()
        composeRule.onNodeWithText("Planned Time").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertIsDisplayed()
    }

    @Test
    fun focusScreenLiveBrokenStateOffersContinueGuidanceAndEndAction() {
        var eventReceived: FocusEvent? = null
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState =
                        FocusUiState(
                            presentationState = FocusPresentationState.CleanStatusLost,
                            penaltySeconds = 60,
                        ),
                    onEvent = { eventReceived = it },
                )
            }
        }

        composeRule.onNodeWithText("Clean status lost").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertIsNotDisplayed()
        composeRule.onNodeWithText("End session").performClick()
        assert(eventReceived is FocusEvent.EndClicked)
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
                    uiState =
                        FocusUiState(
                            presentationState = FocusPresentationState.Active,
                            showEndConfirmation = true,
                        ),
                    onEvent = { eventReceived = it },
                )
            }
        }

        composeRule.onNodeWithText("End Focus Session?").assertIsDisplayed()
        composeRule.onNodeWithText("End Session").performClick()
        assert(eventReceived is FocusEvent.EndConfirmed)
    }

    @Test
    fun focusScreenDurationSelectorShowsPresets() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState =
                        FocusUiState(
                            presentationState = FocusPresentationState.Idle,
                            showDurationSelector = true,
                        ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("10 minutes").assertIsDisplayed()
        composeRule.onNodeWithText("25 minutes").assertIsDisplayed()
        composeRule.onNodeWithText("60 minutes").assertIsDisplayed()
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
                    uiState =
                        FocusUiState(
                            presentationState = FocusPresentationState.PausedByPickup,
                            penaltySeconds = 60,
                        ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("Phone Picked Up").assertIsDisplayed()
        composeRule.onNodeWithText("+1:00 penalty").assertIsDisplayed()
        composeRule.onNodeWithText("Keep your phone down to continue").assertIsDisplayed()
    }

    @Test
    fun focusScreenTodayMetricsDisplay() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState =
                        FocusUiState(
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

    @Test
    fun customDurationAboveLegacyLimitIsApplied() {
        var selectedEvent: FocusEvent? = null
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen(
                    uiState =
                        FocusUiState(
                            presentationState = FocusPresentationState.Idle,
                            showDurationSelector = true,
                        ),
                    onEvent = { selectedEvent = it },
                )
            }
        }

        composeRule.onNodeWithTag(FocusTestTags.CUSTOM_DURATION_INPUT).performTextInput("90")
        composeRule.onAllNodesWithText("Free custom duration is currently limited to 60 minutes.").assertCountEquals(0)
        composeRule.onNodeWithText("Apply Custom Duration").performClick()

        assertEquals(FocusEvent.DurationSelected(90 * 60L), selectedEvent)
    }
}
