package phonedown.app.insights

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import phonedown.core.model.SessionResult
import phonedown.domain.insights.SessionHistoryItem
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.Executors

class FocusHistoryCsvExporterTest {
    private val history =
        listOf(
            SessionHistoryItem(
                sessionId = "session-1",
                startedAtEpochMillis = 0L,
                plannedDurationSeconds = 1_500L,
                validFocusSeconds = 1_200L,
                result = SessionResult.CleanCompleted,
                clean = true,
                broken = false,
            ),
        )

    @Test
    fun `document output mode requests truncation`() {
        assertEquals("wt", FOCUS_HISTORY_OUTPUT_MODE)
    }

    @Test
    fun `writes complete CSV and closes output`() =
        runTest {
            val output = TrackingOutputStream()

            val result =
                exportFocusHistoryCsv(
                    loadHistory = { history },
                    openOutput = { output },
                    dispatcher = coroutineContext,
                )

            assertTrue(result)
            assertTrue(output.closed)
            assertEquals(formatFocusHistoryCsv(history), output.toString(Charsets.UTF_8.name()))
        }

    @Test
    fun `returns false when provider supplies no output stream`() =
        runTest {
            val result =
                exportFocusHistoryCsv(
                    loadHistory = { history },
                    openOutput = { null },
                    dispatcher = coroutineContext,
                )

            assertFalse(result)
        }

    @Test
    fun `returns false and closes output when write fails`() =
        runTest {
            val output = ThrowingOutputStream()

            val result =
                exportFocusHistoryCsv(
                    loadHistory = { history },
                    openOutput = { output },
                    dispatcher = coroutineContext,
                )

            assertFalse(result)
            assertTrue(output.closed)
        }

    @Test
    fun `loads formats and writes on supplied background dispatcher`() =
        runTest {
            val executor = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "csv-export-test") }
            val dispatcher = executor.asCoroutineDispatcher()
            var loadThread = ""
            var openThread = ""
            val output = ThreadTrackingOutputStream()
            try {
                val result =
                    exportFocusHistoryCsv(
                        loadHistory = {
                            loadThread = Thread.currentThread().name
                            history
                        },
                        openOutput = {
                            openThread = Thread.currentThread().name
                            output
                        },
                        dispatcher = dispatcher,
                    )

                assertTrue(result)
                assertTrue(loadThread.contains("csv-export-test"))
                assertTrue(openThread.contains("csv-export-test"))
                assertTrue(output.writeThread.contains("csv-export-test"))
            } finally {
                dispatcher.close()
                executor.shutdownNow()
            }
        }
}

private class ThreadTrackingOutputStream : ByteArrayOutputStream() {
    var writeThread = ""

    override fun write(bytes: ByteArray) {
        writeThread = Thread.currentThread().name
        super.write(bytes)
    }
}

private class TrackingOutputStream : ByteArrayOutputStream() {
    var closed = false

    override fun close() {
        closed = true
        super.close()
    }
}

private class ThrowingOutputStream : ByteArrayOutputStream() {
    var closed = false

    override fun write(bytes: ByteArray): Nothing = throw IOException("write failed")

    override fun close() {
        closed = true
        super.close()
    }
}
