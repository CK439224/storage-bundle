package com.storagebundle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.storagebundle.core.media.permission.MediaPermissions
import com.storagebundle.core.ui.theme.StorageBundleTheme
import com.storagebundle.navigation.StorageBundleNavHost
import com.storagebundle.navigation.TopLevelDestination
import com.storagebundle.permission.PermissionGate
import dagger.hilt.android.AndroidEntryPoint

/**
 * The app's single activity.
 *
 * Everything above this is Compose; the activity exists to host the navigation graph and to
 * own the permission gate.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            StorageBundleTheme {
                StorageBundleApp()
            }
        }
    }
}

/**
 * Root composable: bottom navigation plus the gated feature content.
 *
 * The media permission is requested here because all three Phase 0 destinations sit behind
 * it. As the features land, the gate moves down to the individual screens that need it, so a
 * user who only wants permission tracking is never asked for image access (PLAN.md §6).
 */
@Composable
private fun StorageBundleApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { StorageBundleBottomBar(navController) },
    ) { innerPadding ->
        PermissionGate(
            permission = MediaPermissions.requiredImagePermission,
            rationaleTitle = stringResource(R.string.permission_media_title),
            rationaleBody = stringResource(R.string.permission_media_rationale),
            modifier = Modifier.padding(innerPadding),
        ) {
            StorageBundleNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun StorageBundleBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        // Single top-level back stack: tabs switch, they do not stack up.
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.labelRes)) },
            )
        }
    }
}
