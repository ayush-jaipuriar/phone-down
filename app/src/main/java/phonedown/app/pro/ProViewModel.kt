package phonedown.app.pro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import phonedown.core.model.BillingEvent
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ProProduct
import phonedown.core.model.ProPurchase
import phonedown.core.model.isSubscriptionProductId
import phonedown.core.model.repository.BillingRepository
import javax.inject.Inject

@HiltViewModel
class ProViewModel
    @Inject
    constructor(
        private val billingRepository: BillingRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProUiState())
        val uiState: StateFlow<ProUiState> = _uiState.asStateFlow()

        init {
            observeBillingState()
            observeBillingEvents()
            refreshProducts()
        }

        private fun observeBillingState() {
            viewModelScope.launch {
                combine(
                    billingRepository.products,
                    billingRepository.purchases,
                    billingRepository.entitlement,
                ) { products, purchases, entitlement ->
                    Triple(products, purchases, entitlement)
                }.collect { (products, purchases, entitlement) ->
                    _uiState.update { current ->
                        current.copy(
                            products = products,
                            isLoadingProducts = current.isLoadingProducts && products.isEmpty() && current.productLoadError == null,
                            isProUser = entitlement is ProEntitlement.Pro,
                            hasManageableSubscription =
                                purchases.any { purchase ->
                                    purchase.productId.isSubscriptionProductId()
                                },
                        )
                    }
                }
            }
        }

        private fun observeBillingEvents() {
            viewModelScope.launch {
                billingRepository.events.collect(::handleBillingEvent)
            }
        }

        fun retryLoadProducts() {
            refreshProducts()
        }

        fun purchase(product: ProProduct) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        purchaseInProgressProductId = product.id,
                        message = null,
                    )
                }
                try {
                    billingRepository.launchPurchaseFlow(product)
                } catch (exception: Exception) {
                    _uiState.update {
                        it.copy(
                            purchaseInProgressProductId = null,
                            message =
                                ProMessage(
                                    title = "Purchase Unavailable",
                                    body = exception.message ?: "Unable to start the purchase flow right now.",
                                    tone = ProMessageTone.Error,
                                ),
                        )
                    }
                }
            }
        }

        fun restorePurchases() {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isRestoringPurchases = true,
                        message = null,
                    )
                }
                try {
                    billingRepository.restorePurchases()
                } catch (exception: Exception) {
                    _uiState.update {
                        it.copy(
                            isRestoringPurchases = false,
                            message =
                                ProMessage(
                                    title = "Restore Failed",
                                    body = exception.message ?: "Unable to restore purchases right now.",
                                    tone = ProMessageTone.Error,
                                ),
                        )
                    }
                }
            }
        }

        fun dismissMessage() {
            _uiState.update { it.copy(message = null) }
        }

        private fun refreshProducts() {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoadingProducts = true,
                        productLoadError = null,
                        message = null,
                    )
                }
                try {
                    billingRepository.loadProducts()
                    _uiState.update {
                        it.copy(
                            isLoadingProducts = false,
                            productLoadError = null,
                        )
                    }
                } catch (exception: Exception) {
                    val message = exception.message ?: "Unable to load Play Billing products right now."
                    _uiState.update {
                        it.copy(
                            isLoadingProducts = false,
                            productLoadError = message,
                            message =
                                ProMessage(
                                    title = "Products Unavailable",
                                    body = message,
                                    tone = ProMessageTone.Error,
                                ),
                        )
                    }
                }
            }
        }

        private fun handleBillingEvent(event: BillingEvent) {
            when (event) {
                is BillingEvent.PurchaseCompleted -> {
                    _uiState.update {
                        it.copy(
                            purchaseInProgressProductId = null,
                            message =
                                ProMessage(
                                    title = "Pro Unlocked",
                                    body = purchaseCompletedMessage(event.productId),
                                    tone = ProMessageTone.Success,
                                ),
                        )
                    }
                }

                is BillingEvent.PurchasePending -> {
                    _uiState.update {
                        it.copy(
                            purchaseInProgressProductId = null,
                            message =
                                ProMessage(
                                    title = "Purchase Pending",
                                    body = "Google Play is still processing this purchase. Your Pro access will unlock as soon as the purchase completes.",
                                    tone = ProMessageTone.Info,
                                ),
                        )
                    }
                }

                BillingEvent.PurchaseCancelled -> {
                    _uiState.update {
                        it.copy(
                            purchaseInProgressProductId = null,
                            message =
                                ProMessage(
                                    title = "Purchase Cancelled",
                                    body = "Nothing changed. You can try again whenever you're ready.",
                                    tone = ProMessageTone.Info,
                                ),
                        )
                    }
                }

                is BillingEvent.PurchaseFailed -> {
                    _uiState.update {
                        it.copy(
                            purchaseInProgressProductId = null,
                            message =
                                ProMessage(
                                    title = "Purchase Failed",
                                    body = event.message,
                                    tone = ProMessageTone.Error,
                                ),
                        )
                    }
                }

                is BillingEvent.RestoreCompleted -> {
                    _uiState.update {
                        it.copy(
                            isRestoringPurchases = false,
                            message =
                                ProMessage(
                                    title = "Purchases Restored",
                                    body =
                                        if (event.restoredPurchaseCount == 1) {
                                            "Your Google Play purchase was restored and Pro is active again."
                                        } else {
                                            "Your Google Play purchases were restored and Pro is active again."
                                        },
                                    tone = ProMessageTone.Success,
                                ),
                        )
                    }
                }

                BillingEvent.RestoreNoPurchases -> {
                    _uiState.update {
                        it.copy(
                            isRestoringPurchases = false,
                            message =
                                ProMessage(
                                    title = "No Purchases Found",
                                    body = "No Pro purchases were found for this Google Play account.",
                                    tone = ProMessageTone.Info,
                                ),
                        )
                    }
                }

                is BillingEvent.RestoreFailed -> {
                    _uiState.update {
                        it.copy(
                            isRestoringPurchases = false,
                            message =
                                ProMessage(
                                    title = "Restore Failed",
                                    body = event.message,
                                    tone = ProMessageTone.Error,
                                ),
                        )
                    }
                }
            }
        }

        private fun purchaseCompletedMessage(productId: String): String =
            when {
                productId == phonedown.core.model.PRO_LIFETIME_PRODUCT_ID ->
                    "Lifetime Pro is now active on this device."
                productId.isSubscriptionProductId() ->
                    "Your Pro subscription is active. Google Play will manage renewals and billing."
                else ->
                    "Your purchase completed successfully."
            }
    }

data class ProUiState(
    val products: List<ProProduct> = emptyList(),
    val isLoadingProducts: Boolean = true,
    val productLoadError: String? = null,
    val purchaseInProgressProductId: String? = null,
    val isRestoringPurchases: Boolean = false,
    val isProUser: Boolean = false,
    val hasManageableSubscription: Boolean = false,
    val message: ProMessage? = null,
) {
    val hasBlockingAction: Boolean
        get() = purchaseInProgressProductId != null || isRestoringPurchases
}

data class ProMessage(
    val title: String,
    val body: String,
    val tone: ProMessageTone,
)

enum class ProMessageTone {
    Info,
    Success,
    Error,
}
