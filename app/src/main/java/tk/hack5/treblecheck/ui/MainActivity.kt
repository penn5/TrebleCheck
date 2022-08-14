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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.data.*
import tk.hack5.treblecheck.getOrNull
import tk.hack5.treblecheck.ui.theme.TrebleCheckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrebleCheckTheme(darkTheme = false) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val treble = remember { try {
                        Optional.Value(TrebleDetector.getVndkData())
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get VNDK data", e)
                        Optional.Nothing
                    }}
                    val ab = remember { try {
                        ABDetector.checkAB()
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get AB status", e)
                        null
                    }}
                    val dynamic = remember { try {
                        DynamicPartitionsDetector.isDynamic()
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get Dynamic Partitions status", e)
                        null
                    }}
                    val vab = remember { try {
                        Optional.Value(VABDetector.getVABData())
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get VAB status", e)
                        Optional.Nothing
                    }}
                    val sar = remember {try {
                        MountDetector.isSAR()
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get SAR status", e)
                        null
                    }}
                    val arch = remember { try {
                        ArchDetector.getArch()
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get arch", e)
                        Arch.UNKNOWN(null, null)
                    }}
                    val fileName = remember { try {
                        treble.getOrNull()?.let { FileNameAnalyzer.getFileName(it, arch, sar) }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to generate filename", e)
                        null
                    }}


                    AllEntries(treble, ab, dynamic, vab, sar, arch, fileName)
/*
                    MainCards(Optional.Value(TrebleResult(false, true, 30, 0)), true, true, Optional.Value(
                        VABResult(true, true)
                    ), true, Arch.ARM32_BINDER64, "system-arm64-ab.img.xz")
  */              }
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    TrebleCheckTheme(darkTheme = false) {
        MainScreen(
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
        )
    }
}

@Composable
fun MainScreen(
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    vab: Optional<VABResult?>,
    sar: Boolean?,
    arch: Arch,
    fileName: String?
) {

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
data class Entry(val icon: Painter, val actionIcon: Painter?, val tint: Color, val body: String, val header: String, val detail: String, val clickAction: ClickAction?, val accented: Boolean = false, val expandedContent: List<Entry>? = null)




private const val tag = "MainActivity"