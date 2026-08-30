package phonedown.app.insights

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import phonedown.feature.insights.InsightsContent

@Composable
@Suppress("FunctionName")
fun InsightsRoute(viewModel: InsightsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingCsv by remember { mutableStateOf<String?>(null) }
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            val csv = pendingCsv
            pendingCsv = null
            if (uri != null && csv != null) {
                val exportSucceeded =
                    runCatching {
                        requireNotNull(context.contentResolver.openOutputStream(uri)).use { output ->
                            output.write(csv.toByteArray(Charsets.UTF_8))
                        }
                    }.isSuccess
                val message =
                    if (exportSucceeded) "Focus history exported." else "Unable to export focus history."
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

    InsightsContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onExport = {
            viewModel.prepareFocusHistoryExport { history ->
                pendingCsv = formatFocusHistoryCsv(history)
                exportLauncher.launch("phone-down-focus-history.csv")
            }
        },
        onDaySelected = viewModel::onDaySelected,
        onBackToToday = viewModel::onBackToToday,
    )
}
