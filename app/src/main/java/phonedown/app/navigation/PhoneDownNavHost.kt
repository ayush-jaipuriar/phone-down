package phonedown.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import phonedown.feature.account.AccountScreen
import phonedown.feature.focus.FocusScreen
import phonedown.feature.insights.InsightsScreen
import phonedown.feature.onboarding.OnboardingScreen
import phonedown.feature.pro.ProScreen
import phonedown.feature.settings.SettingsScreen

@Composable
@Suppress("FunctionName")
fun PhoneDownApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination.showsTabs()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PhoneDownBottomBar(
                    currentDestination = currentDestination,
                    onTabSelected = { route ->
                        navController.navigate(route.path) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        PhoneDownNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
@Suppress("FunctionName")
private fun PhoneDownNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = PhoneDownRoute.Onboarding.path,
        modifier = modifier,
    ) {
        composable(PhoneDownRoute.Onboarding.path) {
            OnboardingScreen(
                onContinue = {
                    navController.navigate(PhoneDownRoute.Focus.path) {
                        popUpTo(PhoneDownRoute.Onboarding.path) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(PhoneDownRoute.Focus.path) {
            FocusScreen()
        }
        composable(PhoneDownRoute.Insights.path) {
            InsightsScreen()
        }
        composable(PhoneDownRoute.Settings.path) {
            SettingsScreen(
                onAccountClick = { navController.navigate(PhoneDownRoute.Account.path) },
                onProClick = { navController.navigate(PhoneDownRoute.Pro.path) },
            )
        }
        composable(PhoneDownRoute.Account.path) {
            AccountScreen(onBack = navController::popBackStack)
        }
        composable(PhoneDownRoute.Pro.path) {
            ProScreen(onBack = navController::popBackStack)
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun PhoneDownBottomBar(
    currentDestination: NavDestination?,
    onTabSelected: (PhoneDownRoute) -> Unit,
) {
    NavigationBar {
        phoneDownBottomTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentDestination.isRouteSelected(tab.route),
                onClick = { onTabSelected(tab.route) },
                icon = { Text(tab.label.take(1)) },
                label = { Text(tab.label) },
            )
        }
    }
}

private fun NavDestination?.showsTabs(): Boolean = phoneDownBottomTabs.any { tab -> isRouteSelected(tab.route) }

private fun NavDestination?.isRouteSelected(route: PhoneDownRoute): Boolean =
    this?.hierarchy?.any { destination -> destination.route == route.path } == true
