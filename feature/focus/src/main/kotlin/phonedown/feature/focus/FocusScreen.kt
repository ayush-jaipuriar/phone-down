package phonedown.feature.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import phonedown.core.designsystem.PhoneDownAccent
import phonedown.core.designsystem.PhoneDownButton
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownIconButton
import phonedown.core.designsystem.PhoneDownInlineStatus
import phonedown.core.designsystem.PhoneDownMetricCard
import phonedown.core.designsystem.PhoneDownProgressRing
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownTopBar
import phonedown.core.model.ThemeMode
import phonedown.feature.focus.state.FocusEvent
import phonedown.feature.focus.state.FocusPresentationState
import phonedown.feature.focus.state.FocusUiState

@Composable
@Suppress("FunctionName", "LongMethod")
fun FocusScreen(
    uiState: FocusUiState,
    onEvent: (FocusEvent) -> Unit,
) {
    val showTodaySummary =
        uiState.presentationState == FocusPresentationState.Idle ||
            uiState.presentationState == FocusPresentationState.SensorUnavailable

    PhoneDownScreen(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(FocusTestTags.SCREEN),
    ) {
        PhoneDownTopBar(title = topBarTitle(uiState.presentationState))

        Spacer(modifier = Modifier.height(PhoneDownSpacing.xl))

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg),
        ) {
            FocusRingSection(uiState = uiState)

            AnimatedContent(
                targetState = uiState.presentationState,
                label = "FocusActions",
                modifier = Modifier.fillMaxWidth(),
            ) { state ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    when (state) {
                        FocusPresentationState.Idle -> {
                            IdleActions(
                                selectedDurationSeconds = uiState.selectedDurationSeconds,
                                onStartClick = { onEvent(FocusEvent.StartClicked) },
                                onDurationSelectorClick = { onEvent(FocusEvent.DurationSelectorClicked) },
                            )
                        }

                        FocusPresentationState.WaitingForPhoneDown -> {
                            GuidanceState(
                                title = "Place phone down to begin.",
                                body = "Put your phone face down on a stable surface.",
                                actionLabel = "Cancel",
                                onAction = { onEvent(FocusEvent.EndClicked) },
                            )
                        }

                        FocusPresentationState.Arming -> {
                            GuidanceState(
                                title = "Hold still...",
                                body = "Keep your phone face down until focus begins.",
                                actionLabel = "Cancel",
                                onAction = { onEvent(FocusEvent.EndClicked) },
                            )
                        }

                        FocusPresentationState.Active,
                        FocusPresentationState.PausedByPickup,
                        FocusPresentationState.PausedByCall,
                        -> {
                            InProgressActions(
                                presentationState = state,
                                penaltySeconds = uiState.penaltySeconds,
                                onEndClick = { onEvent(FocusEvent.EndClicked) },
                            )
                        }

                        FocusPresentationState.CompletedClean,
                        FocusPresentationState.CompletedInterrupted,
                        FocusPresentationState.EndedEarly,
                        FocusPresentationState.Broken,
                        FocusPresentationState.Invalid,
                        -> {
                            ResultState(
                                presentationState = state,
                                interruptionCount = uiState.interruptionCount,
                                onDoneClick = { onEvent(FocusEvent.BackToHomeClicked) },
                            )
                        }

                        FocusPresentationState.SensorUnavailable -> {
                            SensorUnavailableState(
                                onRetryClick = { onEvent(FocusEvent.RetrySensorsClicked) },
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showTodaySummary) {
            TodaySummary(uiState = uiState)
        }
    }

    if (uiState.showDurationSelector) {
        DurationSelectorSheet(
            currentDurationSeconds = uiState.selectedDurationSeconds,
            freeCustomDurationSeconds = uiState.freeCustomDurationSeconds,
            onDismiss = { onEvent(FocusEvent.DurationSelectorDismissed) },
            onSelect = { onEvent(FocusEvent.DurationSelected(it)) },
        )
    }

    if (uiState.showEndConfirmation) {
        EndConfirmationSheet(
            onConfirm = { onEvent(FocusEvent.EndConfirmed) },
            onDismiss = { onEvent(FocusEvent.EndDismissed) },
        )
    }
}

@Composable
private fun IdleActions(
    selectedDurationSeconds: Long,
    onStartClick: () -> Unit,
    onDurationSelectorClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
    ) {
        PhoneDownButton(
            text = "Start Focus",
            onClick = onStartClick,
            modifier = Modifier.testTag(FocusTestTags.START_BUTTON),
        )
        Text(
            text = "Default ${selectedDurationSeconds / 60L} min",
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelMedium,
            modifier =
                Modifier
                    .clickable(onClick = onDurationSelectorClick)
                    .padding(PhoneDownSpacing.xs),
        )
    }
}

@Composable
private fun GuidanceState(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm),
    ) {
        Text(
            text = title,
            color = PhoneDownDesign.colors.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))
        PhoneDownButton(
            text = actionLabel,
            onClick = onAction,
            quiet = true,
        )
    }
}

