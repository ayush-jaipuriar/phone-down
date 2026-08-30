package phonedown.app.insights

import org.junit.Assert.assertEquals
import org.junit.Test
import phonedown.core.model.SessionResult
import phonedown.domain.insights.SessionHistoryItem

class FocusHistoryCsvFormatterTest {
    @Test
    fun `formats deterministic header and column order`() {
        val csv =
            formatFocusHistoryCsv(
                listOf(
                    SessionHistoryItem(
                        sessionId = "session-1",
                        startedAtEpochMillis = 1_700_000_000_000L,
                        plannedDurationSeconds = 1500L,
                        validFocusSeconds = 1200L,
                        result = SessionResult.CleanCompleted,
                        clean = true,
                        broken = false,
                    ),
                ),
            )

        assertEquals(
            "session_id,started_at,planned_seconds,focused_seconds,result,clean,broken\n" +
                "session-1,2023-11-14T22:13:20Z,1500,1200,CleanCompleted,true,false",
            csv,
        )
    }

    @Test
    fun `formats nullable result and Boolean values`() {
        val csv =
            formatFocusHistoryCsv(
                listOf(
                    SessionHistoryItem(
                        sessionId = "session-2",
                        startedAtEpochMillis = 0L,
                        plannedDurationSeconds = 900L,
                        validFocusSeconds = 0L,
                        result = null,
                        clean = false,
                        broken = true,
                    ),
                ),
            )

        assertEquals(
            "session_id,started_at,planned_seconds,focused_seconds,result,clean,broken\n" +
                "session-2,1970-01-01T00:00:00Z,900,0,,false,true",
            csv,
        )
    }

    @Test
    fun `escapes commas quotes and line breaks`() {
        val csv =
            formatFocusHistoryCsv(
                listOf(
                    SessionHistoryItem(
                        sessionId = "session,\"quoted\"\nnext\rline",
                        startedAtEpochMillis = 0L,
                        plannedDurationSeconds = 0L,
                        validFocusSeconds = 0L,
                        result = null,
                        clean = false,
                        broken = false,
                    ),
                ),
            )

        assertEquals(
            "session_id,started_at,planned_seconds,focused_seconds,result,clean,broken\n" +
                "\"session,\"\"quoted\"\"\nnext\rline\"" +
                ",1970-01-01T00:00:00Z,0,0,,false,false",
            csv,
        )
    }

    @Test
    fun `formats header for empty history`() {
        assertEquals(
            "session_id,started_at,planned_seconds,focused_seconds,result,clean,broken",
            formatFocusHistoryCsv(emptyList()),
        )
    }
}
