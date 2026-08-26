package phonedown.app.pro

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import phonedown.core.model.BillingEvent
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ProProduct
import phonedown.core.model.ProProductType
import phonedown.core.model.ProPurchase
import phonedown.core.model.ProPurchaseState
import phonedown.core.model.repository.BillingRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ProViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads products`() =
        runTest(testDispatcher) {
            val billingRepo = FakeBillingRepository()

            createViewModel(billingRepo = billingRepo)
            advanceUntilIdle()

            assertTrue(billingRepo.loadProductsCalled)
        }

    @Test
    fun `repository flows are reflected in ui state`() =
        runTest(testDispatcher) {
            val products =
                listOf(
                    ProProduct("pro_monthly", ProProductType.Monthly, 4_990_000L, "$4.99", "P1M"),
                    ProProduct("pro_yearly", ProProductType.Yearly, 29_990_000L, "$29.99", "P1Y"),
                )
            val purchases =
                listOf(
                    ProPurchase(
                        productId = "pro_monthly",
                        purchaseToken = "token",
                        state = ProPurchaseState.Acknowledged,
                        purchaseTimeMillis = 1234L,
                    ),
                )
            val billingRepo =
                FakeBillingRepository(
                    products = products,
                    purchases = purchases,
                    entitlement = ProEntitlement.Pro(),
                )

            val viewModel = createViewModel(billingRepo = billingRepo)
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.products.size)
            assertTrue(viewModel.uiState.value.isProUser)
            assertTrue(viewModel.uiState.value.hasManageableSubscription)
            assertFalse(viewModel.uiState.value.isLoadingProducts)
        }

    @Test
    fun `purchase marks product busy and calls repository`() =
        runTest(testDispatcher) {
            val product = ProProduct("pro_monthly", ProProductType.Monthly, 4_990_000L, "$4.99", "P1M")
            val billingRepo = FakeBillingRepository(products = listOf(product))
            val viewModel = createViewModel(billingRepo = billingRepo)
            advanceUntilIdle()

            viewModel.purchase(product)
            runCurrent()

            assertEquals("pro_monthly", viewModel.uiState.value.purchaseInProgressProductId)
            advanceUntilIdle()

            assertTrue(billingRepo.launchPurchaseFlowCalled)
            assertEquals(product, billingRepo.lastPurchasedProduct)
        }

    @Test
    fun `purchase failure clears busy state and shows error`() =
        runTest(testDispatcher) {
            val product = ProProduct("pro_monthly", ProProductType.Monthly, 4_990_000L, "$4.99", "P1M")
            val billingRepo =
                FakeBillingRepository(
                    products = listOf(product),
                    purchaseException = IllegalStateException("Billing unavailable"),
                )
            val viewModel = createViewModel(billingRepo = billingRepo)
            advanceUntilIdle()

            viewModel.purchase(product)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.purchaseInProgressProductId)
            assertEquals(
                "Purchase Unavailable",
                viewModel.uiState.value.message
                    ?.title,
            )
        }

    @Test
    fun `purchase completed event shows success message`() =
        runTest(testDispatcher) {
            val billingRepo = FakeBillingRepository()
            val viewModel = createViewModel(billingRepo = billingRepo)
            advanceUntilIdle()

            billingRepo.emitEvent(BillingEvent.PurchaseCompleted("pro_yearly"))
            advanceUntilIdle()

            assertEquals(
                "Pro Unlocked",
                viewModel.uiState.value.message
                    ?.title,
            )
            assertNull(viewModel.uiState.value.purchaseInProgressProductId)
        }

    @Test
    fun `restorePurchases sets busy state and calls repository`() =
        runTest(testDispatcher) {
            val billingRepo = FakeBillingRepository()
            val viewModel = createViewModel(billingRepo = billingRepo)
            advanceUntilIdle()

            viewModel.restorePurchases()
            runCurrent()

            assertTrue(viewModel.uiState.value.isRestoringPurchases)
            advanceUntilIdle()

            assertTrue(billingRepo.restorePurchasesCalled)
        }

    @Test
    fun `restore no purchases event clears busy state and shows info`() =
        runTest(testDispatcher) {
            val billingRepo = FakeBillingRepository()
            val viewModel = createViewModel(billingRepo = billingRepo)
            advanceUntilIdle()

            viewModel.restorePurchases()
            billingRepo.emitEvent(BillingEvent.RestoreNoPurchases)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isRestoringPurchases)
            assertEquals(
                "No Purchases Found",
                viewModel.uiState.value.message
                    ?.title,
            )
        }

    @Test
    fun `retryLoadProducts clears previous error and reloads`() =
        runTest(testDispatcher) {
            val billingRepo =
                FakeBillingRepository(
                    loadProductsException = IllegalStateException("No products"),
                )
            val viewModel = createViewModel(billingRepo = billingRepo)
            advanceUntilIdle()
            assertEquals("No products", viewModel.uiState.value.productLoadError)

            billingRepo.loadProductsException = null
            billingRepo.productsFlow.value =
                listOf(
                    ProProduct("pro_lifetime", ProProductType.Lifetime, 99_990_000L, "$99.99", null),
                )

            viewModel.retryLoadProducts()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.productLoadError)
            assertEquals(1, viewModel.uiState.value.products.size)
            assertFalse(viewModel.uiState.value.isLoadingProducts)
        }

    @Test
    fun `dismissMessage clears current message`() =
        runTest(testDispatcher) {
            val billingRepo = FakeBillingRepository()
            val viewModel = createViewModel(billingRepo = billingRepo)
            advanceUntilIdle()

            billingRepo.emitEvent(BillingEvent.PurchaseCancelled)
            advanceUntilIdle()
            assertEquals(
                "Purchase Cancelled",
                viewModel.uiState.value.message
                    ?.title,
            )

            viewModel.dismissMessage()

            assertNull(viewModel.uiState.value.message)
        }

    private fun createViewModel(billingRepo: BillingRepository = FakeBillingRepository()): ProViewModel =
        ProViewModel(billingRepository = billingRepo)
}

private class FakeBillingRepository(
    products: List<ProProduct> = emptyList(),
    purchases: List<ProPurchase> = emptyList(),
    entitlement: ProEntitlement = ProEntitlement.Free,
    var loadProductsException: IllegalStateException? = null,
    private val purchaseException: IllegalStateException? = null,
) : BillingRepository {
    val productsFlow = MutableStateFlow(products)
    private val purchasesFlow = MutableStateFlow(purchases)
    private val entitlementFlow = MutableStateFlow(entitlement)
    private val eventsFlow = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 4)

    var loadProductsCalled = false
    var launchPurchaseFlowCalled = false
    var restorePurchasesCalled = false
    var lastPurchasedProduct: ProProduct? = null

    override val products = productsFlow
    override val purchases = purchasesFlow
    override val entitlement = entitlementFlow
    override val events = eventsFlow

    override suspend fun loadProducts() {
        loadProductsCalled = true
        loadProductsException?.let { throw it }
    }

    override suspend fun launchPurchaseFlow(product: ProProduct) {
        launchPurchaseFlowCalled = true
        lastPurchasedProduct = product
        purchaseException?.let { throw it }
    }

    override suspend fun restorePurchases() {
        restorePurchasesCalled = true
    }

    override suspend fun syncPurchases() = Unit

    override suspend fun acknowledgePurchase(purchaseToken: String) = Unit

    suspend fun emitEvent(event: BillingEvent) {
        eventsFlow.emit(event)
    }
}
