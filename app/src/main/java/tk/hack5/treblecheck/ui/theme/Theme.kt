/*
 *     Treble Info
 *     Copyright (C) 2023 Hackintosh Five
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
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