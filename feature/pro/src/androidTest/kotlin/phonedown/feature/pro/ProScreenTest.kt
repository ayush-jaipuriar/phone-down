package phonedown.feature.pro

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode

class ProScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun proScreenShowsIncludedCapabilitiesWithoutPaymentControls() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                ProScreen(
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Phone Down Pro").assertIsDisplayed()
        composeRule.onNodeWithText("Advanced insights").assertIsDisplayed()
        composeRule.onNodeWithText("Unlimited history").assertIsDisplayed()
        composeRule.onNodeWithText("Flexible focus controls").assertIsDisplayed()
        composeRule.onNodeWithText("Backup and restore").assertIsDisplayed()

        listOf("Restore Purchases", "Manage Subscription", "Monthly", "Yearly", "Lifetime")
            .forEach { paymentControl ->
                composeRule.onNodeWithText(paymentControl).assertDoesNotExist()
            }
    }
}
