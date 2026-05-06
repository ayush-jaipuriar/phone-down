package phonedown.app.pro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import phonedown.core.model.ProProduct
import phonedown.core.model.repository.BillingRepository
import javax.inject.Inject

@HiltViewModel
class ProViewModel
    @Inject
    constructor(
        private val billingRepository: BillingRepository,
    ) : ViewModel() {
        val products: StateFlow<List<ProProduct>> =
            billingRepository.products
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptyList(),
                )

        init {
            viewModelScope.launch {
                billingRepository.loadProducts()
            }
        }

        fun purchase(product: ProProduct) {
            viewModelScope.launch {
                billingRepository.launchPurchaseFlow(product)
            }
        }

        fun restorePurchases() {
            viewModelScope.launch {
                billingRepository.restorePurchases()
            }
        }
    }
