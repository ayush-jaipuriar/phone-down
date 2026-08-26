@file:Suppress("UnusedPrivateProperty")

package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.repository.SessionRepository
import java.time.LocalDate
import java.time.ZoneId

class GetDayInsightsUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(epochDay: Long): InsightSummary {
        val date = LocalDate.ofEpochDay(epochDay)
        val startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis =
            date
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() - 1

        val sessions = sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()
        return GetTodayInsightsUseCase.summarize(sessions)
    }
}
