/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2022 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// TODO better colors
private val DarkColorPalette = darkColorScheme(
    primary = Purple200,
    secondary = Teal200,
    tertiary = Purple700
)

private val LightColorPalette = lightColorScheme(
    primary = Purple500,
    secondary = Teal200,
    tertiary = Purple700
)

@Composable
fun TrebleCheckTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColors: Boolean = true, content: @Composable () -> Unit) {
    val supportsDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        darkTheme && dynamicColors && supportsDynamicColors -> dynamicDarkColorScheme(LocalContext.current)
        darkTheme -> DarkColorPalette
        dynamicColors && supportsDynamicColors -> dynamicLightColorScheme(LocalContext.current)
        else -> LightColorPalette
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )

}