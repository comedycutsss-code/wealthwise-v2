package com.wealthwise.app.presentation.common.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wealthwise.app.presentation.dashboard.DashboardScreen
import com.wealthwise.app.presentation.settings.SettingsScreen

sealed class Destination(val route: String) {
    data object Dashboard : Destination("dashboard")
    data object TransactionDetail : Destination("transaction/{id}") {
        fun build(id: Long) = "transaction/$id"
    }
    data object Analytics : Destination("analytics")
    data object Search : Destination("search")
    data object Settings : Destination("settings")
}

/**
 * "Shared axis" transition (borrowed from Material's motion system): forward navigation
 * slides the incoming screen in from the trailing edge while fading, and slides the outgoing
 * screen out toward the leading edge while fading — rather than a flat cross-fade or the
 * default abrupt push. Distance is kept short (24% of width) so it reads as a deliberate
 * axis shift, not a full-screen slide.
 */
private const val MOTION_DURATION = 380

private fun sharedAxisEnter() = slideInHorizontally(
    animationSpec = tween(MOTION_DURATION), initialOffsetX = { (it * 0.24f).toInt() }
) + fadeIn(animationSpec = tween(MOTION_DURATION))

private fun sharedAxisExit() = slideOutHorizontally(
    animationSpec = tween(MOTION_DURATION), targetOffsetX = { -(it * 0.24f).toInt() }
) + fadeOut(animationSpec = tween(MOTION_DURATION))

private fun sharedAxisPopEnter() = slideInHorizontally(
    animationSpec = tween(MOTION_DURATION), initialOffsetX = { -(it * 0.24f).toInt() }
) + fadeIn(animationSpec = tween(MOTION_DURATION))

private fun sharedAxisPopExit() = slideOutHorizontally(
    animationSpec = tween(MOTION_DURATION), targetOffsetX = { (it * 0.24f).toInt() }
) + fadeOut(animationSpec = tween(MOTION_DURATION))

@Composable
fun WealthWiseNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Destination.Dashboard.route,
        enterTransition = { sharedAxisEnter() },
        exitTransition = { sharedAxisExit() },
        popEnterTransition = { sharedAxisPopEnter() },
        popExitTransition = { sharedAxisPopExit() }
    ) {
        composable(Destination.Dashboard.route) {
            DashboardScreen(
                onOpenTransaction = { id -> navController.navigate(Destination.TransactionDetail.build(id)) },
                onOpenSettings = { navController.navigate(Destination.Settings.route) }
            )
        }
        composable(Destination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        // Additional destinations (TransactionDetail, Analytics, Search) plug in here
        // following the same composable(...) { } pattern — omitted in this scaffold to keep
        // the reviewable surface focused; each inherits the shared-axis motion above
        // automatically since it's set at the NavHost level.
    }
}
