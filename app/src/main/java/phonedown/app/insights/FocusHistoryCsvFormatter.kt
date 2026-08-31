package phonedown.app.insights

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import phonedown.domain.insights.SessionHistoryItem
import java.io.OutputStream
import java.time.Instant
import kotlin.coroutines.CoroutineContext

private const val CSV_HEADER = "session_id,started_at,planned_seconds,focused_seconds,result,clean,broken"
internal const val FOCUS_HISTORY_OUTPUT_MODE = "wt"

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

suspend fun exportFocusHistoryCsv(
    loadHistory: suspend () -> List<SessionHistoryItem>,
    openOutput: () -> OutputStream?,
    dispatcher: CoroutineContext = Dispatchers.IO,
): Boolean =
    withContext(dispatcher) {
        runCatching {
            val csv = formatFocusHistoryCsv(loadHistory())
            requireNotNull(openOutput()).use { output ->
                output.write(csv.toByteArray(Charsets.UTF_8))
            }
        }.isSuccess
    }
