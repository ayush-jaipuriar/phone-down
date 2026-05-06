package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.repository.SessionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GetHourlyFocusUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): List<HourFocus> {
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(clock.currentTimeMillis()), ZoneId.systemDefault())
        return invoke(today)
    }

    suspend fun invoke(date: LocalDate): List<HourFocus> {
        val startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val sessions = sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()

        val hourlyFocus = LongArray(24)
        for (session in sessions) {
            val startedInstant = Instant.ofEpochMilli(session.startedAtEpochMillis)
            val localDateTime = startedInstant.atZone(ZoneId.systemDefault())
            val hour = localDateTime.hour
            hourlyFocus[hour] += session.validFocusSeconds
        }

        return (0..23).map { hour ->
            HourFocus(
                hour = hour,
                focusMinutes = (hourlyFocus[hour] / 60L).toInt(),
            )
        }
    }
}
