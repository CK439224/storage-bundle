package com.storagebundle.core.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing and sizing tokens.
 *
 * Layout values come from here rather than being written inline, so spacing stays consistent
 * across eleven modules and can be audited in one place.
 */
object Dimens {
    /** Tight spacing, used between closely related elements. */
    val SpaceXs: Dp = 4.dp

    /** Default spacing between elements within a group. */
    val SpaceSm: Dp = 8.dp

    /** Standard screen padding. */
    val SpaceMd: Dp = 16.dp

    /** Spacing between distinct sections. */
    val SpaceLg: Dp = 24.dp

    /** Spacing around major page divisions. */
    val SpaceXl: Dp = 32.dp

    /**
     * Minimum interactive size.
     *
     * 48dp is the accessibility floor checked in QA (PLAN.md §8); no tappable element may be
     * smaller, including the dense selection grids.
     */
    val MinTouchTarget: Dp = 48.dp

    /** Corner radius for cards and result tiles. */
    val CornerRadius: Dp = 12.dp
}
