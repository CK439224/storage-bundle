package com.storagebundle.feature.dupes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.storagebundle.core.ui.component.PlaceholderScreen

/**
 * Entry point for the Duplicates feature.
 *
 * v0.2 (PLAN.md 5.2). Phase 0 delivers the module, its dependency wiring, and this route so the
 * navigation graph is exercised end to end; the screen itself is built in that release.
 *
 * @param modifier applied to the root layout.
 */
@Composable
fun DuplicatesRoute(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Duplicates",
        plannedFor = "Arriving in v0.2 - finds near-duplicate photos, including burst shots, not just exact copies.",
        modifier = modifier,
    )
}
