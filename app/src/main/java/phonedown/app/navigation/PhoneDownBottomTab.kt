package phonedown.app.navigation

import androidx.compose.runtime.Immutable

@Immutable
data class PhoneDownBottomTab(
    val route: PhoneDownRoute,
    val label: String,
)

val phoneDownBottomTabs =
    listOf(
        PhoneDownBottomTab(PhoneDownRoute.Focus, "Focus"),
        PhoneDownBottomTab(PhoneDownRoute.Insights, "Insights"),
        PhoneDownBottomTab(PhoneDownRoute.Settings, "Settings"),
    )
