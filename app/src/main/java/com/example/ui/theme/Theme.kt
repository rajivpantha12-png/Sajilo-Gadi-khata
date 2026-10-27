package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FleetPrimaryDark,
    onPrimary = FleetOnPrimaryDark,
    primaryContainer = FleetPrimaryContainerDark,
    onPrimaryContainer = FleetOnPrimaryContainerDark,
    secondary = FleetSecondaryDark,
    onSecondary = FleetOnSecondaryDark,
    secondaryContainer = FleetSecondaryContainerDark,
    onSecondaryContainer = FleetOnSecondaryContainerDark,
    tertiary = FleetTertiaryDark,
    onTertiary = FleetOnTertiaryDark,
    tertiaryContainer = FleetTertiaryContainerDark,
    onTertiaryContainer = FleetOnTertiaryContainerDark,
    background = FleetBackgroundDark,
    onBackground = FleetOnBackgroundDark,
    surface = FleetSurfaceDark,
    onSurface = FleetOnSurfaceDark,
    surfaceVariant = FleetSurfaceVariantDark,
    onSurfaceVariant = FleetOnSurfaceVariantDark,
)

private val LightColorScheme = lightColorScheme(
    primary = FleetPrimary,
    onPrimary = FleetOnPrimary,
    primaryContainer = FleetPrimaryContainer,
    onPrimaryContainer = FleetOnPrimaryContainer,
    secondary = FleetSecondary,
    onSecondary = FleetOnSecondary,
    secondaryContainer = FleetSecondaryContainer,
    onSecondaryContainer = FleetOnSecondaryContainer,
    tertiary = FleetTertiary,
    onTertiary = FleetOnTertiary,
    tertiaryContainer = FleetTertiaryContainer,
    onTertiaryContainer = FleetOnTertiaryContainer,
    background = FleetBackground,
    onBackground = FleetOnBackground,
    surface = FleetSurface,
    onSurface = FleetOnSurface,
    surfaceVariant = FleetSurfaceVariant,
    onSurfaceVariant = FleetOnSurfaceVariant,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded financial colors consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
