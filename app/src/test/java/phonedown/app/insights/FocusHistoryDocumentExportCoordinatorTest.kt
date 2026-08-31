package phonedown.app.insights

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusHistoryDocumentExportCoordinatorTest {
    @Test
    fun `returned destination after coordinator recreation starts export`() {
        val exportedDestinations = mutableListOf<String>()
        val messages = mutableListOf<String>()
        createCoordinator(exportedDestinations, messages)

        val recreatedCoordinator = createCoordinator(exportedDestinations, messages)
        recreatedCoordinator.onDocumentResult("content://focus-history")

        assertEquals(listOf("content://focus-history"), exportedDestinations)
        assertEquals(listOf("Focus history exported."), messages)
    }

    @Test
    fun `cancelled document selection does nothing`() {
        val exportedDestinations = mutableListOf<String>()
        val messages = mutableListOf<String>()
        val coordinator = createCoordinator(exportedDestinations, messages)

        coordinator.onDocumentResult(null)

        assertTrue(exportedDestinations.isEmpty())
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `failed export reports failure message`() {
        val messages = mutableListOf<String>()
        val coordinator =
            FocusHistoryDocumentExportCoordinator<String>(
                export = { _, onComplete -> onComplete(false) },
                notify = messages::add,
            )

        coordinator.onDocumentResult("content://focus-history")

        assertEquals(listOf("Unable to export focus history."), messages)
    }

    private fun createCoordinator(
        exportedDestinations: MutableList<String>,
        messages: MutableList<String>,
    ): FocusHistoryDocumentExportCoordinator<String> =
        FocusHistoryDocumentExportCoordinator(
            export = { destination, onComplete ->
                exportedDestinations += destination
                onComplete(true)
            },
            notify = messages::add,
        )
}
