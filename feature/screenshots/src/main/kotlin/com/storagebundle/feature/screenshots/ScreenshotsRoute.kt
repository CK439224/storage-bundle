package com.storagebundle.feature.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.storagebundle.core.ui.component.PlaceholderScreen

/**
 * Entry point for the Screenshots feature.
 *
 * v0.1 lead feature (PLAN.md 5.1). Phase 0 delivers the module, its dependency wiring, and this route so the
 * navigation graph is exercised end to end; the screen itself is built in that release.
 *
 * @param modifier applied to the root layout.
 */
@Composable
fun ScreenshotsRoute(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Screenshots",
        plannedFor = "Arriving in v0.1 - search your screenshots by their text, then sweep what you no longer need.",
        modifier = modifier,
    )
}
