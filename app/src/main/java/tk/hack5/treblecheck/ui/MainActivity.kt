/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2022 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.*
import tk.hack5.treblecheck.getOrNull
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(showBackButtonTransition: Transition<Boolean>, specs: TransitionSpecs, goBack: () -> Unit) {
    SmallTopAppBar(
        title = { Text(stringResource(id = R.string.title)) },
        navigationIcon = {
            showBackButtonTransition.AnimatedVisibility(visible = { it }, enter = expandIn(specs.expandShrink) + fadeIn(specs.fade), exit = shrinkOut(specs.expandShrink) + fadeOut(specs.fade)) {
                IconButton(onClick = { goBack() }) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTransitionApi::class)
@Composable
fun MainActivityContent(
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    vab: Optional<VABResult?>,
    sar: Boolean?,
    arch: Arch,
    fileName: String?,
    onRequiredImageClick: () -> Unit
) {
    var rootExpandedIndex: Int by rememberSaveable { mutableStateOf(-1) }
    val rootExpandedIndexTransition = updateTransition(rootExpandedIndex, label = "rootExpandedIndex")
    val defaultSpecs = TransitionSpecs(
        crossfade = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 0.03125f),
        expandShrink = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntSize(1, 1)),
        fade = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 0.03125f),
        padding = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 0.1.dp),
        height = spring(stiffness = Spring.StiffnessMediumLow),
        top = spring(stiffness = Spring.StiffnessMediumLow)
    )

    val slowSpecs = defaultSpecs.scale(4f) // TODO remove

    TrebleCheckTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopBar(
                    rootExpandedIndexTransition.createChildTransition { it != -1 },
                    slowSpecs
                ) { rootExpandedIndex = -1 }
            }
        ) { innerPadding ->
            AutoList(
                Modifier
                    .padding(innerPadding) /* TODO maybe apply on the inside? */,
                cardOuterHorizontalPadding,
                cardOuterVerticalSeparation,
                rootExpandedIndexTransition,
                { rootExpandedIndex = it },
                slowSpecs
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
            }


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
fun DisplayedEntry(modifier: Modifier = Modifier, entry: Entry, isTitle: Transition<Boolean>, specs: TransitionSpecs, onClick: () -> Unit) {
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