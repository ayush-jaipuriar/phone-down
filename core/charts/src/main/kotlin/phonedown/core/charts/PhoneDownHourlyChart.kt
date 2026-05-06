package phonedown.core.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import phonedown.core.designsystem.PhoneDownDesign

@Composable
fun PhoneDownHourlyChart(
    values: List<Int>,
    modifier: Modifier = Modifier,
) {
    val barColor = PhoneDownDesign.colors.progress
    val trackColor = PhoneDownDesign.colors.borderSubtle
    val labelTextColor = 0xFF888888.toInt()

    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
        val labelAreaHeight = 22f * density
        val chartHeight = size.height - labelAreaHeight
        val barCount = values.size
        val barWidth = size.width / (barCount * 1.5f + 0.5f)
        val gap = barWidth * 0.5f

        values.forEachIndexed { index, value ->
            val left = gap + index * (barWidth + gap)
            val barHeight = (value.toFloat() / maxValue) * chartHeight
            val top = chartHeight - barHeight

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(left, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(2f, 2f),
            )
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2f, 2f),
            )
        }

        val hourLabels = listOf(0, 6, 12, 18)
        hourLabels.forEach { hour ->
            val left = gap + hour * (barWidth + gap)
            val label =
                when (hour) {
                    0 -> "12a"
                    12 -> "12p"
                    in 1..11 -> "${hour}a"
                    else -> "${hour - 12}p"
                }
            val textWidth =
                android.graphics
                    .Paint()
                    .apply {
                        textSize = 9f * density
                    }.measureText(label)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                left + barWidth / 2 - textWidth / 2,
                chartHeight + 16f * density,
                android.graphics.Paint().apply {
                    textSize = 9f * density
                    color = labelTextColor
                    textAlign = android.graphics.Paint.Align.LEFT
                },
            )
        }
    }
}
