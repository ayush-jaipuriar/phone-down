package phonedown.app.pro

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import phonedown.core.model.ProProduct
import phonedown.core.model.ProProductType
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
    fun `init loads products`() = runTest(testDispatcher) {
        val billingRepo = FakeBillingRepository()
        createViewModel(billingRepo = billingRepo)
        testScheduler.advanceUntilIdle()

        assertTrue(billingRepo.loadProductsCalled)
    }

    @Test
    fun `products flow reflected in ui`() = runTest(testDispatcher) {
        val products = listOf(
            ProProduct("monthly", ProProductType.Monthly, 4_990_000L, "$4.99", "P1M"),
            ProProduct("yearly", ProProductType.Yearly, 29_990_000L, "$29.99", "P1Y"),
        )
        val billingRepo = FakeBillingRepository(products = products)
        val viewModel = createViewModel(billingRepo = billingRepo)

        // Collect products to activate the flow
        val job = launch { viewModel.products.collect {} }
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.products.value.size)
        assertEquals("monthly", viewModel.products.value[0].id)
        job.cancel()
    }

    @Test
    fun `purchase calls repository`() = runTest(testDispatcher) {
        val billingRepo = FakeBillingRepository()
        val viewModel = createViewModel(billingRepo = billingRepo)
        val product = ProProduct("monthly", ProProductType.Monthly, 4_990_000L, "$4.99", "P1M")

        viewModel.purchase(product)
        testScheduler.advanceUntilIdle()

        assertTrue(billingRepo.launchPurchaseFlowCalled)
        assertEquals(product, billingRepo.lastPurchasedProduct)
    }

    @Test
    fun `restorePurchases calls repository`() = runTest(testDispatcher) {
        val billingRepo = FakeBillingRepository()
        val viewModel = createViewModel(billingRepo = billingRepo)

        viewModel.restorePurchases()
        testScheduler.advanceUntilIdle()

        assertTrue(billingRepo.restorePurchasesCalled)
    }

    @Test
    fun `empty products list by default`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.products.value.size)
    }

    private fun createViewModel(
        billingRepo: BillingRepository = FakeBillingRepository(),
    ): ProViewModel = ProViewModel(billingRepository = billingRepo)
}

private class FakeBillingRepository(
    products: List<ProProduct> = emptyList(),
) : BillingRepository {
    private val productsFlow = MutableStateFlow(products)
    var loadProductsCalled = false
    var launchPurchaseFlowCalled = false
    var restorePurchasesCalled = false
    var lastPurchasedProduct: ProProduct? = null

    override val products = productsFlow
    override val purchases = kotlinx.coroutines.flow.flowOf(emptyList<phonedown.core.model.ProPurchase>())
    override val entitlement = kotlinx.coroutines.flow.flowOf(phonedown.core.model.ProEntitlement.Free)

    override suspend fun loadProducts() {
        loadProductsCalled = true
    }

    override suspend fun launchPurchaseFlow(product: ProProduct) {
        launchPurchaseFlowCalled = true
        lastPurchasedProduct = product
    }

    override suspend fun restorePurchases() {
        restorePurchasesCalled = true
    }

    override suspend fun acknowledgePurchase(purchaseToken: String) {}
}
