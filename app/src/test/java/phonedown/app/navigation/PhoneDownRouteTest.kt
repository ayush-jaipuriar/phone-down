package phonedown.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneDownRouteTest {
    @Test
    fun routePathsAreStable() {
        assertEquals("onboarding", PhoneDownRoute.Onboarding.path)
        assertEquals("focus", PhoneDownRoute.Focus.path)
        assertEquals("insights", PhoneDownRoute.Insights.path)
        assertEquals("settings", PhoneDownRoute.Settings.path)
        assertEquals("account", PhoneDownRoute.Account.path)
        assertEquals("pro", PhoneDownRoute.Pro.path)
    }
}
