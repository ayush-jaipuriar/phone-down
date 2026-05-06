package phonedown.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownSpacing
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
@Suppress("FunctionName")
fun InsightsCalendarStrip(
    selectedDateEpochDay: Long?,
    onDaySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    // Determine the Monday of the current week
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val weekDays = (0..6).map { monday.plusDays(it.toLong()) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        weekDays.forEach { date ->
            val epochDay = date.toEpochDay()
            val isToday = date.isEqual(today)
            val isSelected = selectedDateEpochDay == epochDay
            val isFuture = date.isAfter(today)

            CalendarDayItem(
                date = date,
                isToday = isToday,
                isSelected = isSelected,
                isFuture = isFuture,
                onClick = { onDaySelected(epochDay) },
            )
        }
    }
}

@Composable
private fun CalendarDayItem(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val dayNumber = date.dayOfMonth.toString()

    val backgroundColor = when {
        isToday -> PhoneDownDesign.colors.progress
        isSelected -> PhoneDownDesign.colors.surfaceRaised
        else -> PhoneDownDesign.colors.background
    }

    val textColor = when {
        isToday -> PhoneDownDesign.colors.surface
        isFuture -> PhoneDownDesign.colors.textTertiary
        else -> PhoneDownDesign.colors.textPrimary
    }

    val secondaryTextColor = when {
        isToday -> PhoneDownDesign.colors.surface
        isFuture -> PhoneDownDesign.colors.textTertiary
        else -> PhoneDownDesign.colors.textSecondary
    }

    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .then(
                    if (!isFuture) {
                        Modifier.clickable(role = Role.Button, onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = dayName,
            color = secondaryTextColor,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            text = dayNumber,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
