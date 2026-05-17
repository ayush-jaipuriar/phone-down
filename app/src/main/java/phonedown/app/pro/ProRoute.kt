package phonedown.app.pro

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import phonedown.feature.pro.ProScreenMessage
import phonedown.feature.pro.ProScreenMessageTone
import phonedown.feature.pro.ProScreenState
import phonedown.feature.pro.ProScreen

@Composable
@Suppress("FunctionName")
fun ProRoute(
    onBack: () -> Unit,
    viewModel: ProViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProScreen(
        uiState =
            ProScreenState(
                products = uiState.products,
                isLoadingProducts = uiState.isLoadingProducts,
                productLoadError = uiState.productLoadError,
                purchaseInProgressProductId = uiState.purchaseInProgressProductId,
                isRestoringPurchases = uiState.isRestoringPurchases,
                isProUser = uiState.isProUser,
                hasManageableSubscription = uiState.hasManageableSubscription,
                message =
                    uiState.message?.let { message ->
                        ProScreenMessage(
                            title = message.title,
                            body = message.body,
                            tone =
                                when (message.tone) {
                                    ProMessageTone.Info -> ProScreenMessageTone.Info
                                    ProMessageTone.Success -> ProScreenMessageTone.Success
                                    ProMessageTone.Error -> ProScreenMessageTone.Error
                                },
                        )
                    },
            ),
        onPurchase = viewModel::purchase,
        onRestore = viewModel::restorePurchases,
        onRetryLoadProducts = viewModel::retryLoadProducts,
        onDismissMessage = viewModel::dismissMessage,
        onManageSubscriptions = {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/account/subscriptions?package=phonedown.app"),
                ),
            )
        },
        onBack = onBack,
    )
}
