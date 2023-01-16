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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.horizontal

@Composable
fun Images(
    browseImages: () -> Unit,
    navigateToDetails: () -> Unit,
    reportBug: () -> Unit,
    innerPadding: PaddingValues,
    treble: Boolean?,
    fileName: String?,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(innerPadding.horizontal()), Arrangement.Center, Alignment.CenterHorizontally) {
        Spacer(Modifier.height(innerPadding.calculateTopPadding()))
        val icon: Painter
        val title: String
        val body: String
        if (treble == null || fileName == null) {
            icon = painterResource(R.drawable.bug)
            title = stringResource(R.string.detection_error_title)
            body = stringResource(R.string.detection_error_body)
        } else if (treble == false) {
            icon = painterResource(R.drawable.no_treble)
            title = stringResource(R.string.no_treble_title)
            body = stringResource(R.string.no_treble_body)
        } else {
            icon = painterResource(R.drawable.images_found)
            title = stringResource(R.string.images_found_title)
            body = stringResource(R.string.images_found_body)
        }
        Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(body, textAlign = TextAlign.Center)

        if (treble == null || fileName == null) {
            Button(reportBug) { Text(stringResource(R.string.report_this_bug)) }
        } else {
            Text(fileName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Button(browseImages) { Text(stringResource(R.string.browse_images)) }
        }
        OutlinedButton(navigateToDetails) { Text(stringResource(R.string.view_details)) }
        Spacer(Modifier.height(innerPadding.calculateBottomPadding()))
    }
}