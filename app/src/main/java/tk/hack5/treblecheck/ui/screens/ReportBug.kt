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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import tk.hack5.treblecheck.horizontal

@Composable
fun ReportBug(
    sendEmail: () -> Unit,
    innerPadding: PaddingValues
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(innerPadding.horizontal()), Arrangement.Center, Alignment.CenterHorizontally) {
        Spacer(Modifier.height(innerPadding.calculateTopPadding()))
        // TODO
        Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
    }
}