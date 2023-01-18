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

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.*
import com.mikepenz.aboutlibraries.ui.compose.Libraries
import com.mikepenz.aboutlibraries.util.withContext
import tk.hack5.treblecheck.*
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.ui.listVerticalPadding
import tk.hack5.treblecheck.ui.pageHorizontalPadding
import tk.hack5.treblecheck.ui.theme.libraryColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Licenses(
    innerPadding: PaddingValues,
    scrollConnection: NestedScrollConnection,
) {
    val libraries = remember { mutableStateOf<Libs?>(null) }

    val context = LocalContext.current
    LaunchedEffect(libraries) {
        libraries.value = Libs.Builder().withContext(context).build()
    }

    val thisLibrary = Library(
        "tk.hack5:treblecheck",
        BuildConfig.VERSION_NAME,
        stringResource(R.string.title),
        stringResource(R.string.this_app),
        "https://hack5.dev/about/projects/TrebleInfo",
        listOf(Developer("hackintosh5", "https://hack5.dev/about")),
        null,
        Scm(
            "https://hack5.dev/about/projects/TrebleInfo",
            "scm:git:ssh://git@gitlab.com/hackintosh5/TrebleInfo.git",
            "scm:git:https://gitlab.com/hackintosh5/TrebleInfo.git"
        ),
        setOf(
            License(
                SpdxLicense.GPL_3_0_only.fullName,
                SpdxLicense.GPL_3_0_only.getUrl(),
                null,
                SpdxLicense.GPL_3_0_only.id,
                context.resources.openRawResource(R.raw.license).bufferedReader().readText(),
                SpdxLicense.GPL_3_0_only.id
            )
        ),
        setOf(),
        null
    )
    val libs = libraries.value?.libraries ?: emptyList()
    Box(Modifier.consumeWindowInsets(innerPadding)) {
        val insets = WindowInsets.safeDrawing
        Libraries(
            libraries = listOf(thisLibrary) + libs,
            contentPadding = innerPadding,
            colors = libraryColors,
            itemContentPadding = PaddingValues(
                horizontal = pageHorizontalPadding,
                vertical = listVerticalPadding
            ) + insets.asPaddingValues().horizontal(),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollConnection)
                .consumeWindowInsets(WindowInsets.safeDrawing)
        )
    }
}