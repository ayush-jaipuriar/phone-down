package phonedown.app.insights

internal class FocusHistoryDocumentExportCoordinator<Destination : Any>(
    private val export: (Destination, (Boolean) -> Unit) -> Unit,
    private val notify: (String) -> Unit,
) {
    fun onDocumentResult(destination: Destination?) {
        destination ?: return
        export(destination) { succeeded ->
            notify(focusHistoryExportMessage(succeeded))
        }
    }
}

private fun focusHistoryExportMessage(succeeded: Boolean): String =
    if (succeeded) "Focus history exported." else "Unable to export focus history."
