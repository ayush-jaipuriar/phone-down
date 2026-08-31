package phonedown.app.insights

import phonedown.domain.insights.SessionHistoryItem
import java.time.Instant

private const val CSV_HEADER = "session_id,started_at,planned_seconds,focused_seconds,result,clean,broken"

fun formatFocusHistoryCsv(history: List<SessionHistoryItem>): String =
    buildString {
        append(CSV_HEADER)
        history.forEach { session ->
            append('\n')
            append(
                listOf(
                    session.sessionId,
                    Instant.ofEpochMilli(session.startedAtEpochMillis).toString(),
                    session.plannedDurationSeconds.toString(),
                    session.validFocusSeconds.toString(),
                    session.result?.name.orEmpty(),
                    session.clean.toString(),
                    session.broken.toString(),
                ).joinToString(separator = ",", transform = ::escapeCsvCell),
            )
        }
    }

private fun escapeCsvCell(value: String): String =
    if (value.any { it == ',' || it == '"' || it == '\r' || it == '\n' }) {
        "\"${value.replace("\"", "\"\"")}\""
    } else {
        value
    }
