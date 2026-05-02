package phonedown.app.runtime

object FocusSessionServiceContract {
    const val ACTION_START = "phonedown.app.action.START_FOCUS_SESSION"
    const val ACTION_END = "phonedown.app.action.END_FOCUS_SESSION"
    const val ACTION_RETRY_SENSORS = "phonedown.app.action.RETRY_FOCUS_SENSORS"
    const val EXTRA_OPEN_FOCUS = "phonedown.app.extra.OPEN_FOCUS"
    const val EXTRA_PLANNED_DURATION_SECONDS = "phonedown.app.extra.PLANNED_DURATION_SECONDS"
}
