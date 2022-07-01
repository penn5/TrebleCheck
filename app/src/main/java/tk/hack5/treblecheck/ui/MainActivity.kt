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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.copyText
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import tk.hack5.treblecheck.*
import tk.hack5.treblecheck.R
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
                    val treble = try {
                        Optional.Value(TrebleDetector.getVndkData())
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get VNDK data", e)
                        Optional.Nothing
                    }
                    val ab = try {
                        ABDetector.checkAB()
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get AB status", e)
                        null
                    }
                    val dynamic = try {
                        DynamicPartitionsDetector.isDynamic()
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get Dynamic Partitions status", e)
                        null
                    }
                    val vab = try {
                        Optional.Value(VABDetector.getVABData())
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get VAB status", e)
                        Optional.Nothing
                    }
                    val sar = try {
                        MountDetector.isSAR()
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get SAR status", e)
                        null
                    }
                    val arch = try {
                        ArchDetector.getArch()
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get arch", e)
                        Arch.UNKNOWN(null, null)
                    }
                    val fileName = try {
                        treble.getOrNull()?.let { FileNameAnalyzer.getFileName(it, arch, sar) }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to generate filename", e)
                        null
                    }


                    MainCards(treble, ab, dynamic, vab, sar, arch, fileName)
/*
                    MainCards(Optional.Value(TrebleResult(false, true, 30, 0)), true, true, Optional.Value(
                        VABResult(true, true)
                    ), true, Arch.ARM32_BINDER64, "system-arm64-ab.img.xz")
  */              }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainCards(
    treble: Optional<TrebleResult?>,
    ab: Boolean?,
    dynamic: Boolean?,
    vab: Optional<VABResult?>,
    sar: Boolean?,
    arch: Arch,
    fileName: String?
) {
    val animationParameters = AnimationParameters.DEFAULT
    var expanded by remember { mutableStateOf(0) }
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .wrapContentHeight()) {
        if (treble is Optional.Value) {
            RequiredImageCard(fileName, animationParameters, expanded == 0)
        }
        TrebleCard(treble, animationParameters, expanded in 1..4) { expanded = if (expanded in 1..4) 0 else 1 }
        treble.getOrNull()?.let {
            AnimatedVisibility(expanded in 1..4) {
                Column {
                    TrebleVersionCard(it, animationParameters, expanded == 2) { expanded = if (expanded == 2) 1 else 2 }
                    TrebleLiteCard(it, animationParameters, expanded == 3) { expanded = if (expanded == 3) 1 else 3 }
                    TrebleLegacyCard(it, animationParameters, expanded == 4) { expanded = if (expanded == 4) 1 else 4 }
                }
            }
        }
        SARCard(sar, animationParameters, expanded in 5..10) { expanded = if (expanded in 5..10) 0 else 5 }
        AnimatedVisibility(expanded in 5..10) {
            Column {
                DynamicPartitionsCard(dynamic, animationParameters, expanded == 6) { expanded = if (expanded == 6) 5 else 6 }
                ABCard(ab, animationParameters, expanded == 7) { expanded = if (expanded == 7) 5 else 7 }
                VABCard(vab, animationParameters, expanded in 8..10) { expanded = if (expanded in 8..10) 5 else 8 }
                vab.getOrNull()?.let {
                    AnimatedVisibility(expanded in 8..10, exit = ExitTransition.None) {
                        Column {
                            VABRCard(it, animationParameters, expanded == 9) { expanded = if (expanded == 9) 8 else 9 }
                            VABCCard(it, animationParameters, expanded == 10) { expanded = if (expanded == 10) 8 else 10 }
                        }
                    }
                }
            }
        }
        ArchCard(arch, animationParameters, expanded == 11) { expanded = if (expanded == 11) 0 else 11 }
    }
}

