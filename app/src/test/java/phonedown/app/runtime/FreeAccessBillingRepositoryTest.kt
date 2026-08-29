package phonedown.app.runtime

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import phonedown.core.model.PRO_LIFETIME_PRODUCT_ID
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ProProduct
import phonedown.core.model.ProProductType

class FreeAccessBillingRepositoryTest {
    @Test
    fun `free release grants permanent pro entitlement`() =
        runTest {
            val repository = FreeAccessBillingRepository()

            assertEquals(ProEntitlement.Pro(expiryDateMillis = null), repository.entitlement.first())
        }

    @Test
    fun `free release exposes no products or purchases`() =
        runTest {
            val repository = FreeAccessBillingRepository()

            assertTrue(repository.products.first().isEmpty())
            assertTrue(repository.purchases.first().isEmpty())
        }

    @Test
    fun `purchase launch is impossible in free release`() =
        runTest {
            val sampleProduct =
                ProProduct(
                    id = PRO_LIFETIME_PRODUCT_ID,
                    type = ProProductType.Lifetime,
                    priceAmountMicros = 0L,
                    formattedPrice = "",
                    billingPeriod = null,
                )

            try {
                FreeAccessBillingRepository().launchPurchaseFlow(sampleProduct)
                fail("Expected public-free purchase launch to fail")
            } catch (exception: IllegalStateException) {
                assertEquals("Unsupported operation in public free mode", exception.message)
            }
        }
}
