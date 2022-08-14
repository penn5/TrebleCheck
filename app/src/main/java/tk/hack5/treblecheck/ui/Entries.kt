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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.Arch
import tk.hack5.treblecheck.data.TrebleResult
import tk.hack5.treblecheck.data.VABResult

@Composable
fun requiredImageEntry(fileName: String?): Entry {
    return Entry(
        painterResource(if (fileName == null) R.drawable.filename_unknown else R.drawable.filename_known),
        painterResource(R.drawable.copy),
        if (fileName == null) Error else Green,
        fileName?.let { stringResource(R.string.filename, it) } ?: stringResource(R.string.filename_unknown),
        stringResource(R.string.filename_header),
        stringResource(R.string.filename_explanation),
        fileName?.let {
            ClickAction.CopyText(
                stringResource(R.string.filename_header),
                it
            )
        },
        accented = true
        )

}

@Composable
fun trebleEntry(treble: Optional<TrebleResult?>): Entry {
    val icon = painterResource(
        when (treble) {
            is Optional.Nothing -> R.drawable.unknown
            is Optional.Value -> when {
                treble.value == null -> R.drawable.treble_false
                treble.value.legacy || treble.value.lite -> R.drawable.treble_legacy
                else -> R.drawable.treble_modern
            }
        }
    )
    val tint = when (treble) {
        is Optional.Nothing -> Error
        is Optional.Value -> when {
            treble.value == null -> Red
            treble.value.legacy || treble.value.lite -> Orange
            else -> Green
        }
    }
    val body = stringResource(
        when (treble) {
            is Optional.Nothing -> R.string.treble_unknown
            is Optional.Value -> when (treble.value) {
                null -> R.string.treble_false
                else -> R.string.treble_true
            }
        }
    )
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.treble_header),
            stringResource(R.string.treble_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun trebleVersionEntry(treble: TrebleResult): Entry {
    val icon = painterResource(R.drawable.treble_version)
    val tint = Green
    val body = stringResource(R.string.treble_version, treble.vndkVersion, treble.vndkSubVersion)
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.treble_version_header),
            stringResource(R.string.treble_version_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun trebleLiteEntry(treble: TrebleResult): Entry {
    val icon = painterResource(
        when (treble.lite) {
            false -> R.drawable.treble_lite_false
            true -> R.drawable.treble_lite_true
        }
    )
    val tint = when (treble.lite) {
        false -> Green
        true -> Orange
    }
    val body = stringResource(
        when (treble.lite) {
            false -> R.string.treble_lite_false
            true -> R.string.treble_lite_true
        }
    )
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.treble_lite_header),
            stringResource(R.string.treble_lite_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun trebleLegacyEntry(treble: TrebleResult): Entry {
    val icon = painterResource(
        when (treble.legacy) {
            false -> R.drawable.treble_legacy_false
            true -> R.drawable.treble_legacy_true
        }
    )
    val tint = when (treble.lite) {
        false -> Green
        true -> Orange
    }
    val body = stringResource(
        when (treble.lite) {
            false -> R.string.treble_legacy_false
            true -> R.string.treble_legacy_true
        }
    )
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.treble_legacy_header),
            stringResource(R.string.treble_legacy_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun sarEntry(sar: Boolean?): Entry {
    val icon = painterResource(
        when (sar) {
            null -> R.drawable.unknown
            false -> R.drawable.sar_false
            true -> R.drawable.sar_true
        }
    )
    val tint = when (sar) {
        null -> Error
        false -> Red
        true -> Green
    }
    val body = stringResource(
        when (sar) {
            null -> R.string.sar_unknown
            false -> R.string.sar_false
            true -> R.string.sar_true
        }
    )
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.sar_header),
            stringResource(R.string.sar_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun abEntry(ab: Boolean?): Entry {
    val icon = painterResource(
        when (ab) {
            null -> R.drawable.unknown
            false -> R.drawable.ab_false
            true -> R.drawable.ab_true
        }
    )
    val tint = when (ab) {
        null -> Error
        false -> Red
        true -> Green
    }
    val body = stringResource(
        when (ab) {
            null -> R.string.ab_unknown
            false -> R.string.ab_false
            true -> R.string.ab_true
        }
    )
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.ab_header),
            stringResource(R.string.ab_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun dynamicPartitionsEntry(dynamic: Boolean?): Entry {
    val icon = painterResource(
        when (dynamic) {
            null -> R.drawable.unknown
            false -> R.drawable.dynamicpartitions_false
            true -> R.drawable.dynamicpartitions_true
        }
    )
    val tint = when (dynamic) {
        null -> Error
        false -> Red
        true -> Green
    }
    val body = stringResource(
        when (dynamic) {
            null -> R.string.dynamicpartitions_unknown
            false -> R.string.dynamicpartitions_false
            true -> R.string.dynamicpartitions_true
        }
    )
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.dynamicpartitions_header),
            stringResource(R.string.dynamicpartitions_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun vabEntry(vab: Optional<VABResult?>): Entry {
    val icon = painterResource(
        when (vab) {
            is Optional.Nothing -> R.drawable.unknown
            is Optional.Value -> when (vab.value) {
                null -> R.drawable.vab_false
                else -> R.drawable.vab_true
            }
        }
    )
    val tint = when (vab) {
        is Optional.Nothing -> Error
        is Optional.Value -> when (vab.value) {
            null -> Red
            else -> Green
        }
    }
    val body = stringResource(
        when (vab) {
            is Optional.Nothing -> R.string.vab_unknown
            is Optional.Value -> when (vab.value) {
                null -> R.string.vab_false
                else -> R.string.vab_true
            }
        }
    )
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.vab_header),
            stringResource(R.string.vab_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun vabrEntry(vab: VABResult): Entry {
    val icon = painterResource(
        when (vab.retrofit) {
            null -> R.drawable.unknown
            false -> R.drawable.vabr_false
            true -> R.drawable.vabr_true
        }
    )
    val tint = when (vab.retrofit) {
        null -> Error
        false -> Red
        true -> Green
    }
    val body = stringResource(
        when (vab.retrofit) {
            null -> R.string.vabr_unknown
            false -> R.string.vabr_false
            true -> R.string.vabr_true
        }
    )
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.vabr_header),
            stringResource(R.string.vabr_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun vabcEntry(vab: VABResult): Entry {
    val icon = painterResource(
        when (vab.compressed) {
            null -> R.drawable.unknown
            false -> R.drawable.vabc_false
            true -> R.drawable.vabc_true
        }
    )
    val tint = when (vab.compressed) {
        null -> Error
        false -> Red
        true -> Green
    }
    val body = stringResource(
        when (vab.compressed) {
            null -> R.string.vabc_unknown
            false -> R.string.vabc_false
            true -> R.string.vabc_true
        }
    )
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.sar_header),
            stringResource(R.string.sar_explanation),
            ClickAction.ToggleDetail
        )
}

@Composable
fun archEntry(arch: Arch): Entry {
    val icon = painterResource(
        when (arch) {
            Arch.ARM64, Arch.X86_64 -> R.drawable.arch_64_bit
            Arch.ARM32, Arch.X86 -> R.drawable.arch_32_bit
            Arch.ARM32_BINDER64, Arch.X86_BINDER64 -> R.drawable.arch_32_64_bit
            is Arch.UNKNOWN -> R.drawable.unknown
        }
    )
    val tint = when (arch) {
        Arch.ARM64, Arch.X86_64 -> Green
        Arch.ARM32, Arch.X86 -> Red
        Arch.ARM32_BINDER64, Arch.X86_BINDER64 -> Blue
        is Arch.UNKNOWN -> Error
    }
    val body = if (arch is Arch.UNKNOWN && (arch.binderVersion != null || arch.cpuName != null)) {
        stringResource(
            R.string.arch_unknown,
            arch.cpuName ?: stringResource(R.string.arch_cpu_unknown),
            arch.binderVersion?.toString() ?: stringResource(R.string.arch_binder_unknown)
        )
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
    return Entry(
            icon,
            null,
            tint,
            body,
            stringResource(R.string.sar_header),
            stringResource(R.string.sar_explanation),
            ClickAction.ToggleDetail
        )
}

// TODO
private val Red: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Error: Color @Composable get() = Color.Red
private val Orange: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Green: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Blue: Color @Composable get() = MaterialTheme.colorScheme.primary
