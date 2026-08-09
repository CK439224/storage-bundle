package com.storagebundle.feature.permissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.storagebundle.core.ui.component.PlaceholderScreen

/**
 * Entry point for the Permissions feature.
 *
 * v0.3 (PLAN.md 5.3). Phase 0 delivers the module, its dependency wiring, and this route so the
 * navigation graph is exercised end to end; the screen itself is built in that release.
 *
 * @param modifier applied to the root layout.
 */
@Composable
fun PermissionDriftRoute(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Permissions",
        plannedFor = "Arriving in v0.3 - a timeline of permissions your installed apps gained after an update.",
        modifier = modifier,
    )
}
