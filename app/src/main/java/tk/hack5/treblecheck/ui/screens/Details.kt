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

package tk.hack5.treblecheck.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.data.BinderArch
import tk.hack5.treblecheck.data.CPUArch
import tk.hack5.treblecheck.data.TrebleResult
import tk.hack5.treblecheck.data.VABResult
import tk.hack5.treblecheck.getOrNull
import tk.hack5.treblecheck.horizontal
import tk.hack5.treblecheck.ui.*

@Composable
fun DetailsList(
    innerPadding: PaddingValues,
    twoColumn: Boolean,
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    vab: Optional<VABResult?>,
    sar: Boolean?,
    binderArch: BinderArch,
    cpuArch: CPUArch,
) {
    // TODO limit column width
    var openDialog by remember { mutableStateOf<Detail?>(null) }
    val onClick: (Detail) -> Unit = { openDialog = it }

    Row(
        Modifier
            .fillMaxSize()
            .padding(innerPadding.horizontal()),
        if (twoColumn) Arrangement.Center else Arrangement.Start
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(if (twoColumn) 0.35f else 1f)
        ) {
            Spacer(Modifier.height(innerPadding.calculateTopPadding()))
            // treble
            DetailEntry(trebleDetail(treble), onClick)
            treble.getOrNull()?.let {
                DetailEntry(trebleVersionEntry(it), onClick)
                DetailEntry(trebleLiteEntry(it), onClick)
                DetailEntry(trebleLegacyEntry(it), onClick)
            }

            // sar
            DetailEntry(sarEntry(sar), onClick)

            // a/b
            DetailEntry(abEntry(ab), onClick)

            // dynamic partitions
            DetailEntry(dynamicPartitionsEntry(dynamic), onClick)

            // virtual a/b
            DetailEntry(vabEntry(vab), onClick)
            vab.getOrNull()?.let {
                DetailEntry(vabcEntry(it), onClick)
                DetailEntry(vabrEntry(it), onClick)
            }

            // arch
            DetailEntry(cpuArchEntry(cpuArch), onClick)
            DetailEntry(binderArchEntry(binderArch), onClick)

            Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
        }
        if (twoColumn) {
            Spacer(Modifier.width(gutter))
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(0.65f)
                    .padding(horizontal = pageHorizontalPadding)
            ) {
                Spacer(Modifier.height(innerPadding.calculateTopPadding()))
                openDialog?.let {
                    Text(it.title, style = MaterialTheme.typography.titleLarge)
                    Text(it.subtitle, style = MaterialTheme.typography.titleMedium)
                    Text(it.body, style = MaterialTheme.typography.bodyMedium)
                } ?: run {
                    // TODO placeholder
                }
                Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
            }
        }
    }
}

@Composable
fun DetailEntry(detail: Detail, onClick: (Detail) -> Unit) {
    Row(
        Modifier
            .clickable { onClick(detail) }
            .fillMaxWidth()
            .padding(horizontal = pageHorizontalPadding, vertical = listVerticalPadding),
        verticalAlignment = Alignment.Top
    ) {
        Icon(detail.icon, null, Modifier.size(36.dp), detail.iconTint)
        Spacer(Modifier.width(cardIconSpacerWidth)) // TODO rename
        Column {
            Text(detail.title, maxLines = 1, style = MaterialTheme.typography.titleMedium)
            Text(detail.subtitle, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

