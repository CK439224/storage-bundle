package com.storagebundle.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Brand seed colour — a muted teal, chosen to read as a utility rather than a "cleaner". */
private val BrandPrimary = Color(0xFF1F5B4E)
private val BrandSecondary = Color(0xFF4A635C)
private val BrandTertiary = Color(0xFF3F6375)

/** Reserved for destructive affordances; never used decoratively. */
private val DestructiveLight = Color(0xFFBA1A1A)
private val DestructiveDark = Color(0xFFFFB4AB)

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandTertiary,
    error = DestructiveLight,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8CD6C4),
    secondary = Color(0xFFB1CCC3),
    tertiary = Color(0xFFA7CBE0),
    error = DestructiveDark,
)

/**
 * The app's Material 3 theme.
 *
 * Dynamic colour is used where the platform offers it (API 31+) so the app inherits the
 * user's system palette; the brand scheme is the fallback.
 *
 * @param darkTheme whether to use the dark colour scheme. Defaults to the system setting.
 * @param dynamicColor whether to prefer the wallpaper-derived palette on supported devices.
 * @param content the themed content.
 */
@Composable
fun StorageBundleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && supportsDynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StorageBundleTypography,
        content = content,
    )
}
