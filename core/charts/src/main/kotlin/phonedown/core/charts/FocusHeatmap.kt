@file:Suppress("MagicNumber")

package phonedown.core.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.domain.insights.HeatmapDay
import java.time.LocalDate

private const val TILE_SIZE_PX = 12f
private const val TILE_GAP_PX = 2f
private const val TOP_LABEL_HEIGHT_PX = 14f
private const val LEFT_LABEL_WIDTH_PX = 28f
private const val CORNER_PX = 2f

@Composable
fun FocusHeatmap(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
) {
    if (days.isEmpty()) return

    val levelColors =
        listOf(
            PhoneDownDesign.colors.borderSubtle,
            Color(0xFF9BE9A8),
            Color(0xFF40C463),
            Color(0xFF30A14E),
            Color(0xFF216E39),
        )

    val canvasHeight = 7 * (TILE_SIZE_PX + TILE_GAP_PX) + TOP_LABEL_HEIGHT_PX + 16f

    var tooltipText by remember { mutableStateOf<String?>(null) }
    val labelColor = PhoneDownDesign.colors.textTertiary

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(4.dp))

        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height((canvasHeight / 2).dp)
                    .pointerInput(days) {
                        detectTapGestures { offset ->
                            val col = ((offset.x - LEFT_LABEL_WIDTH_PX) / (TILE_SIZE_PX + TILE_GAP_PX)).toInt()
                            val row = ((offset.y - TOP_LABEL_HEIGHT_PX) / (TILE_SIZE_PX + TILE_GAP_PX)).toInt()
                            val dayIndex = col * 7 + row
                            if (dayIndex in days.indices) {
                                val day = days[dayIndex]
                                val date = LocalDate.ofEpochDay(day.dateEpochDay)
                                tooltipText = "${date.monthValue}/${date.dayOfMonth}: ${day.focusMinutes}m"
                            }
                        }
                    },
        ) {
            var col = 0
            var lastWeek = -1
            val firstDay = days.first().dateEpochDay

            for (day in days) {
                val date = LocalDate.ofEpochDay(day.dateEpochDay)
                val dow = date.dayOfWeek.value - 1
                val weekOffset = ((day.dateEpochDay - firstDay) / 7).toInt()

                if (weekOffset != lastWeek) {
                    col = weekOffset
                    lastWeek = weekOffset
                }

                val x = LEFT_LABEL_WIDTH_PX + col * (TILE_SIZE_PX + TILE_GAP_PX)
                val y = TOP_LABEL_HEIGHT_PX + dow * (TILE_SIZE_PX + TILE_GAP_PX)
                val color = levelColors.getOrElse(day.level) { levelColors[0] }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(TILE_SIZE_PX, TILE_SIZE_PX),
                    cornerRadius = CornerRadius(CORNER_PX, CORNER_PX),
                )
            }

            listOf("Mon", "", "Wed", "", "Fri", "", "").forEachIndexed { index, label ->
                if (label.isNotEmpty()) {
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        LEFT_LABEL_WIDTH_PX - 4f,
                        TOP_LABEL_HEIGHT_PX + index * (TILE_SIZE_PX + TILE_GAP_PX) + TILE_SIZE_PX,
                        android.graphics.Paint().apply {
                            textSize = 10f * density
                            color = 0xFF888888.toInt()
                            textAlign = android.graphics.Paint.Align.RIGHT
                        },
                    )
                }
            }
        }

        tooltipText?.let { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
            )
            levelColors.forEach { color ->
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawRoundRect(
                        color = color,
                        cornerRadius = CornerRadius(CORNER_PX),
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
