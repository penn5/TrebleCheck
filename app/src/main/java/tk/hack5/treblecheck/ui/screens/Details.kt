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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.BinderArch
import tk.hack5.treblecheck.data.CPUArch
import tk.hack5.treblecheck.data.TrebleResult
import tk.hack5.treblecheck.getOrNull
import tk.hack5.treblecheck.ui.*
import tk.hack5.treblecheck.vertical

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsList(
    innerPadding: PaddingValues,
    scrollConnection: NestedScrollConnection,
    twoColumn: Boolean,
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    sar: Boolean?,
    binderArch: BinderArch,
    cpuArch: CPUArch,
) {
    val details = buildList {
        add(trebleDetail(treble))
        treble.getOrNull()?.let {
            add(trebleVersionEntry(it))
            add(trebleLiteEntry(it))
            add(trebleLegacyEntry(it))
        }

        // sar
        add(sarEntry(sar))

        // a/b
        add(abEntry(ab))

        // dynamic partitions
        add(dynamicPartitionsEntry(dynamic))

        // arch
        add(cpuArchEntry(cpuArch))
        add(binderArchEntry(binderArch))
    }
    var openDialog by rememberSaveable(details) { mutableStateOf<Int?>(null) }
    val onClick: (Detail) -> Unit = { openDialog = details.indexOf(it) }

    if (!twoColumn) {
        openDialog?.let { index ->
            val detail = details[index]
            AlertDialog(
                onDismissRequest = { openDialog = null },
                confirmButton = {
                    TextButton(
                        onClick = { openDialog = null }
                    ) {
                        Text(stringResource(R.string.close_dialog))
                    }
                },
                title = { Text(detail.title) },
                text = {
                    Column {
                        Text(detail.subtitle, style = MaterialTheme.typography.titleMedium)
                        Text(detail.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }
    }

    Row(
        Modifier
            .fillMaxSize(),
            //.padding(innerPadding.horizontal())
            //.consumeWindowInsets(innerPadding.horizontal())
            //.padding(horizontal = pageHorizontalPadding),
        //if (twoColumn) Arrangement.Center else Arrangement.Start
    ) {
        Column(
            Modifier
                .nestedScroll(scrollConnection)
                .verticalScroll(rememberScrollState())
                .weight(0.35f)
                .consumeWindowInsets(innerPadding.vertical()),
            horizontalAlignment = if (twoColumn) Alignment.End else Alignment.Start
        ) {
            Spacer(Modifier.height(innerPadding.calculateTopPadding()))

            val modifier = Modifier.safeDrawingPadding().padding(start = pageHorizontalPadding, end = gutter / 2).fillMaxWidth()
            details.forEach {
                DetailEntry(it, modifier, onClick)
            }
            Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
        }
        if (twoColumn) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(0.65f)
                    .consumeWindowInsets(innerPadding.vertical())
            ) {
                Spacer(Modifier.height(innerPadding.calculateTopPadding()))
                val modifier = Modifier.safeDrawingPadding().padding(start = gutter / 2, end = pageHorizontalPadding).fillMaxWidth()
                openDialog?.let { index ->
                    val detail = details[index]
                    Text(detail.title, modifier, style = MaterialTheme.typography.titleLarge)
                    Text(detail.subtitle, modifier, style = MaterialTheme.typography.titleMedium)
                    Text(detail.body, modifier, style = MaterialTheme.typography.bodyMedium)
                } ?: run {
                    Text(stringResource(R.string.detail_placeholder), modifier)
                }
                Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
            }
        }
    }
}

@Composable
fun DetailEntry(detail: Detail, modifier: Modifier, onClick: (Detail) -> Unit) {
    Row(
        modifier
            .clickable { onClick(detail) }
            .fillMaxWidth()
            .padding(vertical = listVerticalPadding),
        verticalAlignment = Alignment.Top
    ) {
        Icon(detail.icon, null, Modifier.size(36.dp), MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(cardIconSpacerWidth))
        Column {
            Text(detail.title, maxLines = 1, style = MaterialTheme.typography.titleMedium)
            Text(detail.subtitle, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

