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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
            val binderArch = remember {
                try {
                    BinderDetector.getBinderArch()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to get binder arch", e)
                    BinderArch.Unknown(null)
                }
            }
            val cpuArch = remember {
                try {
                    ArchDetector.getCPUArch()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to get CPU arch", e)
                    CPUArch.Unknown(null)
                }
            }
            val fileName = remember {
                try {
                    treble.getOrNull()
                        ?.let { FileNameAnalyzer.getFileName(it, binderArch, cpuArch, sar) }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to generate filename", e)
                    null
                }
            }

            MainActivityContent(
                calculateWindowSizeClass(this),
                treble,
                ab,
                dynamic,
                vab,
                sar,
                binderArch,
                cpuArch,
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
    windowSizeClass: WindowSizeClass,
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    vab: Optional<VABResult?>,
    sar: Boolean?,
    binderArch: BinderArch,
    cpuArch: CPUArch,
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
                                if (navController.currentDestination?.route != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = !selected
                                    }
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
                composable(Screens.Details.route) {
                    DetailsList(innerPadding, windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact, treble, ab, dynamic, vab, sar, binderArch, cpuArch)
                }
                composable(Screens.Licenses.route) { Licenses(innerPadding) }
                composable(Screens.Contribute.route) { Contribute(
                    { TODO() },
                    {
                        navController.navigate(Screens.ReportBug.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    { TODO() },
                    { TODO() },
                    { TODO() },
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


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
fun MainActivityPreview() {
    TrebleCheckTheme(darkTheme = false) {
        MainActivityContent(
            WindowSizeClass.calculateFromSize(DpSize(400.dp, 400.dp)),
            Optional.Value(
                TrebleResult(false, true, 30, 0)
            ),
            true,
            true,
            Optional.Value(
                VABResult(true, true)
            ),
            true,
            BinderArch.Binder8,
            CPUArch.ARM64,
            "system-arm64-ab.img.xz"
        ) {  }
    }
}

private const val tag = "MainActivity"