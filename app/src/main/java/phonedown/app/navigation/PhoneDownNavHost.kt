@file:Suppress("LongParameterList")

package phonedown.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import phonedown.app.account.AccountRoute
import phonedown.app.focus.FocusRoute
import phonedown.app.insights.InsightsRoute
import phonedown.app.onboarding.OnboardingRoute
import phonedown.app.pro.ProRoute
import phonedown.app.settings.SettingsRoute
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode

@Composable
@Suppress("FunctionName")
fun PhoneDownApp(
    themeMode: ThemeMode = ThemeMode.System,
    initialRoute: PhoneDownRoute = PhoneDownRoute.Onboarding,
    onThemeModeSelected: suspend (ThemeMode) -> Unit = {},
    onStartFocusClick: (Long) -> Unit = {},
    onRetrySensorsClick: (Long) -> Unit = {},
    openFocusRequests: Flow<Unit> = emptyFlow(),
    callPausePermissionGranted: Boolean = false,
    onCallPausePermissionRequested: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination.showsTabs()
    val coroutineScope = rememberCoroutineScope()

    PhoneDownTheme(themeMode = themeMode) {
        LaunchedEffect(navController, openFocusRequests) {
            openFocusRequests.collect {
                navController.navigate(PhoneDownRoute.Focus.path) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

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
                onThemeModeSelected = { mode ->
                    coroutineScope.launch {
                        onThemeModeSelected(mode)
                    }
                },
                onStartFocusClick = onStartFocusClick,
                onRetrySensorsClick = onRetrySensorsClick,
                callPausePermissionGranted = callPausePermissionGranted,
                onCallPausePermissionRequested = onCallPausePermissionRequested,
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
    onThemeModeSelected: (ThemeMode) -> Unit,
    onStartFocusClick: (Long) -> Unit,
    onRetrySensorsClick: (Long) -> Unit,
    callPausePermissionGranted: Boolean,
    onCallPausePermissionRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = initialRoute.path,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NAV_TRANSITION_MILLIS),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NAV_TRANSITION_MILLIS),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NAV_TRANSITION_MILLIS),
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NAV_TRANSITION_MILLIS),
            )
        },
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
            InsightsRoute()
        }
        composable(PhoneDownRoute.Settings.path) {
            SettingsRoute(
                onAccountClick = { navController.navigate(PhoneDownRoute.Account.path) },
                onProClick = { navController.navigate(PhoneDownRoute.Pro.path) },
                onPrivacyPolicyClick = { navController.navigate(PhoneDownRoute.PrivacyPolicy.path) },
                onThemeModeSelected = onThemeModeSelected,
                callPausePermissionGranted = callPausePermissionGranted,
                onCallPausePermissionRequested = onCallPausePermissionRequested,
            )
        }
        composable(PhoneDownRoute.Account.path) {
            AccountRoute(onBack = navController::popBackStack)
        }
        composable(PhoneDownRoute.Pro.path) {
            ProRoute(onBack = navController::popBackStack)
        }
        composable(PhoneDownRoute.PrivacyPolicy.path) {
            phonedown.feature.settings.PrivacyPolicyScreen(onBack = navController::popBackStack)
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
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
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

private const val NAV_TRANSITION_MILLIS = 280
