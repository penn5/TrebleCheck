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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.BinderArch
import tk.hack5.treblecheck.data.CPUArch
import tk.hack5.treblecheck.data.TrebleResult
import tk.hack5.treblecheck.getOrNull
import tk.hack5.treblecheck.horizontal
import tk.hack5.treblecheck.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsList(
    innerPadding: PaddingValues,
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
            AlertDialog(onDismissRequest = { openDialog = null }) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        Modifier.padding(dialogPadding)
                    ) {
                        Text(detail.title, style = MaterialTheme.typography.titleLarge)
                        Text(detail.subtitle, style = MaterialTheme.typography.titleMedium)
                        Text(detail.body, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(verticalSpacer))
                        TextButton(
                            onClick = { openDialog = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.close_dialog))
                        }
                    }
                }
            }
        }
    }

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

            details.forEach {
                DetailEntry(it, onClick)
            }

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
                openDialog?.let { index ->
                    val detail = details[index]
                    Text(detail.title, style = MaterialTheme.typography.titleLarge)
                    Text(detail.subtitle, style = MaterialTheme.typography.titleMedium)
                    Text(detail.body, style = MaterialTheme.typography.bodyMedium)
                } ?: run {
                    Text(stringResource(R.string.detail_placeholder))
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