@Composable
private fun InProgressActions(
    presentationState: FocusPresentationState,
    penaltySeconds: Long,
    onEndClick: () -> Unit,
) {
    val statusText =
        when (presentationState) {
            FocusPresentationState.Active -> "Keep your phone down"
            FocusPresentationState.PausedByCall -> "Focus paused for a call"
            else -> "Keep your phone down to continue"
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
    ) {
        Text(
            text = statusText,
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))
        Row(
            horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PhoneDownIconButton(
                label = "End",
                symbol = "■",
                onClick = onEndClick,
            )
        }
        if (penaltySeconds > 0L) {
            PhoneDownInlineStatus(
                text = "+${penaltySeconds / 60L}:00 penalty",
                accent = PhoneDownAccent.Danger,
            )
        }
    }
}

@Composable
private fun ResultState(
    presentationState: FocusPresentationState,
    interruptionCount: Int,
    onDoneClick: () -> Unit,
) {
    val title =
        when (presentationState) {
            FocusPresentationState.CompletedClean -> "Clean session completed"
            FocusPresentationState.CompletedInterrupted -> "Session completed"
            FocusPresentationState.EndedEarly -> "Session ended early"
            FocusPresentationState.Invalid -> "Not enough focus time to count."
            FocusPresentationState.Broken -> "Session broken"
            else -> ""
        }
    val body =
        when (presentationState) {
            FocusPresentationState.CompletedInterrupted ->
                if (interruptionCount > 0) {
                    "$interruptionCount interruptions recorded."
                } else {
                    "Focus session saved."
                }
            FocusPresentationState.EndedEarly -> "Current progress was saved as partial."
            FocusPresentationState.Broken -> "This session no longer counts as clean focus."
            else -> null
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
    ) {
        Text(
            text = title,
            color = PhoneDownDesign.colors.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (body != null) {
            Text(
                text = body,
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        PhoneDownButton(
            text = "Done",
            onClick = onDoneClick,
        )
    }
}

@Composable
private fun SensorUnavailableState(onRetryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
    ) {
        Text(
            text = "Sensors unavailable",
            color = PhoneDownDesign.colors.danger,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Phone Down needs device sensors to run honestly. Check sensor access, then try again.",
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        PhoneDownButton(
            text = "Retry",
            onClick = onRetryClick,
        )
    }
}

@Composable
private fun TodaySummary(uiState: FocusUiState) {
    PhoneDownCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = PhoneDownSpacing.md)
                .testTag(FocusTestTags.TODAY_METRICS),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm)) {
            Text(
                text = "TODAY",
                color = PhoneDownDesign.colors.textTertiary,
                style = MaterialTheme.typography.labelSmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PhoneDownMetricCard(
                    label = "Total Focus",
                    value = formatDurationHoursMins(uiState.todayTotalFocusSeconds),
                    modifier = Modifier.weight(1f),
                )
                PhoneDownMetricCard(
                    label = "Sessions",
                    value = uiState.todaySessionsCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                PhoneDownMetricCard(
                    label = "Clean",
                    value = uiState.todayCleanCount.toString(),
                    modifier = Modifier.weight(1f),
                    accent = PhoneDownAccent.Success,
                )
            }
        }
    }
}

