package phonedown.app.insights

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import phonedown.feature.insights.InsightsContent

@Composable
@Suppress("FunctionName")
fun InsightsRoute(
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    InsightsContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
    )
}
