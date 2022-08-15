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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.createChildTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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

enum class DetailType {
    TREBLE,
    SAR,
    PARTITIONS,
    ARCH,
}

sealed class Screen(val ordinal: Int) {
    object MAIN : Screen(0)
    object LIST : Screen(1)
    object LICENSE : Screen(2)
    object CONTRIBUTE : Screen(3)
    class Detail(val type: DetailType) : Screen(-type.ordinal)

    companion object {
        fun fromOrdinal(ordinal: Int) = when (ordinal) {
            0 -> MAIN
            1 -> LIST
            2 -> LICENSE
            3 -> CONTRIBUTE
            else -> Detail(DetailType.values()[-ordinal])
        }

        val Saver = Saver(
            { it.ordinal },
            ::fromOrdinal
        )
        val StateSaver = Saver<MutableState<Screen>, Int>(
            { it.value.ordinal },
            { mutableStateOf(fromOrdinal(it)) }
        )
    }
}

fun triggerDonation() {
    TODO()
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
    class PlainEntry(val content: @Composable () -> Entry) : AutoListEntry()
    class ExpandableEntry(val content: List<AutoListEntry>) : AutoListEntry()
}

val AutoListEntry.collapsed: @Composable () -> Entry get() {
    var entry = this
    while (entry is AutoListEntry.ExpandableEntry) {
        entry = entry.content.first()
    }
    return (entry as AutoListEntry.PlainEntry).content
}

class AutoListScope {
    val contents = mutableListOf<AutoListEntry>()


    fun plain(content: @Composable () -> Entry) {
        contents.add(AutoListEntry.PlainEntry(content))
    }

    fun expandable(content: AutoListScope.() -> Unit) {
        contents.add(AutoListEntry.ExpandableEntry(AutoListScope().also(content).contents))
    }
}


