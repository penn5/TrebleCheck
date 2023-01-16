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

package tk.hack5.treblecheck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.data.Arch
import tk.hack5.treblecheck.data.TrebleResult
import tk.hack5.treblecheck.data.VABResult
import tk.hack5.treblecheck.horizontal
import tk.hack5.treblecheck.ui.Detail
import tk.hack5.treblecheck.ui.cardIconSpacerWidth
import tk.hack5.treblecheck.ui.trebleDetail

@Composable
fun Details(
    innerPadding: PaddingValues,
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    vab: Optional<VABResult?>,
    sar: Boolean?,
    arch: Arch
) {
    Column(
        Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
            .padding(innerPadding.horizontal())
    ) {
        Spacer(Modifier.height(innerPadding.calculateTopPadding()))
        // treble
        DetailEntry(trebleDetail(treble))

        Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
    }
}

@Composable
fun DetailEntry(detail: Detail) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(detail.icon, null, Modifier.size(36.dp), detail.iconTint)
        Spacer(Modifier.width(cardIconSpacerWidth)) // TODO rename
        Column {
            Text(detail.title, maxLines = 1, style = MaterialTheme.typography.titleMedium)
            Text(detail.subtitle, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

