package phonedown.app.pro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import phonedown.feature.pro.ProScreen

@Composable
@Suppress("FunctionName")
fun ProRoute(
    onBack: () -> Unit,
    viewModel: ProViewModel = hiltViewModel(),
) {
    val products by viewModel.products.collectAsStateWithLifecycle()

    ProScreen(
        products = products,
        onPurchase = viewModel::purchase,
        onRestore = viewModel::restorePurchases,
        onBack = onBack,
    )
}