@Composable
fun AutoList(
    modifier: Modifier,
    entryPadding: PaddingValues,
    rootExpandedIndexTransition: Transition<Int?>,
    setRootExpandedIndex: (Int) -> Unit,
    content: AutoListScope.() -> Unit
) {
    val contents = AutoListScope().also(content).contents

    AutoListExpandableEntry(modifier, rootExpandedIndexTransition, setRootExpandedIndex, contents, entryPadding)


}
@Composable
private fun AutoListExpandableEntry(
    modifier: Modifier,
    expandedIndexTransition: Transition<Int?>,
    setExpandedIndex: (Int) -> Unit,
    contents: List<AutoListEntry>,
    entryPadding: PaddingValues
) {
    Surface {
        ExpandableColumn(
            modifier
                .fillMaxSize(),
            expandedIndex = expandedIndexTransition,
            expandIndex = setExpandedIndex
        ) {
            contents.forEach {
                when (it) {
                    is AutoListEntry.PlainEntry -> plain {
                        DisplayedEntry(Modifier.padding(entryPadding), it.content(), true, /* TODO */ {})
                    }
                    is AutoListEntry.ExpandableEntry -> expandable {
                        //val paddingScale by transition.animateFloat(label = "Padding") { if (it) 0f else 1f }

                        if (transition.isRunning) {
                            Surface {
                                Column {
                                    it.content.map(AutoListEntry::collapsed).forEach { child ->
                                        DisplayedEntry(Modifier.padding(entryPadding), child(), true, onClick)
                                    }
                                }
                            }
                        } else if (transition.currentState || transition.isRunning) {
                            var innerExpandedIndex: Int? by rememberSaveable { mutableStateOf(null) }
                            val innerExpandedIndexTransition = updateTransition(innerExpandedIndex, label = "moreInfoExpandedIndex")

                            AutoListExpandableEntry(
                                modifier = Modifier,
                                expandedIndexTransition = innerExpandedIndexTransition,
                                setExpandedIndex = { i -> innerExpandedIndex = i },
                                contents = it.content,
                                entryPadding = entryPadding
                            )
                        } else {
                            val content = (it.content[0] as AutoListEntry.PlainEntry).content

                            DisplayedEntry(Modifier.padding(entryPadding), content(), true, onClick)
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
    var rootExpandedIndex: Int? by rememberSaveable { mutableStateOf(null) }
    val rootExpandedIndexTransition = updateTransition(rootExpandedIndex, label = "rootExpandedIndex")

    TrebleCheckTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopBar(
                    rootExpandedIndexTransition.createChildTransition { it != null }
                ) { rootExpandedIndex = null }
            }
        ) { innerPadding ->
            AutoList(
                Modifier
                    .padding(innerPadding) /* TODO maybe apply on the inside? */,
                cardOuterPadding,
                rootExpandedIndexTransition,
                { rootExpandedIndex = it }
            ) {
                plain { requiredImageEntry(fileName) }
                expandable {
                    plain { moreInfoEntry() }
                    expandable {
                        plain { trebleEntry() }
                    }
                }
                expandable {
                    plain { licenseEntry() }
                }
                expandable {
                    plain { contributeEntry() }
                    // plain { translateEntry() }
                    // plain { donateEntry() }
                }
            }


/*
            Surface {
            ExpandableColumn(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding) /* TODO maybe apply on the inside? */,
                expandedIndex = rootExpandedIndexTransition,
                expandIndex = { rootExpandedIndex = it }
            ) {
                plain {
                    DisplayedEntry(Modifier.padding(cardOuterPadding), requiredImageEntry(fileName), true, onRequiredImageClick)
                }
                expandable {
                    val moreInfoPaddingScale by transition.animateFloat(label = "Padding") { if (it) 0f else 1f }
                    if (transition.currentState) {
                        var moreInfoExpandedIndex: Int? by rememberSaveable { mutableStateOf(null) }
                        val moreInfoExpandedIndexTransition = updateTransition(moreInfoExpandedIndex, label = "moreInfoExpandedIndex")

                        Surface {
                            ExpandableColumn(
                                expandedIndex = moreInfoExpandedIndexTransition,
                                expandIndex = { moreInfoExpandedIndex = it }
                            ) {
                                plain {
                                    DisplayedEntry(Modifier.padding(cardOuterPadding), moreInfoEntry(), true, onClick)
                                }
                                expandable {
                                    val treblePaddingScale by transition.animateFloat(label = "Padding") { if (it) 0f else 1f }
                                    DisplayedEntry(Modifier.padding(cardOuterPadding * treblePaddingScale), trebleEntry(), true, onClick)
                                }
                                expandable {
                                    val sarPaddingScale by transition.animateFloat(label = "Padding") { if (it) 0f else 1f }
                                    DisplayedEntry(Modifier.padding(cardOuterPadding * sarPaddingScale), sarEntry(), true, onClick)
                                }
                                expandable {
                                    val partitionsPaddingScale by transition.animateFloat(label = "Padding") { if (it) 0f else 1f }
                                    DisplayedEntry(Modifier.padding(cardOuterPadding * partitionsPaddingScale), partitionsEntry(), true, onClick)
                                }
                                expandable {
                                    val archPaddingScale by transition.animateFloat(label = "Padding") { if (it) 0f else 1f }
                                    DisplayedEntry(Modifier.padding(cardOuterPadding * archPaddingScale), archEntry(), true, onClick)
                                }
                            }
                        }
                    } else {
                        DisplayedEntry(Modifier.padding(cardOuterPadding * moreInfoPaddingScale), moreInfoEntry(), transition.currentState, onClick)
                    }
                }
                expandable {
                    val licensePaddingScale by transition.animateFloat(label = "Padding") { if (it) 0f else 1f }
                    DisplayedEntry(Modifier.padding(cardOuterPadding * licensePaddingScale), licenseEntry(), true, onClick)
                }
                repeat(10) {
                expandable {
                    val contributePaddingScale by transition.animateFloat(label = "Padding") { if (it) 0f else 1f }
                    DisplayedEntry(Modifier.padding(cardOuterPadding * contributePaddingScale), contributeEntry(), true, onClick)
                }
                }
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

/*
@Composable
fun MainScreen(
    fileName: String?,
    innerPadding: PaddingValues,
    onRequiredImageClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onContributeClick: () -> Unit
) {
    Column(
        Modifier
            .scrollable(rememberScrollState(), Orientation.Vertical)
            .padding(innerPadding)
    ) {
        DisplayedEntry(requiredImageEntry(fileName), true, onRequiredImageClick)
        DisplayedEntry(moreInfoEntry(), true, onMoreInfoClick)
        DisplayedEntry(licenseEntry(), true, onLicenseClick)
        DisplayedEntry(contributeEntry(), true, onContributeClick)
    }
}
*/
@Composable
fun DisplayedEntry(modifier: Modifier = Modifier, entry: Entry, border: Boolean, onClick: () -> Unit) {
    ClickableCardWithContent(
        modifier = modifier,
        border = border,
        icon = entry.icon,
        iconTint = entry.tint,
        onClick = onClick,
        title = entry.header,
        htmlBody = entry.body,
        detail = entry.detail
    )
}


/*
@OptIn(ExperimentalAnimationApi::class, ExperimentalContracts::class)
@Composable
fun AllEntries(
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    vab: Optional<VABResult?>,
    sar: Boolean?,
    arch: Arch,
    fileName: String?
) {
    val animationParameters = AnimationParameters.DEFAULT




    val entries = listOf(
        requiredImageEntry(fileName),
        trebleEntry(treble).copy(expandedContent = listOfNotNull(
            treble.getOrNull()?.let { trebleVersionEntry(it) },
            treble.getOrNull()?.let { trebleLiteEntry(it) },
            treble.getOrNull()?.let { trebleLegacyEntry(it) }
        )),
        sarEntry(sar).copy(expandedContent = listOf(
            abEntry(ab)
        )),
        dynamicPartitionsEntry(dynamic).copy(expandedContent = listOfNotNull(
            vabEntry(vab),
            vab.getOrNull()?.let { vabrEntry(it) },
            vab.getOrNull()?.let { vabcEntry(it) }
        )),
        archEntry(arch)
    )

    var expanded: Entry? by remember { mutableStateOf(null) }

    var expandCardsInPlace: Boolean

    Grid2(
        minColumnWidth = 128.dp,
        maxColumnWidth = 256.dp
    ) {
        expandCardsInPlace = maxColumns == 1
        entries.forEach { entry ->
            group {
                ListedEntry(entry, entry.accented || (expanded == entry && expandCardsInPlace), AnimationParameters.DEFAULT, object : ClickActionExecutor {
                    override fun click(action: ClickAction) {
                        when (action) {
                            is ClickAction.CopyText -> { /* TODO */ }
                            ClickAction.ToggleDetail -> {
                                expanded = entry
                            }
                        }
                    }
                })
            }
        }
    }

    if (!expandCardsInPlace) {
        expanded?.let { entry ->
            Dialog(
                onDismissRequest = { expanded = null }
            ) {
                ListedEntry(entry, true, AnimationParameters.DEFAULT, object : ClickActionExecutor {
                    override fun click(action: ClickAction) {
                        when (action) {
                            is ClickAction.CopyText -> error("ClickAction changed")
                            ClickAction.ToggleDetail -> {
                                expanded = null
                            }
                        }
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OldAllEntries(
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    vab: Optional<VABResult?>,
    sar: Boolean?,
    arch: Arch,
    fileName: String?
) {
    val animationParameters = AnimationParameters.DEFAULT

    //Box(Modifier.scrollable(rememberScrollState(), Orientation.Vertical)) {
        AdaptiveGrid2(
            minColumnWidth = 128.dp/*, modifier = Modifier
                .scrollable(rememberScrollState(), Orientation.Vertical)
            //.wrapContentHeight()*/
        ) {
            val entries = listOfNotNull(
                requiredImageEntry(fileName),
                trebleEntry(treble),
                treble.getOrNull()?.let { trebleVersionEntry(it) },
                treble.getOrNull()?.let { trebleLiteEntry(it) },
                treble.getOrNull()?.let { trebleLegacyEntry(it) },
                sarEntry(sar),
                abEntry(ab),
                dynamicPartitionsEntry(dynamic),
                vabEntry(vab),
                vab.getOrNull()?.let { vabrEntry(it) },
                vab.getOrNull()?.let { vabcEntry(it) },
                archEntry(arch)
            )
            var expanded: Int? by remember { mutableStateOf(null) }
            entries.forEachIndexed { i, it ->
                if (numColumns == 1) {
                    ListedEntry(
                        it,
                        expanded == i,
                        animationParameters,
                        object : ClickActionExecutor {
                            override fun click(action: ClickAction) {
                                when (action) {
                                    is ClickAction.CopyText -> TODO()
                                    ClickAction.ToggleDetail -> if (expanded == i) expanded =
                                        null else expanded = i
                                }
                            }
                        }
                    )
                } else {
                    ListedEntry(
                        it,
                        expanded == i,
                        animationParameters,
                        object : ClickActionExecutor {
                            override fun click(action: ClickAction) {
                                when (action) {
                                    is ClickAction.CopyText -> TODO()
                                    ClickAction.ToggleDetail -> if (expanded == i) expanded =
                                        null else expanded = i
                                }
                            }
                        }
                    )
                }
            }
        }



/*
    LazyVerticalGrid(
        GridCells.Adaptive(minSize = 128.dp)
    ) {
        if (treble.getOrNull() != null) {
            item {
                RequiredImageCard(fileName, animationParameters, expanded == 0)
            }
        }
        item {
            TrebleCard(treble, animationParameters, expanded in 1..4) { expanded = if (expanded in 1..4) 0 else 1 }
        }
        treble.getOrNull()?.let {
            item {
                TrebleVersionCard(it, animationParameters, expanded == 2) { expanded = if (expanded == 2) 1 else 2 }
            }
            item {
                TrebleLiteCard(it, animationParameters, expanded == 3) { expanded = if (expanded == 3) 1 else 3 }
            }
            item {
                TrebleLegacyCard(it, animationParameters, expanded == 4) { expanded = if (expanded == 4) 1 else 4 }
            }
        }
        item {
            SARCard(sar, animationParameters, expanded in 5..10) { expanded = if (expanded in 5..10) 0 else 5 }
        }
        item {
            DynamicPartitionsCard(dynamic, animationParameters, expanded == 6) { expanded = if (expanded == 6) 5 else 6 }
        }
        item {
            ABCard(ab, animationParameters, expanded == 7) { expanded = if (expanded == 7) 5 else 7 }
        }
        item {
            VABCard(vab, animationParameters, expanded in 8..10) { expanded = if (expanded in 8..10) 5 else 8 }
        }
        vab.getOrNull()?.let {
            item {
                VABRCard(it, animationParameters, expanded == 9) { expanded = if (expanded == 9) 8 else 9 }
            }
            item {
                VABCCard(it, animationParameters, expanded == 10) { expanded = if (expanded == 10) 8 else 10 }
            }
        }
        item {
            ArchCard(arch, animationParameters, expanded == 11) { expanded = if (expanded == 11) 0 else 11 }
        }
    }*/
}

@Composable
fun ListedEntry(entry: Entry, expanded: Boolean, animationParameters: AnimationParameters, clickActionExecutor: ClickActionExecutor) = entry.run {
    ClickableIconCard(
        Modifier.semantics {
            when (clickAction) {
                ClickAction.ToggleDetail ->
                    if (expanded) {
                        collapse { clickActionExecutor.click(clickAction); true }
                    } else {
                        expand { clickActionExecutor.click(clickAction); true }
                    }
                is ClickAction.CopyText -> {
                    copyText { clickActionExecutor.click(clickAction); true }
                }
                null -> { }
            }
        },
        icon,
        tint,
        onClick = { clickAction?.let { clickActionExecutor.click(it) } }
    ) {
        TextCardContent(
            header,
            body,
            detail,
            expanded,
            animationParameters
        )
    }
}
*/
interface ClickActionExecutor {
    fun click(action: ClickAction)
}
sealed class ClickAction {
    object ToggleDetail : ClickAction()
    class CopyText(val title: String, val text: String) : ClickAction()
}
data class Entry(
    val icon: Painter,
    val tint: Color,
    val body: String,
    val header: String,
    val detail: String?
)




private const val tag = "MainActivity"