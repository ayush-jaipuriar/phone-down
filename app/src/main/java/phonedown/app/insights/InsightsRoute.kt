package phonedown.app.insights

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import phonedown.feature.insights.InsightsContent

@Composable
@Suppress("FunctionName")
fun InsightsRoute(viewModel: InsightsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri != null) {
                viewModel.exportFocusHistory(
                    openOutput = {
                        applicationContext.contentResolver.openOutputStream(uri, FOCUS_HISTORY_OUTPUT_MODE)
                    },
                    onComplete = { exportSucceeded ->
                        val message =
                            if (exportSucceeded) "Focus history exported." else "Unable to export focus history."
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

    InsightsContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onExport = { exportLauncher.launch("phone-down-focus-history.csv") },
        onDaySelected = viewModel::onDaySelected,
        onBackToToday = viewModel::onBackToToday,
    )
}
