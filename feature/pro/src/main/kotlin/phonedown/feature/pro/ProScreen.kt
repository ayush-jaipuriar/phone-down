package phonedown.feature.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import phonedown.core.designsystem.PhoneDownButton
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownTopBar
import phonedown.core.model.ProProduct
import phonedown.core.model.ProProductType
import phonedown.core.model.ThemeMode

@Composable
@Suppress("FunctionName")
fun ProScreen(
    uiState: ProScreenState,
    onPurchase: (ProProduct) -> Unit,
    onRestore: () -> Unit,
    onRetryLoadProducts: () -> Unit,
    onDismissMessage: () -> Unit,
    onManageSubscriptions: () -> Unit,
    onBack: () -> Unit,
) {
    PhoneDownScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        PhoneDownTopBar(
            title = "Phone Down Pro",
            trailing = {
                Text(
                    text = "Close",
                    color = PhoneDownDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable(onClick = onBack),
                )
            },
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.lg))

        when {
            uiState.isLoadingProducts && uiState.products.isEmpty() -> {
                LoadingState()
            }

            uiState.products.isEmpty() && uiState.productLoadError != null -> {
                ProductLoadErrorState(
                    message = uiState.productLoadError,
                    onRetry = onRetryLoadProducts,
                )
            }

            else -> {
                ContentState(
                    uiState = uiState,
                    onPurchase = onPurchase,
                    onRestore = onRestore,
                    onManageSubscriptions = onManageSubscriptions,
                )
            }
        }

        uiState.message?.let { message ->
            AlertDialog(
                onDismissRequest = onDismissMessage,
                title = { Text(message.title) },
                text = { Text(message.body) },
                confirmButton = {
                    TextButton(onClick = onDismissMessage) {
                        Text("OK")
                    }
                },
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            color = PhoneDownDesign.colors.progress,
            strokeWidth = 2.dp,
        )
        Text(
            text = "Loading Play Store options...",
            style = MaterialTheme.typography.bodyLarge,
            color = PhoneDownDesign.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "We are fetching live pricing and availability from Google Play so the paywall stays accurate for this device and region.",
            style = MaterialTheme.typography.bodyMedium,
            color = PhoneDownDesign.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProductLoadErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Play Store products unavailable",
            style = MaterialTheme.typography.headlineSmall,
            color = PhoneDownDesign.colors.textPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = PhoneDownDesign.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        PhoneDownButton(
            text = "Retry",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ContentState(
    uiState: ProScreenState,
    onPurchase: (ProProduct) -> Unit,
    onRestore: () -> Unit,
    onManageSubscriptions: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeroCopy(
            isProUser = uiState.isProUser,
            hasManageableSubscription = uiState.hasManageableSubscription,
            onManageSubscriptions = onManageSubscriptions,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            uiState.products.forEach { product ->
                ProductCard(
                    product = product,
                    isRecommended = product.type == ProProductType.Yearly,
                    isBusy = uiState.purchaseInProgressProductId == product.id,
                    enabled = !uiState.hasBlockingAction,
                    onClick = { onPurchase(product) },
                )
            }
        }

        Text(
            text = "Backup stays tied to your Google account. Pro entitlement stays tied to Google Play purchases.",
            style = MaterialTheme.typography.bodySmall,
            color = PhoneDownDesign.colors.textTertiary,
            textAlign = TextAlign.Center,
        )

        PhoneDownButton(
            text = if (uiState.isRestoringPurchases) "Restoring..." else "Restore Purchases",
            onClick = onRestore,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.hasBlockingAction,
            quiet = true,
        )
    }
}

@Composable
private fun HeroCopy(
    isProUser: Boolean,
    hasManageableSubscription: Boolean,
    onManageSubscriptions: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (isProUser) "Pro is active" else "Unlock your full focus system",
            style = MaterialTheme.typography.headlineSmall,
            color = PhoneDownDesign.colors.textPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text =
                if (isProUser) {
                    "Your account already has Pro access. You can restore, manage billing, or switch plans through Google Play."
                } else {
                    "Get advanced insights, unlimited history, and Drive backup with live Google Play pricing."
                },
            style = MaterialTheme.typography.bodyLarge,
            color = PhoneDownDesign.colors.textSecondary,
            textAlign = TextAlign.Center,
        )

        if (isProUser && hasManageableSubscription) {
            PhoneDownButton(
                text = "Manage Subscription",
                onClick = onManageSubscriptions,
                modifier = Modifier.fillMaxWidth(),
                quiet = true,
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: ProProduct,
    isRecommended: Boolean,
    isBusy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isRecommended) PhoneDownDesign.colors.toggle else PhoneDownDesign.colors.borderSubtle
    val backgroundColor = if (isRecommended) PhoneDownDesign.colors.surface else PhoneDownDesign.colors.surfaceRaised

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(backgroundColor)
                .border(2.dp, borderColor, MaterialTheme.shapes.large)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(PhoneDownSpacing.card),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = productLabel(product.type),
                        style = MaterialTheme.typography.titleSmall,
                        color = PhoneDownDesign.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (isRecommended) {
                        Box(
                            modifier =
                                Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(PhoneDownDesign.colors.toggle)
                                    .padding(horizontal = PhoneDownSpacing.xs, vertical = 2.dp),
                        ) {
                            Text(
                                text = "Best Value",
                                style = MaterialTheme.typography.labelSmall,
                                color = PhoneDownDesign.colors.surface,
                            )
                        }
                    }
                }

                Text(
                    text = productSubtitle(product),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PhoneDownDesign.colors.textSecondary,
                )
            }

            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PhoneDownDesign.colors.progress,
                    strokeWidth = 2.dp,
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
                ) {
                    Text(
                        text = product.formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        color = PhoneDownDesign.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = purchaseCta(product.type),
                        style = MaterialTheme.typography.labelMedium,
                        color = PhoneDownDesign.colors.textSecondary,
                    )
                }
            }
        }
    }
}

