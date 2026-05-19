package com.streamatico.polymarketviewer.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4EA8FF),
    onPrimary = Color(0xFF001B33),
    primaryContainer = Color(0xFF0C2A43),
    onPrimaryContainer = Color(0xFFD7EBFF),
    secondary = Color(0xFF8ED4FF),
    onSecondary = Color(0xFF002033),
    tertiary = Color(0xFFFFB86B),
    background = Color.Black,
    onBackground = Color(0xFFECEFF1),
    surface = Color(0xFF0A0A0A),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF151515),
    onSurfaceVariant = Color(0xFFB8C0C4),
    outline = Color(0xFF30363A),
    error = Color(0xFFFF6B6B)
)

private val LightColorScheme = lightColorScheme(
    primary = Moss,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8EADF),
    onPrimaryContainer = Color(0xFF082F21),
    secondary = Brass,
    tertiary = Signal,
    background = Paper,
    onBackground = Ink,
    surface = Porcelain,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Graphite,
    outline = Zinc,
    error = Signal
)

// Define the structure for custom colors
@Immutable
data class ExtendedColors(
    val trendUpContainer: Color,
    val trendDownContainer: Color,

    val onTrendUpContainer: Color,
    val onTrendDownContainer: Color
)

// Define instances for light and dark themes
private val lightExtendedColors = ExtendedColors(
    trendUpContainer = TrendUpContainerLight,
    onTrendUpContainer = OnTrendUpContainerAuto,

    trendDownContainer = TrendDownContainerLight,
    onTrendDownContainer = OnTrendDownContainerAuto,
)

private val darkExtendedColors = ExtendedColors(
    trendUpContainer = TrendUpContainerDark,
    onTrendUpContainer = OnTrendUpContainerAuto,

    trendDownContainer = TrendDownContainerDark,
    onTrendDownContainer = OnTrendDownContainerAuto,
)

// CompositionLocal to provide extended colors
private val LocalExtendedColors = staticCompositionLocalOf { lightExtendedColors }


@Composable
fun PolymarketAppTheme(
    darkTheme: Boolean = true,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val baseColorScheme = getPolymarketColorScheme(
        context = LocalContext.current,
        isDarkTheme = darkTheme,
        dynamicColor = dynamicColor
    )

    // Determine which set of extended colors to use
    val extendedColors = if (darkTheme) darkExtendedColors else lightExtendedColors

    // Provide both MaterialTheme colors and ExtendedColors
    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = baseColorScheme,
            typography = Typography,
            content = content
        )
    }
}

internal fun getPolymarketColorScheme(context: Context, isDarkTheme: Boolean, dynamicColor: Boolean = true): ColorScheme {
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
}

// Custom theme object to access extended colors easily
object ExtendedTheme {
    val colors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}

// Usage example (in another Composable):
// Text(
//     text = "Trending Up",
//     color = ExtendedTheme.colors.trendUp
// )
// Text(
//     text = "Trending Down",
//     color = ExtendedTheme.colors.trendDown
// )
