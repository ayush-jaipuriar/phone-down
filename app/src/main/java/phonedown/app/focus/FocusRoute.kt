package phonedown.app.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import phonedown.feature.focus.FocusScreen
import phonedown.feature.focus.state.FocusEvent

@Composable
@Suppress("FunctionName")
fun FocusRoute(
    onStartFocusClick: (Long) -> Unit,
    onRetrySensorsClick: (Long) -> Unit,
    viewModel: FocusViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FocusScreen(
        uiState = uiState,
        onEvent = { event ->
            viewModel.onEvent(event)
            if (event == FocusEvent.StartClicked) {
                onStartFocusClick(uiState.selectedDurationSeconds)
            }
            if (event == FocusEvent.RetrySensorsClicked) {
                onRetrySensorsClick(uiState.selectedDurationSeconds)
            }
        },
    )
}