@Composable
fun RequiredImageCard(fileName: String?, animationParameters: AnimationParameters, expanded: Boolean) {
    val context = LocalContext.current
    val onClick: () -> Unit = {
        context.run {
            (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let {
                it.setPrimaryClip(
                    ClipData.newPlainText(
                        getString(R.string.filename_header),
                        fileName
                    )
                )
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
            }
        }
    }
    ClickableIconCard(
        Modifier.semantics {
            copyText { onClick(); true }
        },
        painterResource(if (fileName == null) R.drawable.filename_unknown else R.drawable.filename_known),
        if (fileName == null) Error else Green,
        onClick
    ) {
        TextCardContent(
            stringResource(R.string.filename_header),
            fileName?.let { stringResource(R.string.filename, it) } ?: stringResource(R.string.filename_unknown),
            stringResource(R.string.filename_explanation),
            expanded,
            animationParameters,
            icon = painterResource(R.drawable.copy)
        )
    }
}

@Composable
fun TrebleCard(treble: Optional<TrebleResult?>, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (treble) {
        is Optional.Nothing -> R.drawable.unknown
        is Optional.Value -> when {
            treble.value == null -> R.drawable.treble_false
            treble.value.legacy || treble.value.lite -> R.drawable.treble_legacy
            else -> R.drawable.treble_modern
        }
    }
    val tint = when (treble) {
        is Optional.Nothing -> Error
        is Optional.Value -> when {
            treble.value == null -> Red
            treble.value.legacy || treble.value.lite -> Orange
            else -> Green
        }
    }
    @StringRes val body = when (treble) {
        is Optional.Nothing -> R.string.treble_unknown
        is Optional.Value -> when (treble.value) {
            null -> R.string.treble_false
            else -> R.string.treble_true
        }
    }

    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.treble_header),
            stringResource(body),
            stringResource(R.string.treble_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable fun TrebleVersionCard(treble: TrebleResult, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = R.drawable.treble_version
    val tint = Green
    @StringRes val body = R.string.treble_version
    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.treble_version_header),
            stringResource(body, treble.vndkVersion, treble.vndkSubVersion),
            stringResource(R.string.treble_version_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable fun TrebleLiteCard(treble: TrebleResult, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (treble.lite) {
        false -> R.drawable.treble_lite_false
        true -> R.drawable.treble_lite_true
    }
    val tint = when (treble.lite) {
        false -> Green
        true -> Orange
    }
    @StringRes val body = when (treble.lite) {
        false -> R.string.treble_lite_false
        true -> R.string.treble_lite_true
    }
    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.treble_lite_header),
            stringResource(body),
            stringResource(R.string.treble_lite_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable fun TrebleLegacyCard(treble: TrebleResult, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (treble.legacy) {
        false -> R.drawable.treble_legacy_false
        true -> R.drawable.treble_legacy_true
    }
    val tint = when (treble.lite) {
        false -> Green
        true -> Orange
    }
    @StringRes val body = when (treble.lite) {
        false -> R.string.treble_legacy_false
        true -> R.string.treble_legacy_true
    }
    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.treble_legacy_header),
            stringResource(body),
            stringResource(R.string.treble_legacy_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable
fun SARCard(sar: Boolean?, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (sar) {
        null -> R.drawable.unknown
        false -> R.drawable.sar_false
        true -> R.drawable.sar_true
    }
    val tint = when (sar) {
        null -> Error
        false -> Red
        true -> Green
    }
    @StringRes val body = when (sar) {
        null -> R.string.sar_unknown
        false -> R.string.sar_false
        true -> R.string.sar_true
    }

    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.sar_header),
            stringResource(body),
            stringResource(R.string.sar_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable
fun ABCard(ab: Boolean?, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (ab) {
        null -> R.drawable.unknown
        false -> R.drawable.ab_false
        true -> R.drawable.ab_true
    }
    val tint = when (ab) {
        null -> Error
        false -> Red
        true -> Green
    }
    @StringRes val body = when (ab) {
        null -> R.string.ab_unknown
        false -> R.string.ab_false
        true -> R.string.ab_true
    }
    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.ab_header),
            stringResource(body),
            stringResource(R.string.ab_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable
fun DynamicPartitionsCard(dynamic: Boolean?, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (dynamic) {
        null -> R.drawable.unknown
        false -> R.drawable.dynamicpartitions_false
        true -> R.drawable.dynamicpartitions_true
    }
    val tint = when (dynamic) {
        null -> Error
        false -> Red
        true -> Green
    }
    @StringRes val body = when (dynamic) {
        null -> R.string.dynamicpartitions_unknown
        false -> R.string.dynamicpartitions_false
        true -> R.string.dynamicpartitions_true
    }
    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.dynamicpartitions_header),
            stringResource(body),
            stringResource(R.string.dynamicpartitions_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable
fun VABCard(vab: Optional<VABResult?>, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (vab) {
        is Optional.Nothing -> R.drawable.unknown
        is Optional.Value -> when (vab.value) {
            null -> R.drawable.vab_false
            else -> R.drawable.vab_true
        }
    }
    val tint = when (vab) {
        is Optional.Nothing -> Error
        is Optional.Value -> when (vab.value) {
            null -> Red
            else -> Green
        }
    }
    @StringRes val body = when (vab) {
        is Optional.Nothing -> R.string.vab_unknown
        is Optional.Value -> when (vab.value) {
            null -> R.string.vab_false
            else -> R.string.vab_true
        }
    }
    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.vab_header),
            stringResource(body),
            stringResource(R.string.vab_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable
fun VABRCard(vab: VABResult, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (vab.retrofit) {
        null -> R.drawable.unknown
        false -> R.drawable.vabr_false
        true -> R.drawable.vabr_true
    }
    val tint = when (vab.retrofit) {
        null -> Error
        false -> Red
        true -> Green
    }
    @StringRes val body = when (vab.retrofit) {
        null -> R.string.vabr_unknown
        false -> R.string.vabr_false
        true -> R.string.vabr_true
    }
    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.vabr_header),
            stringResource(body),
            stringResource(R.string.vabr_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable
fun VABCCard(vab: VABResult, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (vab.compressed) {
        null -> R.drawable.unknown
        false -> R.drawable.vabc_false
        true -> R.drawable.vabc_true
    }
    val tint = when (vab.compressed) {
        null -> Error
        false -> Red
        true -> Green
    }
    @StringRes val body = when (vab.compressed) {
        null -> R.string.vabc_unknown
        false -> R.string.vabc_false
        true -> R.string.vabc_true
    }
    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.vabc_header),
            stringResource(body),
            stringResource(R.string.vabc_explanation),
            expanded,
            animationParameters
        )
    }
}

@Composable
fun ArchCard(arch: Arch, animationParameters: AnimationParameters, expanded: Boolean, toggleExpanded: () -> Unit) {
    @DrawableRes val icon = when (arch) {
        Arch.ARM64, Arch.X86_64 -> R.drawable.arch_64_bit
        Arch.ARM32, Arch.X86 -> R.drawable.arch_32_bit
        Arch.ARM32_BINDER64, Arch.X86_BINDER64 -> R.drawable.arch_32_64_bit
        is Arch.UNKNOWN -> R.drawable.unknown
    }
    val tint = when (arch) {
        Arch.ARM64, Arch.X86_64 -> Green
        Arch.ARM32, Arch.X86 -> Red
        Arch.ARM32_BINDER64, Arch.X86_BINDER64 -> Blue
        is Arch.UNKNOWN -> Error
    }
    val body = if (arch is Arch.UNKNOWN && (arch.binderVersion != null || arch.cpuName != null)) {
        stringResource(R.string.arch_unknown, arch.cpuName ?: stringResource(R.string.arch_cpu_unknown), arch.binderVersion?.toString() ?: stringResource(R.string.arch_binder_unknown))
    } else {
        stringResource(
            when (arch) {
                Arch.ARM64 -> R.string.arch_arm64
                Arch.ARM32 -> R.string.arch_arm32
                Arch.ARM32_BINDER64 -> R.string.arch_binder64
                Arch.X86_64 -> R.string.arch_x86_64
                Arch.X86_BINDER64 -> R.string.arch_x86_binder64
                Arch.X86 -> R.string.arch_x86
                is Arch.UNKNOWN -> R.string.arch_detection_error
            }
        )
    }
    ClickableIconCard(
        Modifier.semantics {
            if (expanded) {
                collapse { toggleExpanded(); true }
            } else {
                expand { toggleExpanded(); true }
            }
        },
        painterResource(icon),
        tint,
        onClick = toggleExpanded
    ) {
        TextCardContent(
            stringResource(R.string.arch_header),
            body,
            stringResource(R.string.arch_explanation),
            expanded,
            animationParameters
        )
    }
}



private val Red: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Error: Color @Composable get() = Color.Red
private val Orange: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Green: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Blue: Color @Composable get() = MaterialTheme.colorScheme.primary
private const val tag = "MainActivity"