package phonedown.feature.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import phonedown.core.designsystem.PhoneDownAccent
import phonedown.core.designsystem.PhoneDownButton
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownCardHeaderTextStyle
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownIconButton
import phonedown.core.designsystem.PhoneDownInlineStatus
import phonedown.core.designsystem.PhoneDownMetricCard
import phonedown.core.designsystem.PhoneDownProgressRing
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSectionHeaderTextStyle
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownTimerTextStyle
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
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg),
        ) {
            FocusRingSection(uiState = uiState)

            SessionProgressSummary(uiState = uiState)

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

                        FocusPresentationState.ReadyToFocus -> {
                            ReadyToFocusContent(
                                onBackClick = { onEvent(FocusEvent.ReadyBackClicked) },
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
                        FocusPresentationState.PausedByCall,
                        -> {
                            InProgressActions(
                                presentationState = state,
                                penaltySeconds = uiState.penaltySeconds,
                                showAddTime = uiState.showAddTime,
                                onEndClick = { onEvent(FocusEvent.EndClicked) },
                                onPauseClick = if (state == FocusPresentationState.Active) {
                                    { onEvent(FocusEvent.PauseClicked) }
                                } else { null },
                                onAddTimeClick = if (state == FocusPresentationState.Active) {
                                    { onEvent(FocusEvent.AddTimeClicked) }
                                } else { null },
                                onAddTimeSelected = { onEvent(FocusEvent.AddTimeSelected(it)) },
                            )
                        }

                        FocusPresentationState.PausedByUser -> {
                            InProgressActions(
                                presentationState = state,
                                penaltySeconds = uiState.penaltySeconds,
                                onEndClick = { onEvent(FocusEvent.EndClicked) },
                                onResumeClick = { onEvent(FocusEvent.ResumeClicked) },
                            )
                        }

                        FocusPresentationState.PausedByPickup -> {
                            PausedByPickupActions(
                                graceRemainingSeconds = uiState.graceRemainingSeconds,
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
                            SessionCompleteContent(
                                presentationState = state,
                                selectedDurationSeconds = uiState.selectedDurationSeconds,
                                remainingSeconds = uiState.remainingSeconds,
                                penaltySeconds = uiState.penaltySeconds,
                                interruptionCount = uiState.interruptionCount,
                                clean = uiState.clean,
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
private fun SessionProgressSummary(uiState: FocusUiState) {
    val visible =
        uiState.presentationState == FocusPresentationState.ReadyToFocus ||
            uiState.presentationState == FocusPresentationState.WaitingForPhoneDown ||
            uiState.presentationState == FocusPresentationState.Arming ||
            uiState.presentationState == FocusPresentationState.Active ||
            uiState.presentationState == FocusPresentationState.PausedByPickup ||
            uiState.presentationState == FocusPresentationState.PausedByUser ||
            uiState.presentationState == FocusPresentationState.PausedByCall
    if (!visible) {
        return
    }

    val focusedSeconds = (uiState.selectedDurationSeconds - uiState.remainingSeconds).coerceAtLeast(0L)
    PhoneDownCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PhoneDownMetricCard(
                label = "Focused",
                value = formatDurationMinsSecs(focusedSeconds),
                modifier = Modifier.weight(1f),
                accent =
                    if (focusedSeconds > 0L) {
                        PhoneDownAccent.Progress
                    } else {
                        PhoneDownAccent.Neutral
                    },
            )
            PhoneDownMetricCard(
                label = "Remaining",
                value = formatDurationMinsSecs(uiState.remainingSeconds),
                modifier = Modifier.weight(1f),
            )
            PhoneDownMetricCard(
                label = "State",
                value = progressStateLabel(uiState.presentationState),
                modifier = Modifier.weight(1f),
                accent = progressStateAccent(uiState.presentationState),
            )
        }
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
        Row(
            modifier =
                Modifier
                    .clickable(onClick = onDurationSelectorClick)
                    .padding(PhoneDownSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
        ) {
            Text(
                text = "Default ${selectedDurationSeconds / 60L} min",
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Choose duration",
                tint = PhoneDownDesign.colors.textTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
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
            style = PhoneDownSectionHeaderTextStyle,
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
    showAddTime: Boolean = false,
    onPauseClick: (() -> Unit)? = null,
    onAddTimeClick: (() -> Unit)? = null,
    onAddTimeSelected: ((Int) -> Unit)? = null,
    onResumeClick: (() -> Unit)? = null,
) {
    val statusText =
        when (presentationState) {
            FocusPresentationState.Active -> "Keep your phone down"
            FocusPresentationState.PausedByCall -> "Focus paused for a call"
            FocusPresentationState.PausedByUser -> "Focus paused"
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
        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))
        Row(
            horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PhoneDownIconButton(
                label = "End",
                symbol = "■",
                onClick = onEndClick,
            )
            onResumeClick?.let { resume ->
                PhoneDownIconButton(
                    label = "Resume",
                    symbol = "▶",
                    onClick = resume,
                )
            }
            onPauseClick?.let { pause ->
                PhoneDownIconButton(
                    label = "Pause",
                    symbol = "⏸",
                    onClick = pause,
                )
            }
            onAddTimeClick?.let { addTime ->
                PhoneDownIconButton(
                    label = "+Time",
                    symbol = "+",
                    onClick = addTime,
                )
            }
        }

        if (showAddTime && onAddTimeSelected != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(1, 5, 15).forEach { minutes ->
                    androidx.compose.foundation.layout.Box(
                        modifier =
                            Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(PhoneDownDesign.colors.surfaceRaised)
                                .clickable { onAddTimeSelected(minutes) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "+${minutes}m",
                            color = PhoneDownDesign.colors.textPrimary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
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
private fun PausedByPickupActions(
    graceRemainingSeconds: Long,
    penaltySeconds: Long,
    onEndClick: () -> Unit,
) {
    val countdownColor = when {
        graceRemainingSeconds > 2 -> PhoneDownDesign.colors.warning
        else -> PhoneDownDesign.colors.danger
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
    ) {
        Text(
            text = "Phone Picked Up",
            color = PhoneDownDesign.colors.danger,
            style = PhoneDownSectionHeaderTextStyle,
            textAlign = TextAlign.Center,
        )

        PhonePickedUpIllustration()

        Text(
            text = formatDurationMinsSecs(graceRemainingSeconds),
            color = countdownColor,
            style = PhoneDownTimerTextStyle,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Keep your phone down to continue",
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

        PhoneDownIconButton(
            label = "End",
            symbol = "■",
            onClick = onEndClick,
        )

        if (penaltySeconds > 0L) {
            PhoneDownInlineStatus(
                text = "+${penaltySeconds / 60L}:00 penalty",
                accent = PhoneDownAccent.Danger,
            )
        }
    }
}

@Composable
private fun PhonePickedUpIllustration() {
    val primaryColor = PhoneDownDesign.colors.textPrimary
    val dangerColor = PhoneDownDesign.colors.danger
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(64.dp),
    ) {
        val phoneWidth = size.width * 0.5f
        val phoneHeight = size.height * 0.7f
        val phoneLeft = (size.width - phoneWidth) / 2f
        val phoneTop = size.height * 0.25f

        // Phone body
        drawRoundRect(
            color = primaryColor,
            topLeft = androidx.compose.ui.geometry.Offset(phoneLeft, phoneTop),
            size = androidx.compose.ui.geometry.Size(phoneWidth, phoneHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
        )

        // Screen
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.3f),
            topLeft = androidx.compose.ui.geometry.Offset(phoneLeft + 4f, phoneTop + 8f),
            size = androidx.compose.ui.geometry.Size(phoneWidth - 8f, phoneHeight - 16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
        )

        // Upward arrow
        val arrowCenterX = size.width / 2f
        val arrowBottom = phoneTop - 8f
        val arrowTop = arrowBottom - 20f
        val arrowWidth = 10f

        drawLine(
            color = dangerColor,
            start = androidx.compose.ui.geometry.Offset(arrowCenterX, arrowBottom),
            end = androidx.compose.ui.geometry.Offset(arrowCenterX, arrowTop),
            strokeWidth = 4f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        drawLine(
            color = dangerColor,
            start = androidx.compose.ui.geometry.Offset(arrowCenterX - arrowWidth, arrowTop + arrowWidth),
            end = androidx.compose.ui.geometry.Offset(arrowCenterX, arrowTop),
            strokeWidth = 4f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        drawLine(
            color = dangerColor,
            start = androidx.compose.ui.geometry.Offset(arrowCenterX + arrowWidth, arrowTop + arrowWidth),
            end = androidx.compose.ui.geometry.Offset(arrowCenterX, arrowTop),
            strokeWidth = 4f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}

@Composable
private fun SessionCompleteContent(
    presentationState: FocusPresentationState,
    selectedDurationSeconds: Long,
    remainingSeconds: Long,
    penaltySeconds: Long,
    interruptionCount: Int,
    clean: Boolean,
    onDoneClick: () -> Unit,
) {
    val title =
        when (presentationState) {
            FocusPresentationState.CompletedClean -> "Great focus!"
            FocusPresentationState.CompletedInterrupted -> "Session complete"
            FocusPresentationState.EndedEarly -> "Session ended early"
            FocusPresentationState.Invalid -> "Not enough focus time to count"
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
    val showCircle =
        presentationState == FocusPresentationState.CompletedClean ||
            presentationState == FocusPresentationState.CompletedInterrupted

    val focusTimeSeconds = (selectedDurationSeconds - remainingSeconds).coerceAtLeast(0L)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
    ) {
        if (showCircle) {
            CompletionCircle(clean = clean)
        }

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

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        Column(
            modifier = Modifier.fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm),
        ) {
            TimeBreakdownRow(
                label = "Focus Time",
                value = formatDurationMinsSecs(focusTimeSeconds),
            )
            TimeBreakdownRow(
                label = "Penalty Time",
                value = if (penaltySeconds > 0L) {
                    "+${formatDurationMinsSecs(penaltySeconds)}"
                } else {
                    "+0:00"
                },
                valueColor = if (penaltySeconds > 0L) {
                    PhoneDownDesign.colors.danger
                } else {
                    PhoneDownDesign.colors.textTertiary
                },
            )
            TimeBreakdownRow(
                label = "Total Time",
                value = formatDurationMinsSecs(selectedDurationSeconds),
            )
        }

        if (clean && presentationState == FocusPresentationState.CompletedClean) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "✓",
                    color = PhoneDownDesign.colors.success,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Text(
                    text = "Clean Session",
                    color = PhoneDownDesign.colors.success,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

        PhoneDownButton(
            text = "Done",
            onClick = onDoneClick,
        )
    }
}

@Composable
private fun TimeBreakdownRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = PhoneDownDesign.colors.textPrimary,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CompletionCircle(clean: Boolean) {
    val circleColor = if (clean) PhoneDownDesign.colors.success else PhoneDownDesign.colors.surfaceRaised
    val checkColor = if (clean) PhoneDownDesign.colors.surface else PhoneDownDesign.colors.textPrimary

    androidx.compose.foundation.Canvas(modifier = Modifier.size(96.dp)) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = size.minDimension / 2f

        // Filled circle
        drawCircle(
            color = circleColor,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
        )

        // Checkmark
        val scale = radius / 48f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(centerX - 18f * scale, centerY + 2f * scale)
            lineTo(centerX - 6f * scale, centerY + 18f * scale)
            lineTo(centerX + 20f * scale, centerY - 14f * scale)
        }
        drawPath(
            path = path,
            color = checkColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 5f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
        )
    }
}

@Composable
private fun ArmingCountdown() {
    var countdownValue by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        while (countdownValue > 1) {
            delay(1000L)
            countdownValue -= 1
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = countdownValue,
            label = "ArmingCountdown",
            modifier = Modifier.testTag(FocusTestTags.TIMER),
        ) { value ->
            Text(
                text = value.toString(),
                color = PhoneDownDesign.colors.progress,
                style = PhoneDownTimerTextStyle,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = if (countdownValue > 1) "Get ready..." else "Hold still",
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ReadyToFocusContent(onBackClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
    ) {
        Text(
            text = "Ready to focus?",
            color = PhoneDownDesign.colors.textPrimary,
            style = PhoneDownSectionHeaderTextStyle,
            textAlign = TextAlign.Center,
        )

        PhoneFaceDownIllustration()

        Column(
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            InstructionStep(number = "1", text = "Tap Start Focus")
            InstructionStep(number = "2", text = "Place your phone face down")
            InstructionStep(number = "3", text = "Stay still and focus")
        }

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

        Text(
            text = "Phone Down to begin",
            color = PhoneDownDesign.colors.textPrimary,
            style = PhoneDownCardHeaderTextStyle,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

        PhoneDownButton(
            text = "Cancel",
            onClick = onBackClick,
            quiet = true,
        )
    }
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = number,
            color = PhoneDownDesign.colors.progress,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
        Text(
            text = text,
            color = PhoneDownDesign.colors.textPrimary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PhoneFaceDownIllustration() {
    val primaryColor = PhoneDownDesign.colors.textPrimary
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(80.dp),
    ) {
        val phoneWidth = size.width * 0.6f
        val phoneHeight = size.height * 0.7f
        val phoneLeft = (size.width - phoneWidth) / 2f
        val phoneTop = size.height * 0.2f

        // Phone body
        drawRoundRect(
            color = primaryColor,
            topLeft = androidx.compose.ui.geometry.Offset(phoneLeft, phoneTop),
            size = androidx.compose.ui.geometry.Size(phoneWidth, phoneHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
        )

        // Screen
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.3f),
            topLeft = androidx.compose.ui.geometry.Offset(phoneLeft + 6f, phoneTop + 10f),
            size = androidx.compose.ui.geometry.Size(phoneWidth - 12f, phoneHeight - 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        )

        // Small dot (home button / camera)
        drawCircle(
            color = primaryColor,
            radius = 4f,
            center = androidx.compose.ui.geometry.Offset(size.width / 2f, phoneTop + phoneHeight - 10f),
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
            style = PhoneDownSectionHeaderTextStyle,
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
                color = PhoneDownDesign.colors.textSecondary,
                style = PhoneDownCardHeaderTextStyle,
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
            FocusPresentationState.ReadyToFocus,
            FocusPresentationState.WaitingForPhoneDown,
            FocusPresentationState.Arming,
            FocusPresentationState.SensorUnavailable,
            -> 0f

            FocusPresentationState.Active,
            FocusPresentationState.PausedByPickup,
            FocusPresentationState.PausedByUser,
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
            } else if (uiState.presentationState == FocusPresentationState.Arming) {
                ArmingCountdown()
            } else {
                val displaySeconds =
                    if (uiState.presentationState == FocusPresentationState.Idle ||
                        uiState.presentationState == FocusPresentationState.ReadyToFocus
                    ) {
                        uiState.selectedDurationSeconds
                    } else {
                        uiState.remainingSeconds
                    }

                Text(
                    text = formatDurationMinsSecs(displaySeconds),
                    modifier = Modifier.testTag(FocusTestTags.TIMER),
                    color = PhoneDownDesign.colors.textPrimary,
                    style = PhoneDownTimerTextStyle,
                )

                val label =
                    when (uiState.presentationState) {
                        FocusPresentationState.Idle -> "Focus"
                        FocusPresentationState.ReadyToFocus -> "Ready"
                        FocusPresentationState.WaitingForPhoneDown -> "Ready"
                        FocusPresentationState.Arming -> "Hold still"
                        FocusPresentationState.Active -> "Remaining"
                        FocusPresentationState.PausedByPickup -> "Paused"
                        FocusPresentationState.PausedByUser -> "Paused"
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
                onClick = {
                    customDurationSeconds?.let { onSelect(it) }
                },
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

private fun progressStateLabel(presentationState: FocusPresentationState): String =
    when (presentationState) {
        FocusPresentationState.ReadyToFocus -> "Ready"
        FocusPresentationState.WaitingForPhoneDown -> "Waiting"
        FocusPresentationState.Arming -> "Starting"
        FocusPresentationState.Active -> "Active"
        FocusPresentationState.PausedByPickup -> "Paused"
        FocusPresentationState.PausedByUser -> "Paused"
        FocusPresentationState.PausedByCall -> "Call"
        else -> "-"
    }

private fun progressStateAccent(presentationState: FocusPresentationState): PhoneDownAccent =
    when (presentationState) {
        FocusPresentationState.ReadyToFocus -> PhoneDownAccent.Progress
        FocusPresentationState.Active -> PhoneDownAccent.Success
        FocusPresentationState.Arming -> PhoneDownAccent.Progress
        FocusPresentationState.PausedByPickup -> PhoneDownAccent.Danger
        FocusPresentationState.PausedByUser -> PhoneDownAccent.Warning
        FocusPresentationState.PausedByCall -> PhoneDownAccent.Warning
        else -> PhoneDownAccent.Neutral
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
