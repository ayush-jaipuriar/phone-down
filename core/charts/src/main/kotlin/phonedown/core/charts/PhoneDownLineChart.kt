package phonedown.core.charts

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import phonedown.core.designsystem.PhoneDownDesign

@Composable
@Suppress("FunctionName")
fun PhoneDownLineChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    val lineColor = PhoneDownDesign.colors.progress
    val labelTextColor = 0xFF888888.toInt()

    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val labelAreaHeight = 26f * density
        val chartHeight = size.height - labelAreaHeight
        val chartWidth = size.width

        val points =
            values.mapIndexed { index, value ->
                val x = chartWidth * index / (values.size - 1).coerceAtLeast(1)
                val y = chartHeight - (value / maxValue) * chartHeight
                Offset(x, y)
            }

        if (points.size >= 2) {
            val path = Path()
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cx1 = (prev.x + curr.x) / 2
                path.cubicTo(cx1, prev.y, cx1, curr.y, curr.x, curr.y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 4f * density,
                    center = point,
                )
            }
        }

        labels.forEachIndexed { index, label ->
            if (index < values.size) {
                val x = chartWidth * index / (values.size - 1).coerceAtLeast(1)
                val textWidth =
                    Paint()
                        .apply {
                            textSize = 10f * density
                        }.measureText(label)

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    (x - textWidth / 2).coerceIn(0f, chartWidth - textWidth),
                    chartHeight + 16f * density,
                    Paint().apply {
                        textSize = 10f * density
                        color = labelTextColor
                        textAlign = Paint.Align.LEFT
                    },
                )
            }
        }
    }
}
