package com.storagebundle.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.storagebundle.core.ui.theme.Dimens
import com.storagebundle.core.ui.theme.StorageBundleTheme

/**
 * A stand-in for a screen that has not been built yet.
 *
 * Phase 0 delivers the skeleton and its wiring, not the features (PLAN.md §10). Each feature
 * module renders this so the navigation graph, theme, and DI graph are all exercised
 * end-to-end and the app is genuinely runnable.
 *
 * @param title the feature's name.
 * @param plannedFor the release this screen is scheduled for, shown so the placeholder is
 *   self-explanatory rather than looking like a bug.
 * @param modifier applied to the root layout.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    plannedFor: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = plannedFor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    StorageBundleTheme {
        PlaceholderScreen(
            title = "Screenshots",
            plannedFor = "Arriving in v0.1 — search and sweep",
        )
    }
}
