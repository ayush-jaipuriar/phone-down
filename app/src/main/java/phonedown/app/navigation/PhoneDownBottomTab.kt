package phonedown.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class PhoneDownBottomTab(
    val route: PhoneDownRoute,
    val label: String,
    val icon: ImageVector,
)

val phoneDownBottomTabs =
    listOf(
        PhoneDownBottomTab(PhoneDownRoute.Focus, "Focus", Icons.Rounded.Adjust),
        PhoneDownBottomTab(PhoneDownRoute.Insights, "Insights", Icons.Rounded.BarChart),
        PhoneDownBottomTab(PhoneDownRoute.Settings, "Settings", Icons.Rounded.Settings),
    )
