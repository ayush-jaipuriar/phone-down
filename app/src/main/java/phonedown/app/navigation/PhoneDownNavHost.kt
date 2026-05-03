@file:Suppress("LongParameterList")

package phonedown.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import phonedown.app.focus.FocusRoute
import phonedown.app.onboarding.OnboardingRoute
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode
import phonedown.feature.account.AccountScreen
import phonedown.feature.insights.InsightsScreen
import phonedown.feature.pro.ProScreen
import phonedown.feature.settings.SettingsScreen

@Composable
@Suppress("FunctionName")
fun PhoneDownApp(
    themeMode: ThemeMode = ThemeMode.System,
    initialRoute: PhoneDownRoute = PhoneDownRoute.Onboarding,
    onThemeModeSelected: suspend (ThemeMode) -> Unit = {},
    onStartFocusClick: (Long) -> Unit = {},
    onRetrySensorsClick: (Long) -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination.showsTabs()
    val coroutineScope = rememberCoroutineScope()

    PhoneDownTheme(themeMode = themeMode) {
        Scaffold(
            containerColor = PhoneDownDesign.colors.background,
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
                initialRoute = initialRoute,
                themeMode = themeMode,
                onThemeModeSelected = { mode ->
                    coroutineScope.launch {
                        onThemeModeSelected(mode)
                    }
                },
                onStartFocusClick = onStartFocusClick,
                onRetrySensorsClick = onRetrySensorsClick,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun PhoneDownNavHost(
    navController: NavHostController,
    initialRoute: PhoneDownRoute,
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onStartFocusClick: (Long) -> Unit,
    onRetrySensorsClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = initialRoute.path,
        modifier = modifier,
    ) {
        composable(PhoneDownRoute.Onboarding.path) {
            OnboardingRoute(
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
            FocusRoute(
                onStartFocusClick = onStartFocusClick,
                onRetrySensorsClick = onRetrySensorsClick,
            )
        }
        composable(PhoneDownRoute.Insights.path) {
            InsightsScreen()
        }
        composable(PhoneDownRoute.Settings.path) {
            SettingsScreen(
                onAccountClick = { navController.navigate(PhoneDownRoute.Account.path) },
                onProClick = { navController.navigate(PhoneDownRoute.Pro.path) },
                selectedThemeMode = themeMode,
                onThemeModeSelected = onThemeModeSelected,
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
    NavigationBar(
        containerColor = PhoneDownDesign.colors.background,
        tonalElevation = NavigationBarDefaults.Elevation,
    ) {
        phoneDownBottomTabs.forEach { tab ->
            val selected = currentDestination.isRouteSelected(tab.route)
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab.route) },
                icon = { Text(tab.label.take(1)) },
                label = { Text(tab.label) },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = PhoneDownDesign.colors.textPrimary,
                        selectedTextColor = PhoneDownDesign.colors.textPrimary,
                        indicatorColor = PhoneDownDesign.colors.surfaceRaised,
                        unselectedIconColor = PhoneDownDesign.colors.textTertiary,
                        unselectedTextColor = PhoneDownDesign.colors.textTertiary,
                    ),
            )
        }
    }
}

private fun NavDestination?.showsTabs(): Boolean = phoneDownBottomTabs.any { tab -> isRouteSelected(tab.route) }

private fun NavDestination?.isRouteSelected(route: PhoneDownRoute): Boolean =
    this?.hierarchy?.any { destination -> destination.route == route.path } == true
