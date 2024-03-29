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
// SPDX-License-Identifier: GPL-3.0-or-later

package tk.hack5.treblecheck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.horizontal
import tk.hack5.treblecheck.ui.pageHorizontalPadding

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Contribute(
    innerPadding: PaddingValues,
    scrollConnection: NestedScrollConnection,
    askAQuestion: () -> Unit,
    reportBug: () -> Unit,
    helpTranslate: () -> Unit,
    contributeCode: () -> Unit,
    donate: () -> Unit,
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .nestedScroll(scrollConnection)
            .fillMaxSize()
            .padding(innerPadding.horizontal())
            .consumeWindowInsets(innerPadding)
            .padding(horizontal = pageHorizontalPadding),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(innerPadding.calculateTopPadding()))
        Button(askAQuestion) { Text(stringResource(R.string.ask_a_question)) }
        OutlinedButton(reportBug) { Text(stringResource(R.string.report_a_bug)) }
        OutlinedButton(helpTranslate) { Text(stringResource(R.string.help_translate)) }
        OutlinedButton(contributeCode) { Text(stringResource(R.string.contribute_code)) }
        OutlinedButton(donate) { Text(stringResource(R.string.donate)) }
        Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
    }
}