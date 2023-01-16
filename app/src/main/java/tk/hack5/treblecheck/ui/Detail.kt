/*
 *     Treble Info
 *     Copyright (C) 2019 Hackintosh Five
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

package tk.hack5.treblecheck.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.TrebleResult

data class Detail(val icon: Painter, val iconTint: Color, val title: String, val subtitle: String, val body: String)


@Composable
fun trebleDetail(treble: Optional<TrebleResult?>): Detail {
    val icon = painterResource(
        when (treble) {
            is Optional.Nothing -> R.drawable.unknown
            is Optional.Value -> when (treble.value) {
                null -> R.drawable.treble_false
                else -> R.drawable.treble_true
            }
        }
    )
    val tint = when (treble) {
        is Optional.Nothing -> Error
        is Optional.Value -> when (treble.value) {
            null -> Red
            else -> Green
        }
    }
    val subtitle = stringResource(
        when (treble) {
            is Optional.Nothing -> R.string.treble_unknown
            is Optional.Value -> when (treble.value) {
                null -> R.string.treble_false
                else -> R.string.treble_true
            }
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.treble_title),
        subtitle,
        stringResource(R.string.treble_body)
    )
}


private val Red: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Error: Color @Composable get() = Color.Red
private val Orange: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Green: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Blue: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Neutral: Color @Composable get() = Green
