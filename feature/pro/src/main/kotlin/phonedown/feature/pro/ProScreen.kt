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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import phonedown.core.designsystem.PhoneDownButton
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
    products: List<ProProduct>,
    onPurchase: (ProProduct) -> Unit,
    onRestore: () -> Unit,
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

        Column(
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Unlock your full focus potential",
                style = MaterialTheme.typography.headlineSmall,
                color = PhoneDownDesign.colors.textPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Advanced insights, unlimited history, cloud backup, and data export.",
                style = MaterialTheme.typography.bodyLarge,
                color = PhoneDownDesign.colors.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

            Column(
                verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
                modifier = Modifier.fillMaxWidth(),
            ) {
                products.forEach { product ->
                    ProductCard(
                        product = product,
                        isRecommended = product.type == ProProductType.Yearly,
                        onClick = { onPurchase(product) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

            Text(
                text = "Restore Purchases",
                style = MaterialTheme.typography.bodyMedium,
                color = PhoneDownDesign.colors.toggle,
                modifier = Modifier.clickable(onClick = onRestore),
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: ProProduct,
    isRecommended: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isRecommended) PhoneDownDesign.colors.toggle else PhoneDownDesign.colors.borderSubtle
    val backgroundColor = if (isRecommended) PhoneDownDesign.colors.surface else PhoneDownDesign.colors.surfaceRaised

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(backgroundColor)
            .border(2.dp, borderColor, MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(PhoneDownSpacing.card),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
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
                            modifier = Modifier
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

            Text(
                text = product.formattedPrice,
                style = MaterialTheme.typography.titleMedium,
                color = PhoneDownDesign.colors.textPrimary,
                fontWeight = FontWeight.Bold,
            )
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
        ProProductType.Monthly -> "Billed monthly"
        ProProductType.Yearly -> "Save 50% with yearly billing"
        ProProductType.Lifetime -> "Pay once, keep forever"
    }

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun ProScreenPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        ProScreen(
            products = listOf(
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
            onPurchase = {},
            onRestore = {},
            onBack = {},
        )
    }
}