@Composable
private fun FocusRingSection(uiState: FocusUiState) {
    val progressTarget =
        when (uiState.presentationState) {
            FocusPresentationState.Idle,
            FocusPresentationState.WaitingForPhoneDown,
            FocusPresentationState.Arming,
            FocusPresentationState.SensorUnavailable,
            -> 0f

            FocusPresentationState.Active,
            FocusPresentationState.PausedByPickup,
            FocusPresentationState.PausedByCall,
            -> {
                val total = uiState.selectedDurationSeconds.coerceAtLeast(1L)
                val elapsed = (total - uiState.remainingSeconds).coerceIn(0L, total)
                elapsed.toFloat() / total.toFloat()
            }

            else -> 1f
        }

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 500),
        label = "RingProgress",
    )

    PhoneDownProgressRing(progress = animatedProgress) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (
                uiState.presentationState == FocusPresentationState.CompletedClean ||
                uiState.presentationState == FocusPresentationState.CompletedInterrupted
            ) {
                Text(
                    text = "✓",
                    color = PhoneDownDesign.colors.success,
                    style = MaterialTheme.typography.displayLarge,
                )
            } else {
                val displaySeconds =
                    if (uiState.presentationState == FocusPresentationState.Idle) {
                        uiState.selectedDurationSeconds
                    } else {
                        uiState.remainingSeconds
                    }

                Text(
                    text = formatDurationMinsSecs(displaySeconds),
                    modifier = Modifier.testTag(FocusTestTags.TIMER),
                    color = PhoneDownDesign.colors.textPrimary,
                    style = MaterialTheme.typography.displayLarge,
                )

                val label =
                    when (uiState.presentationState) {
                        FocusPresentationState.Idle -> "Focus"
                        FocusPresentationState.WaitingForPhoneDown -> "Ready"
                        FocusPresentationState.Arming -> "Hold still"
                        FocusPresentationState.Active -> "Remaining"
                        FocusPresentationState.PausedByPickup -> "Paused"
                        FocusPresentationState.PausedByCall -> "Call paused"
                        FocusPresentationState.SensorUnavailable -> "Unavailable"
                        else -> ""
                    }
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        color = PhoneDownDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationSelectorSheet(
    currentDurationSeconds: Long,
    freeCustomDurationSeconds: Long?,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presetsMinutes = listOf(10L, 15L, 25L, 45L, 60L)
    var customMinutesInput by rememberSaveable {
        mutableStateOf(
            if ((currentDurationSeconds / 60L) !in presetsMinutes) {
                (currentDurationSeconds / 60L).toString()
            } else {
                ""
            },
        )
    }

    val customMinutes = customMinutesInput.toLongOrNull()
    val customDurationSeconds = customMinutes?.times(60L)
    val exceedsFreeLimit =
        freeCustomDurationSeconds != null &&
            customDurationSeconds != null &&
            customDurationSeconds > freeCustomDurationSeconds
    val customInputError =
        when {
            customMinutesInput.isBlank() -> null
            customMinutes == null -> "Enter a whole number of minutes."
            customMinutes <= 0L -> "Duration must be at least 1 minute."
            exceedsFreeLimit ->
                "Free custom duration is currently limited to ${freeCustomDurationSeconds / 60L} minutes."
            else -> null
        }
    val canApplyCustom = customDurationSeconds != null && customInputError == null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PhoneDownDesign.colors.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(PhoneDownSpacing.screen)
                    .padding(bottom = PhoneDownSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
        ) {
            Text(
                text = "Select Duration",
                style = MaterialTheme.typography.titleMedium,
                color = PhoneDownDesign.colors.textPrimary,
            )

            presetsMinutes.forEach { minutes ->
                val seconds = minutes * 60L
                val isSelected = seconds == currentDurationSeconds
                PhoneDownCard(
                    modifier = Modifier.clickable { onSelect(seconds) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$minutes minutes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PhoneDownDesign.colors.textPrimary,
                        )
                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = PhoneDownDesign.colors.textPrimary,
                            )
                        }
                    }
                }
            }

            Text(
                text = "Custom",
                style = MaterialTheme.typography.titleSmall,
                color = PhoneDownDesign.colors.textPrimary,
            )
            OutlinedTextField(
                value = customMinutesInput,
                onValueChange = { value ->
                    customMinutesInput = value.filter { it.isDigit() }.take(3)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Minutes") },
                supportingText = {
                    val helperText =
                        when {
                            customInputError != null -> customInputError
                            freeCustomDurationSeconds != null ->
                                "Free custom duration up to ${freeCustomDurationSeconds / 60L} minutes."
                            else -> "Choose any whole-minute duration."
                        }
                    Text(helperText)
                },
                isError = customInputError != null,
            )
            PhoneDownButton(
                text = "Apply Custom Duration",
                onClick = { onSelect(customDurationSeconds!!) },
                enabled = canApplyCustom,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndConfirmationSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PhoneDownDesign.colors.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(PhoneDownSpacing.screen)
                    .padding(bottom = PhoneDownSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
        ) {
            Text(
                text = "End Focus Session?",
                style = MaterialTheme.typography.titleMedium,
                color = PhoneDownDesign.colors.textPrimary,
            )
            Text(
                text = "Current progress will be saved as partial.",
                style = MaterialTheme.typography.bodyMedium,
                color = PhoneDownDesign.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(PhoneDownSpacing.md))
            PhoneDownButton(
                text = "End Session",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
            PhoneDownButton(
                text = "Cancel",
                onClick = onDismiss,
                quiet = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun topBarTitle(presentationState: FocusPresentationState): String =
    when (presentationState) {
        FocusPresentationState.Idle -> "Phone Down"
        FocusPresentationState.WaitingForPhoneDown,
        FocusPresentationState.Arming,
        -> "Ready to focus?"

        FocusPresentationState.Active -> "Focusing"
        FocusPresentationState.PausedByPickup -> "Focus paused"
        FocusPresentationState.PausedByCall -> "Call in progress"
        FocusPresentationState.CompletedClean,
        FocusPresentationState.CompletedInterrupted,
        -> "Session complete"

        else -> "Phone Down"
    }

private fun formatDurationMinsSecs(seconds: Long): String {
    val minutes = seconds / 60L
    val secs = seconds % 60L
    return "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

private fun formatDurationHoursMins(seconds: Long): String {
    val hours = seconds / 3600L
    val minutes = (seconds % 3600L) / 60L
    return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes}m"
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun FocusScreenIdlePreview() {
    PhoneDownTheme(themeMode = ThemeMode.Dark) {
        FocusScreen(
            uiState = FocusUiState(),
            onEvent = {},
        )
    }
}
