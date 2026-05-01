package phonedown.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
@Suppress("FunctionName")
fun PhoneDownScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .background(PhoneDownDesign.colors.background)
                .padding(horizontal = PhoneDownSpacing.screen)
                .padding(top = PhoneDownSpacing.xl),
        content = content,
    )
}

@Composable
@Suppress("FunctionName")
fun PhoneDownTopBar(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(PhoneDownSize.minTouchTarget),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = PhoneDownDesign.colors.textPrimary,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
@Suppress("FunctionName")
fun PhoneDownButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quiet: Boolean = false,
) {
    val colors = PhoneDownDesign.colors

    Button(
        onClick = onClick,
        modifier =
            modifier
                .height(52.dp)
                .defaultMinSize(minWidth = 164.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (quiet) colors.surfaceRaised else colors.textPrimary,
                contentColor = if (quiet) colors.textPrimary else colors.background,
                disabledContainerColor = colors.inactive,
                disabledContentColor = colors.textSecondary,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
@Suppress("FunctionName")
fun PhoneDownIconButton(
    label: String,
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs),
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(PhoneDownDesign.colors.surfaceRaised)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = label,
                        onClick = onClick,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = symbol,
                color = PhoneDownDesign.colors.textPrimary,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            text = label,
            color = PhoneDownDesign.colors.textPrimary,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
@Suppress("FunctionName")
fun PhoneDownCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, PhoneDownDesign.colors.borderSubtle, MaterialTheme.shapes.large),
        color = PhoneDownDesign.colors.surfaceRaised,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = {
            Box(modifier = Modifier.padding(PhoneDownSpacing.card)) {
                content()
            }
        },
    )
}

@Composable
@Suppress("FunctionName")
fun PhoneDownMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: PhoneDownAccent = PhoneDownAccent.Neutral,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = value,
            color = accent.color(),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = label,
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
@Suppress("FunctionName")
fun PhoneDownProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = PhoneDownSize.timerRing,
    strokeWidth: Dp = 7.dp,
    content: @Composable () -> Unit,
) {
    val progressTrackColor = PhoneDownDesign.colors.progressTrack
    val progressColor = PhoneDownDesign.colors.progress

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameterOffset = strokePx / 2
            val arcSize = size.toPx() - strokePx
            drawArc(
                color = progressTrackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(diameterOffset, diameterOffset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                topLeft = Offset(diameterOffset, diameterOffset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@Composable
@Suppress("FunctionName")
fun PhoneDownInlineStatus(
    text: String,
    accent: PhoneDownAccent,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = accent.color(),
        style = MaterialTheme.typography.bodyMedium,
    )
}

enum class PhoneDownAccent {
    Neutral,
    Progress,
    Success,
    Warning,
    Danger,
}

@Composable
private fun PhoneDownAccent.color() =
    when (this) {
        PhoneDownAccent.Neutral -> PhoneDownDesign.colors.textPrimary
        PhoneDownAccent.Progress -> PhoneDownDesign.colors.progress
        PhoneDownAccent.Success -> PhoneDownDesign.colors.success
        PhoneDownAccent.Warning -> PhoneDownDesign.colors.warning
        PhoneDownAccent.Danger -> PhoneDownDesign.colors.danger
    }
