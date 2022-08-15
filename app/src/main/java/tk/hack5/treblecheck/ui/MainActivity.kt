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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.*
import tk.hack5.treblecheck.getOrNull
import tk.hack5.treblecheck.ui.ScaledPaddingValues.Companion.times
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
fun TopBar(showBackButtonTransition: Transition<Boolean>, goBack: () -> Unit) {
    SmallTopAppBar(
        title = { Text(stringResource(id = R.string.title)) },
        navigationIcon = {
            showBackButtonTransition.AnimatedVisibility(visible = { it }) {
                IconButton(onClick = { goBack() }) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                }
            }
        }
    )
}

sealed class AutoListEntry {
    class PlainEntry(val content: @Composable () -> ClickableEntry) : AutoListEntry()
    class ExpandableEntry(val content: List<AutoListEntry>) : AutoListEntry()
}

val AutoListEntry.collapsed: @Composable () -> ClickableEntry get() {
    var entry = this
    while (entry is AutoListEntry.ExpandableEntry) {
        entry = entry.content.first()
    }
    return (entry as AutoListEntry.PlainEntry).content
}

class AutoListScope {
    val contents = mutableListOf<AutoListEntry>()


    fun plain(content: @Composable () -> ClickableEntry) {
        contents.add(AutoListEntry.PlainEntry(content))
    }

    fun expandable(content: AutoListScope.() -> Unit) {
        contents.add(AutoListEntry.ExpandableEntry(AutoListScope().also(content).contents))
    }
}

/**
 * Sometimes we don't want a real transition, this lets us declare that we won't animate it.
 */
@Composable
inline fun <reified T>createFixedTransition(value: T) = updateTransition(value, label = "dummy")


@Composable
fun AutoList(
    modifier: Modifier,
    entryPadding: PaddingValues,
    separator: Dp,
    rootExpandedIndexTransition: Transition<Int>,
    setRootExpandedIndex: (Int) -> Unit,
    specs: TransitionSpecs,
    content: AutoListScope.() -> Unit,
) {
    val contents = AutoListScope().also(content).contents

    AutoListExpandableEntry(modifier, rootExpandedIndexTransition, setRootExpandedIndex, contents, entryPadding, separator, createFixedTransition(false), specs)


}
@OptIn(ExperimentalTransitionApi::class)
@Composable
private fun AutoListExpandableEntry(
    modifier: Modifier,
    expandedIndexTransition: Transition<Int>,
    setExpandedIndex: (Int) -> Unit,
    contents: List<AutoListEntry>,
    entryPadding: PaddingValues,
    separator: Dp,
    firstChildTitle: Transition<Boolean>,
    specs: TransitionSpecs
) {
    Surface {
        ExpandableColumn(
            modifier
                .fillMaxSize(),
            separator = separator,
            expandedIndex = expandedIndexTransition,
            expandIndex = setExpandedIndex,
            heightSpec = specs.height,
            topSpec = specs.top
        ) {
            contents.forEachIndexed { i, entry ->
                when (entry) {
                    is AutoListEntry.PlainEntry -> plain {
                        val isTitle = i == 0 && firstChildTitle.targetState
                        val entryContent = entry.content()
                        DisplayedEntry(
                            Modifier.padding(if (isTitle) PaddingValues(0.dp) else entryPadding),
                            entryContent.entry,
                            firstChildTitle,
                            specs,
                            entryContent.onClick ?: { }
                        )
                    }
                    is AutoListEntry.ExpandableEntry -> expandable {
                        if (transition.isRunning) {
                            Surface {
                                Column {
                                    val firstChildPaddingScale by transition.animateFloat(label = "Padding for the first child", transitionSpec = { specs.padding }) { if (it) 0f else 1f }
                                    val fixedFalse = createFixedTransition(false)

                                    entry.content.map(AutoListEntry::collapsed).forEachIndexed { childIndex, child ->
                                        DisplayedEntry(
                                            Modifier.padding(if (childIndex == 0) entryPadding * firstChildPaddingScale else entryPadding),
                                            child().entry,
                                            if (childIndex == 0) transition else fixedFalse,
                                            specs,
                                            onClick
                                        )
                                    }
                                }
                            }
                        } else if (transition.currentState) {
                            var innerExpandedIndex: Int by rememberSaveable { mutableStateOf(-1) }
                            val innerExpandedIndexTransition = updateTransition(innerExpandedIndex, label = "moreInfoExpandedIndex")

                            AutoListExpandableEntry(
                                modifier = Modifier,
                                expandedIndexTransition = innerExpandedIndexTransition,
                                setExpandedIndex = { innerExpandedIndex = it },
                                contents = entry.content,
                                entryPadding = entryPadding,
                                separator = separator,
                                firstChildTitle = transition.createChildTransition(label = "First child is title") { it },
                                specs = specs
                            )
                        } else {
                            val content = (entry.content[0] as AutoListEntry.PlainEntry).content

                            DisplayedEntry(Modifier.padding(entryPadding), content().entry, transition, specs, onClick)
                        }
                    }
                }
            }
        }
    }
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
        crossfade = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 0.00390625f),
        expandShrink = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntSize(1, 1)),
        fade = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 0.00390625f),
        padding = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 0.00390625f),
        height = spring(stiffness = Spring.StiffnessMediumLow),
        top = spring(stiffness = Spring.StiffnessMediumLow)
    )

    val slowSpecs = defaultSpecs.scale(10f)

    TrebleCheckTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopBar(
                    rootExpandedIndexTransition.createChildTransition { it != -1 }
                ) { rootExpandedIndex = -1 }
            }
        ) { innerPadding ->
            AutoList(
                Modifier
                    .padding(innerPadding) /* TODO maybe apply on the inside? */,
                cardOuterPadding,
                10.dp,
                rootExpandedIndexTransition,
                { rootExpandedIndex = it },
                slowSpecs
            ) {
                plain { requiredImageEntry(fileName).clickable(onRequiredImageClick) }
                expandable {
                    plain { moreInfoEntry().clickable() }
                    expandable {
                        plain { trebleEntry().clickable() }
                    }
                }
                expandable {
                    plain { licenseEntry().clickable() }
                }
                repeat(10) {
                    expandable {
                        plain { contributeEntry().clickable() }
                        // plain { translateEntry() }
                        // plain { donateEntry() }
                    }
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
                    Box(
                        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
                    ) {
                        cardContent()
                    }
                } else {
                    OutlinedCard(
                        modifier = modifier.fillMaxWidth(),
                        onClick = onClick
                    ) {
                        cardContent()
                    }
                }
            }
        }
    }
}

sealed class Entry

class BasicEntry(
    val icon: Painter,
    val tint: Color,
    val body: String,
    val header: String,
    val detail: String?,
) : Entry()

class ClickableEntry(
    val entry: Entry,
    val onClick: (() -> Unit)?,
)

fun Entry.clickable(onClick: (() -> Unit)? = null) = ClickableEntry(this, onClick)


private const val tag = "MainActivity"