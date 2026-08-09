package com.storagebundle.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.storagebundle.R

/**
 * The app's top-level navigation targets, one per feature module.
 *
 * Ordering reflects product priority (PLAN.md §1.2): Screenshot Sweeper leads, because it is
 * the feature that gives value before asking the user to delete anything.
 *
 * @property route the navigation route string.
 * @property labelRes the tab label.
 * @property icon the tab icon.
 */
enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    /** Screenshot Sweeper — v0.1, the lead feature. */
    Screenshots(
        route = "screenshots",
        labelRes = R.string.nav_screenshots,
        icon = Icons.Filled.Search,
    ),

    /** Duplicate Photo Auditor — v0.2. */
    Duplicates(
        route = "duplicates",
        labelRes = R.string.nav_duplicates,
        icon = Icons.AutoMirrored.Filled.List,
    ),

    /** App Permission Drift Tracker — v0.3. */
    Permissions(
        route = "permissions",
        labelRes = R.string.nav_permissions,
        icon = Icons.Filled.Lock,
    ),
    ;

    /** Shared defaults for the destination set. */
    companion object {
        /** The destination shown on launch. */
        val START: TopLevelDestination = Screenshots
    }
}
