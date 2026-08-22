package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun McqGeneratorTheme(
    appThemeMode: AppThemeMode = AppThemeMode.INDIGO_SLATE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appThemeMode) {
        AppThemeMode.INDIGO_SLATE -> if (darkTheme) {
            darkColorScheme(
                primary = IndigoPrimary,
                secondary = IndigoSecondary,
                tertiary = IndigoTertiary,
                background = IndigoBackgroundDark,
                surface = IndigoSurfaceDark,
                surfaceVariant = IndigoCardDark,
                onPrimary = Color.White,
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFF1F5F9)
            )
        } else {
            lightColorScheme(
                primary = IndigoPrimary,
                secondary = IndigoSecondary,
                tertiary = IndigoTertiary,
                background = Color(0xFFF8FAFC),
                surface = Color.White,
                surfaceVariant = Color(0xFFE2E8F0)
            )
        }

        AppThemeMode.EMERALD_FOREST -> if (darkTheme) {
            darkColorScheme(
                primary = EmeraldPrimary,
                secondary = EmeraldSecondary,
                tertiary = EmeraldTertiary,
                background = EmeraldSurfaceDark,
                surface = EmeraldCardDark,
                surfaceVariant = Color(0xFF047857),
                onPrimary = Color.White,
                onBackground = Color(0xFFECFDF5),
                onSurface = Color(0xFFD1FAE5)
            )
        } else {
            lightColorScheme(
                primary = EmeraldPrimary,
                secondary = EmeraldSecondary,
                tertiary = EmeraldTertiary,
                background = Color(0xFFF0FDF4),
                surface = Color.White,
                surfaceVariant = Color(0xFFDCFCE7)
            )
        }

        AppThemeMode.SUNSET_COPPER -> if (darkTheme) {
            darkColorScheme(
                primary = SunsetPrimary,
                secondary = SunsetSecondary,
                tertiary = SunsetTertiary,
                background = SunsetSurfaceDark,
                surface = SunsetBackgroundDark,
                surfaceVariant = SunsetCardDark,
                onPrimary = Color.White,
                onBackground = Color(0xFFFAFAFA),
                onSurface = Color(0xFFF4F4F5)
            )
        } else {
            lightColorScheme(
                primary = SunsetPrimary,
                secondary = SunsetSecondary,
                tertiary = SunsetTertiary,
                background = Color(0xFFFFF7ED),
                surface = Color.White,
                surfaceVariant = Color(0xFFFFEDD5)
            )
        }

        AppThemeMode.CYBER_NEON -> if (darkTheme) {
            darkColorScheme(
                primary = CyberPrimary,
                secondary = CyberSecondary,
                tertiary = CyberTertiary,
                background = CyberBackgroundDark,
                surface = CyberSurfaceDark,
                surfaceVariant = CyberCardDark,
                onPrimary = Color.Black,
                onBackground = Color(0xFFFAFAFA),
                onSurface = Color(0xFFF4F4F5)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF0284C7),
                secondary = Color(0xFFE11D48),
                tertiary = Color(0xFF9333EA),
                background = Color(0xFFFAFAFA),
                surface = Color.White,
                surfaceVariant = Color(0xFFF4F4F5)
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun DevStudioTheme(
    appThemeMode: AppThemeMode = AppThemeMode.INDIGO_SLATE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    McqGeneratorTheme(
        appThemeMode = appThemeMode,
        darkTheme = darkTheme,
        content = content
    )
}
