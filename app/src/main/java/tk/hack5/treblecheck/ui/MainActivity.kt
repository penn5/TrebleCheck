/*
 *     Treble Info
 *     Copyright (C) 2022-2023 Hackintosh Five
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

package tk.hack5.treblecheck.ui

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import tk.hack5.treblecheck.*
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.*
import tk.hack5.treblecheck.ui.screens.*

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            var donationPopup by rememberSaveable { mutableStateOf<Boolean?>(null) }

            val listener = remember {
                object : IABListener {
                    override fun paymentFailed() {
                        donationPopup = false
                    }

                    override fun paymentSuccess() {
                        donationPopup = true
                    }
                }
            }
            val scope = rememberCoroutineScope()
            val iabHelper = remember { IABHelper(this, listener, scope) }

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
                sar,
                binderArch,
                cpuArch,
                fileName,
                {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/phhusson/treble_experimentations/wiki/Generic-System-Image-%28GSI%29-list"))
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.w(tag, "Launch browser failed", e)
                        Toast.makeText(this, R.string.no_browser, Toast.LENGTH_LONG).show()
                    }
                },
                {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("contact-project+trebleinfo-trebleinfo-30453147-issue-@incoming.gitlab.com"))
                    }
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.w(tag, "Send email failed", e)
                        Toast.makeText(this, R.string.no_email, Toast.LENGTH_LONG).show()
                    }
                },
                {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("treble@hack5.dev"))
                    }
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.w(tag, "Send email failed", e)
                        Toast.makeText(this, R.string.no_email, Toast.LENGTH_LONG).show()
                    }
                },
                {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/TrebleInfo/TrebleInfo/-/blob/dev/TRANSLATING.md"))
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.w(tag, "Launch browser failed", e)
                        Toast.makeText(this, R.string.no_browser, Toast.LENGTH_LONG).show()
                    }
                },
                {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/TrebleInfo/TrebleInfo#contributing"))
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.w(tag, "Launch browser failed", e)
                    }
                },
                { scope.launch { iabHelper.makePayment() } },
                donationPopup,
                { donationPopup = null },
            )
        }
    }
}

sealed class Screen(val route: String)

sealed class RootScreen(route: String, @StringRes val title: Int, @DrawableRes val icon: Int) : Screen(route)

object Screens {
    object Images : RootScreen("images", R.string.screen_images, R.drawable.screen_images)
    object Details : RootScreen("details", R.string.screen_details, R.drawable.screen_details)
    object Licenses : RootScreen("licenses", R.string.screen_licenses, R.drawable.screen_licenses)
    object Contribute : RootScreen("contribute", R.string.screen_contribute, R.drawable.screen_contribute)
}

val screens = listOf(Screens.Images, Screens.Details, Screens.Licenses, Screens.Contribute)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    windowSizeClass: WindowSizeClass,
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    sar: Boolean?,
    binderArch: BinderArch,
    cpuArch: CPUArch,
    fileName: String?,
    browseImages: () -> Unit,
    reportABug: () -> Unit,
    askAQuestion: () -> Unit,
    helpTranslate: () -> Unit,
    contributeCode: () -> Unit,
    donate: () -> Unit,
    donationPopup: Boolean?,
    dismissDonationPopup: () -> Unit,
) {
    val navController = rememberNavController()
    val topAppBarState = remember(navController.currentBackStackEntryAsState().value) { TopAppBarState(-Float.MAX_VALUE, 0f, 0f) }
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    TrebleCheckTheme(darkTheme = Mock.data?.theme ?: isSystemInDarkTheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(id = R.string.title), Modifier.padding(WindowInsets.safeDrawing.asPaddingValues().horizontal()))
                            },
                    scrollBehavior = topAppBarScrollBehavior,
                    // disable elevation
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, scrolledContainerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                NavigationBar(
                    Modifier.fillMaxWidth(),
                    // handle large waterfalls
                    windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    screens.forEach { screen ->
                        val selected = remember(navBackStackEntry) { navController.backQueue.lastOrNull { entry -> screens.any { it.route == entry.destination.route } }?.destination?.route == screen.route }
                        NavigationBarItem(
                            selected = selected,
                            icon = { Icon(painterResource(screen.icon), null) },
                            label = { Text(stringResource(screen.title), maxLines = 1) },
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
            donationPopup?.let {
                AlertDialog(
                    onDismissRequest = dismissDonationPopup,
                    confirmButton = { TextButton(onClick = dismissDonationPopup) { Text(stringResource(R.string.close_dialog)) } },
                    title = {
                        if (it) {
                            Text(stringResource(R.string.donation_successful_title))
                        } else {
                            Text(stringResource(R.string.donation_failed_title))
                        }
                    },
                    text = {
                        if (it) {
                            Text(stringResource(R.string.donation_successful_body), style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(stringResource(R.string.donation_failed_body), style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                )
            }

            NavHost(navController = navController, startDestination = "images") {
                composable(Screens.Images.route) { Images(
                    innerPadding,
                    topAppBarScrollBehavior.nestedScrollConnection,
                    browseImages,
                    {
                        navController.navigate(Screens.Details.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    reportABug,
                    treble.supported,
                    fileName
                ) }
                composable(Screens.Details.route) {
                    DetailsList(innerPadding, topAppBarScrollBehavior.nestedScrollConnection, windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact, treble, ab, dynamic, sar, binderArch, cpuArch)
                }
                composable(Screens.Licenses.route) { Licenses(innerPadding, topAppBarScrollBehavior.nestedScrollConnection) }
                composable(Screens.Contribute.route) { Contribute(
                    innerPadding,
                    topAppBarScrollBehavior.nestedScrollConnection,
                    askAQuestion,
                    reportABug,
                    helpTranslate,
                    contributeCode,
                    donate
                ) }
            }
        }
    }
}


@Suppress("BooleanLiteralArgument")
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
            true,
            BinderArch.Binder8,
            CPUArch.ARM64,
            "system-arm64-ab.img.xz",
            { },
            { },
            { },
            { },
            { },
            { },
            null,
            { },
        )
    }
}

private const val tag = "MainActivity"