package com.storagebundle.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.storagebundle.feature.dupes.DuplicatesRoute
import com.storagebundle.feature.permissions.PermissionDriftRoute
import com.storagebundle.feature.screenshots.ScreenshotsRoute

/**
 * The app's navigation graph.
 *
 * Each feature module contributes exactly one route, so a new feature is added here and
 * nowhere else (PLAN.md §4).
 *
 * @param navController controls navigation between destinations.
 * @param modifier applied to the host.
 */
@Composable
fun StorageBundleNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.START.route,
        modifier = modifier,
    ) {
        composable(TopLevelDestination.Screenshots.route) {
            ScreenshotsRoute()
        }
        composable(TopLevelDestination.Duplicates.route) {
            DuplicatesRoute()
        }
        composable(TopLevelDestination.Permissions.route) {
            PermissionDriftRoute()
        }
    }
}
