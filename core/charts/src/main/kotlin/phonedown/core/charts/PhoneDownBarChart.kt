package phonedown.core.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import phonedown.core.designsystem.PhoneDownDesign

private const val BAR_CORNER_PX = 4f

@Composable
fun PhoneDownBarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    val barColor = PhoneDownDesign.colors.progress
    val trackColor = PhoneDownDesign.colors.borderSubtle
    val labelTextColor = 0xFF888888.toInt()

    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val labelAreaHeight = 26f * density
        val chartHeight = size.height - labelAreaHeight
        val barAreaWidth = size.width
        val barCount = values.size
        val totalGaps = barCount + 1
        val barWidth = barAreaWidth / ((barCount * 2) + 1)
        val gap = barWidth

        values.forEachIndexed { index, value ->
            val left = gap + (index * (barWidth + gap))
            val barHeight = (value / maxValue) * chartHeight
            val top = chartHeight - barHeight

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(left, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(BAR_CORNER_PX, BAR_CORNER_PX),
            )
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(BAR_CORNER_PX, BAR_CORNER_PX),
            )

            if (index < labels.size) {
                val label = labels[index]
                val textWidth = android.graphics.Paint().apply {
                    textSize = 10f * density
                }.measureText(label)

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    left + (barWidth / 2) - (textWidth / 2),
                    chartHeight + 16f * density,
                    android.graphics.Paint().apply {
                        textSize = 10f * density
                        color = labelTextColor
                        textAlign = android.graphics.Paint.Align.LEFT
                    },
                )
            }
        }
    }
}
