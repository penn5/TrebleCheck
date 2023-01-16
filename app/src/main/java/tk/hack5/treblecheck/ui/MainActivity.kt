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

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.*
import tk.hack5.treblecheck.getOrNull
import tk.hack5.treblecheck.supported
import tk.hack5.treblecheck.ui.screens.*
import tk.hack5.treblecheck.ui.theme.TrebleCheckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val treble = remember {
                try {
                    Optional.Value(TrebleDetector.getVndkData())
                } catch (e: Exception) {
                    Log.e(tag, "Failed to get VNDK data", e)
                    Optional.Nothing
                }
            }
            val ab = remember {
                try {
                    ABDetector.checkAB()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to get AB status", e)
                    null
                }
            }
            val dynamic = remember {
                try {
                    DynamicPartitionsDetector.isDynamic()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to get Dynamic Partitions status", e)
                    null
                }
            }
            val vab = remember {
                try {
                    Optional.Value(VABDetector.getVABData())
                } catch (e: Exception) {
                    Log.e(tag, "Failed to get VAB status", e)
                    Optional.Nothing
                }
            }
            val sar = remember {
                try {
                    MountDetector.isSAR()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to get SAR status", e)
                    null
                }
            }
            val arch = remember {
                try {
                    ArchDetector.getArch()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to get arch", e)
                    Arch.UNKNOWN(null, null)
                }
            }
            val fileName = remember {
                try {
                    treble.getOrNull()
                        ?.let { FileNameAnalyzer.getFileName(it, arch, sar) }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to generate filename", e)
                    null
                }
            }

            MainActivityContent(
                treble,
                ab,
                dynamic,
                vab,
                sar,
                arch,
                fileName
            ) { TODO() }
        }
    }
}

sealed class Screen(val route: String)

sealed class RootScreen(route: String, @StringRes val title: Int, @DrawableRes val icon: Int) : Screen(route)

object Screens {
    object Images : RootScreen("images", R.string.screen_images, R.drawable.screen_images)
    // TODO change icons
    object Details : RootScreen("details", R.string.screen_details, R.drawable.screen_details)
    object Licenses : RootScreen("licenses", R.string.screen_licenses, R.drawable.screen_licenses)
    object Contribute : RootScreen("contribute", R.string.screen_contribute, R.drawable.screen_contribute)
    object ReportBug : Screen("contribute/bug")
}

val screens = listOf(Screens.Images, Screens.Details, Screens.Licenses, Screens.Contribute)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    vab: Optional<VABResult?>,
    sar: Boolean?,
    arch: Arch,
    fileName: String?,
    browseImages: () -> Unit
) {
    val navController = rememberNavController()

    TrebleCheckTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.title)) },
                )
            },
            bottomBar = {
                NavigationBar(Modifier.fillMaxWidth()) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    screens.forEach { screen ->
                        val selected = remember(navBackStackEntry) { navController.backQueue.lastOrNull { entry -> screens.any { it.route == entry.destination.route } }?.destination?.route == screen.route }
                        NavigationBarItem(
                            selected = selected,
                            icon = { Icon(painterResource(screen.icon), null) },
                            label = { Text(stringResource(screen.title)) },
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = !selected
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController = navController, startDestination = "images") {
                composable(Screens.Images.route) { Images(
                    browseImages,
                    {
                        navController.navigate(Screens.Details.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }, {
                        navController.navigate(Screens.ReportBug.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    innerPadding,
                    treble.supported,
                    fileName
                ) }
                composable(Screens.Details.route) { Details(innerPadding, treble, ab, dynamic, vab, sar, arch) }
                composable(Screens.Licenses.route) { Licenses(innerPadding) }
                composable(Screens.Contribute.route) { Contribute(
                    {
                        navController.navigate(Screens.ReportBug.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    innerPadding
                ) }
                composable(Screens.ReportBug.route) {
                    ReportBug({ TODO() }, innerPadding)
                }
            }
            /*Column(
                Modifier
                    .padding(innerPadding) *//* TODO maybe apply on the inside? *//*,
                verticalArrangement = Arrangement.spacedBy(cardOuterVerticalSeparation)
            ) {
                plain { requiredImageEntry(fileName).clickable(onRequiredImageClick) }
                expandable {
                    plain { moreInfoEntry().clickable() }
                    expandable {
                        plain { trebleEntry().clickable() }
                        plain { trebleEntry(treble).clickable() }
                        treble.getOrNull()?.let {
                            plain { trebleVersionEntry(it).clickable() }
                            plain { trebleLiteEntry(it).clickable() }
                            plain { trebleLegacyEntry(it).clickable() }
                        }
                    }
                    expandable {
                        plain { sarEntry().clickable() }
                        plain { sarEntry(sar).clickable() }
                    }
                    expandable {
                        plain { partitionsEntry().clickable() }
                        plain { abEntry(ab).clickable() }
                        plain { dynamicPartitionsEntry(dynamic).clickable() }
                        expandable {
                            plain { vabEntry().clickable() }
                            plain { vabEntry(vab).clickable() }
                            vab.getOrNull()?.let {
                                plain { vabcEntry(it).clickable() }
                                plain { vabrEntry(it).clickable() }
                            }
                        }
                    }
                    expandable {
                        plain { archEntry().clickable() }
                        plain { archEntry(arch).clickable() }
                    }
                }
                expandable {
                    plain { licenseEntry().clickable() }
                }
                expandable {
                    plain { contributeEntry().clickable() }
                    // plain { translateEntry() }
                    // plain { donateEntry() }
                }
            }*/


        }
    }
}


@Preview
@Composable
fun MainActivityPreview() {
    TrebleCheckTheme(darkTheme = false) {
        MainActivityContent(
            Optional.Value(
                TrebleResult(false, true, 30, 0)
            ),
            true,
            true,
            Optional.Value(
                VABResult(true, true)
            ),
            true,
            Arch.ARM32_BINDER64,
            "system-arm64-ab.img.xz"
        ) { TODO() }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DisplayedEntry(entry: Entry, isTitle: Transition<Boolean>, specs: TransitionSpecs, onClick: () -> Unit) {
    val padding by isTitle.animateDp(label = "Padding") { if (it) 0.dp else cardOuterHorizontalPadding }
    val modifier = Modifier.padding(horizontal = padding)

    when (entry) {
        is BasicEntry -> {
            val cardContent = @Composable {
                TextCardContent(entry.header, entry.body, entry.detail, entry.icon, entry.tint, isTitle, specs)
            }

            isTitle.Crossfade(animationSpec = specs.crossfade) {
                if (it) {
                    OutlinedCard(
                        modifier = modifier
                            .fillMaxWidth(),
                        onClick = onClick,
                        border = BorderStroke(1.dp, Color.Transparent)
                    ) {
                        cardContent()
                    }
                } else {
                    OutlinedCard(
                        modifier = modifier.fillMaxWidth(),
                        onClick = onClick,
                        border = BorderStroke(1.dp, Color.Black /* TODO */)
                    ) {
                        cardContent()
                    }
                }
            }
        }
    }
}

sealed class Entry

data class BasicEntry(
    val icon: Painter,
    val tint: Color,
    val body: String,
    val header: String,
    val detail: String?,
) : Entry()

data class ClickableEntry(
    val entry: Entry,
    val onClick: (() -> Unit)?,
)

fun Entry.clickable(onClick: (() -> Unit)? = null) = ClickableEntry(this, onClick)


private const val tag = "MainActivity"