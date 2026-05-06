package phonedown.app.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test
import phonedown.app.navigation.PhoneDownRoute

class InitialRouteDecisionTest {
    @Test
    fun `fresh install with no focus extra routes to onboarding`() {
        val route = decideInitialRoute(openFocusExtra = null, onboardingCompleted = false)
        assertEquals(PhoneDownRoute.Onboarding, route)
    }

    @Test
    fun `fresh install with open focus extra routes to focus`() {
        val route = decideInitialRoute(openFocusExtra = true, onboardingCompleted = false)
        assertEquals(PhoneDownRoute.Focus, route)
    }

    @Test
    fun `fresh install with open focus extra false routes to onboarding`() {
        val route = decideInitialRoute(openFocusExtra = false, onboardingCompleted = false)
        assertEquals(PhoneDownRoute.Onboarding, route)
    }

    @Test
    fun `completed onboarding routes to focus`() {
        val route = decideInitialRoute(openFocusExtra = null, onboardingCompleted = true)
        assertEquals(PhoneDownRoute.Focus, route)
    }

    @Test
    fun `completed onboarding with open focus extra routes to focus`() {
        val route = decideInitialRoute(openFocusExtra = true, onboardingCompleted = true)
        assertEquals(PhoneDownRoute.Focus, route)
    }

    @Test
    fun `completed onboarding with open focus extra false routes to focus`() {
        val route = decideInitialRoute(openFocusExtra = false, onboardingCompleted = true)
        assertEquals(PhoneDownRoute.Focus, route)
    }

    @Test
    fun `open focus extra takes priority over onboarding not completed`() {
        val route = decideInitialRoute(openFocusExtra = true, onboardingCompleted = false)
        assertEquals(PhoneDownRoute.Focus, route)
    }

    companion object {
        fun decideInitialRoute(
            openFocusExtra: Boolean?,
            onboardingCompleted: Boolean,
        ): PhoneDownRoute =
            when {
                openFocusExtra == true -> PhoneDownRoute.Focus
                onboardingCompleted -> PhoneDownRoute.Focus
                else -> PhoneDownRoute.Onboarding
            }
    }
}