private fun productLabel(type: ProProductType): String =
    when (type) {
        ProProductType.Monthly -> "Monthly"
        ProProductType.Yearly -> "Yearly"
        ProProductType.Lifetime -> "Lifetime"
    }

private fun productSubtitle(product: ProProduct): String =
    when (product.type) {
        ProProductType.Monthly -> "Billed every month. Cancel anytime in Google Play."
        ProProductType.Yearly -> "Billed once a year. Lower long-term cost for regular use."
        ProProductType.Lifetime -> "One-time purchase. No recurring charges."
    }

private fun purchaseCta(type: ProProductType): String =
    when (type) {
        ProProductType.Monthly -> "Start monthly"
        ProProductType.Yearly -> "Start yearly"
        ProProductType.Lifetime -> "Unlock forever"
    }

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun ProScreenPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        ProScreen(
            uiState =
                ProScreenState(
                    products =
                        listOf(
                            ProProduct(
                                id = "monthly",
                                type = ProProductType.Monthly,
                                priceAmountMicros = 4_990_000,
                                formattedPrice = "$4.99",
                                billingPeriod = "P1M",
                            ),
                            ProProduct(
                                id = "yearly",
                                type = ProProductType.Yearly,
                                priceAmountMicros = 29_990_000,
                                formattedPrice = "$29.99",
                                billingPeriod = "P1Y",
                            ),
                            ProProduct(
                                id = "lifetime",
                                type = ProProductType.Lifetime,
                                priceAmountMicros = 79_990_000,
                                formattedPrice = "$79.99",
                                billingPeriod = null,
                            ),
                        ),
                ),
            onPurchase = {},
            onRestore = {},
            onRetryLoadProducts = {},
            onDismissMessage = {},
            onManageSubscriptions = {},
            onBack = {},
        )
    }
}

data class ProScreenState(
    val products: List<ProProduct> = emptyList(),
    val isLoadingProducts: Boolean = true,
    val productLoadError: String? = null,
    val purchaseInProgressProductId: String? = null,
    val isRestoringPurchases: Boolean = false,
    val isProUser: Boolean = false,
    val hasManageableSubscription: Boolean = false,
    val message: ProScreenMessage? = null,
) {
    val hasBlockingAction: Boolean
        get() = purchaseInProgressProductId != null || isRestoringPurchases
}

data class ProScreenMessage(
    val title: String,
    val body: String,
    val tone: ProScreenMessageTone,
)

enum class ProScreenMessageTone {
    Info,
    Success,
    Error,
}
