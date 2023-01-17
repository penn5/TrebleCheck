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

package tk.hack5.treblecheck.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.BinderArch
import tk.hack5.treblecheck.data.CPUArch
import tk.hack5.treblecheck.data.TrebleResult
import tk.hack5.treblecheck.data.VABResult

data class Detail(val icon: Painter, val iconTint: Color, val title: String, val subtitle: String, val body: String)


@Composable
fun trebleDetail(treble: Optional<TrebleResult?>): Detail {
    val icon = painterResource(
        when (treble) {
            is Optional.Nothing -> R.drawable.unknown
            is Optional.Value -> when (treble.value) {
                null -> R.drawable.treble_false
                else -> R.drawable.treble_true
            }
        }
    )
    val tint = when (treble) {
        is Optional.Nothing -> Error
        is Optional.Value -> when (treble.value) {
            null -> Red
            else -> Green
        }
    }
    val subtitle = stringResource(
        when (treble) {
            is Optional.Nothing -> R.string.treble_unknown
            is Optional.Value -> when (treble.value) {
                null -> R.string.treble_false
                else -> R.string.treble_true
            }
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.treble_title),
        subtitle,
        stringResource(R.string.treble_explanation)
    )
}

@Composable
fun trebleVersionEntry(treble: TrebleResult): Detail {
    val icon = painterResource(R.drawable.treble_version)
    val tint = Green
    val subtitle = stringResource(R.string.treble_version_subtitle, treble.vndkVersion, treble.vndkSubVersion)
    return Detail(
        icon,
        tint,
        stringResource(R.string.treble_version_title),
        subtitle,
        stringResource(R.string.treble_version_explanation)
    )
}

@Composable
fun trebleLiteEntry(treble: TrebleResult): Detail {
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
    val subtitle = stringResource(
        when (treble.lite) {
            false -> R.string.treble_lite_false
            true -> R.string.treble_lite_true
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.treble_lite_title),
        subtitle,
        stringResource(R.string.treble_lite_explanation)
    )
}

@Composable
fun trebleLegacyEntry(treble: TrebleResult): Detail {
    val icon = painterResource(
        when (treble.legacy) {
            false -> R.drawable.treble_legacy_false
            true -> R.drawable.treble_legacy_true
        }
    )
    val tint = when (treble.legacy) {
        false -> Green
        true -> Orange
    }
    val subtitle = stringResource(
        when (treble.legacy) {
            false -> R.string.treble_legacy_false
            true -> R.string.treble_legacy_true
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.treble_legacy_title),
        subtitle,
        stringResource(R.string.treble_legacy_explanation)
    )
}

@Composable
fun sarEntry(sar: Boolean?): Detail {
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
    val subtitle = stringResource(
        when (sar) {
            null -> R.string.sar_unknown
            false -> R.string.sar_false
            true -> R.string.sar_true
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.sar_title),
        subtitle,
        stringResource(R.string.sar_explanation)
    )
}

@Composable
fun abEntry(ab: Boolean?): Detail {
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
    val subtitle = stringResource(
        when (ab) {
            null -> R.string.ab_unknown
            false -> R.string.ab_false
            true -> R.string.ab_true
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.ab_title),
        subtitle,
        stringResource(R.string.ab_explanation)
    )
}


@Composable
fun dynamicPartitionsEntry(dynamic: Boolean?): Detail {
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
    val subtitle = stringResource(
        when (dynamic) {
            null -> R.string.dynamicpartitions_unknown
            false -> R.string.dynamicpartitions_false
            true -> R.string.dynamicpartitions_true
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.dynamicpartitions_title),
        subtitle,
        stringResource(R.string.dynamicpartitions_explanation)
    )
}

@Composable
fun vabEntry(vab: Optional<VABResult?>): Detail {
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
    val subtitle = stringResource(
        when (vab) {
            is Optional.Nothing -> R.string.vab_unknown
            is Optional.Value -> when (vab.value) {
                null -> R.string.vab_false
                else -> R.string.vab_true
            }
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.vab_title),
        subtitle,
        stringResource(R.string.vab_explanation)
    )
}

@Composable
fun vabrEntry(vab: VABResult): Detail {
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
    val subtitle = stringResource(
        when (vab.retrofit) {
            null -> R.string.vabr_unknown
            false -> R.string.vabr_false
            true -> R.string.vabr_true
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.vabr_title),
        subtitle,
        stringResource(R.string.vabr_explanation)
    )
}

@Composable
fun vabcEntry(vab: VABResult): Detail {
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
    val subtitle = stringResource(
        when (vab.compressed) {
            null -> R.string.vabc_unknown
            false -> R.string.vabc_false
            true -> R.string.vabc_true
        }
    )
    return Detail(
        icon,
        tint,
        stringResource(R.string.vabc_title),
        subtitle,
        stringResource(R.string.vabc_explanation)
    )
}


@Composable
fun cpuArchEntry(cpuArch: CPUArch): Detail {
    val icon = painterResource(
        when (cpuArch) {
            CPUArch.ARM64, CPUArch.X86_64 -> R.drawable.binder_arch_64_bit
            CPUArch.ARM32, CPUArch.X86 -> R.drawable.cpu_arch_32_bit
            is CPUArch.Unknown -> R.drawable.unknown
        }
    )
    val tint = when (cpuArch) {
        CPUArch.ARM64, CPUArch.X86_64 -> Green
        CPUArch.ARM32, CPUArch.X86 -> Red
        is CPUArch.Unknown -> Error
    }
    val subtitle = if (cpuArch is CPUArch.Unknown) {
        cpuArch.archName?.let { stringResource(R.string.cpu_arch_unknown_name, it) } ?: stringResource(R.string.cpu_arch_unknown)
    } else {
        stringResource(
            when (cpuArch) {
                CPUArch.ARM64 -> R.string.cpu_arch_arm64
                CPUArch.ARM32 -> R.string.cpu_arch_arm32
                CPUArch.X86_64 -> R.string.cpu_arch_x86_64
                CPUArch.X86 -> R.string.cpu_arch_x86
                else -> { error("Unreachable") }
            }
        )
    }
    return Detail(
        icon,
        tint,
        stringResource(R.string.cpu_arch_title),
        subtitle,
        stringResource(R.string.cpu_arch_explanation)
    )
}

@Composable
fun binderArchEntry(binderArch: BinderArch): Detail {
    val icon = painterResource(
        when (binderArch) {
            BinderArch.Binder8 -> R.drawable.binder_arch_64_bit
            BinderArch.Binder7 -> R.drawable.binder_arch_32_bit
            is BinderArch.Unknown -> R.drawable.unknown
        }
    )
    val tint = when (binderArch) {
        BinderArch.Binder8 -> Green
        BinderArch.Binder7 -> Red
        is BinderArch.Unknown -> Error
    }
    val subtitle = if (binderArch is BinderArch.Unknown) {
        binderArch.binderVersion?.let { stringResource(R.string.binder_arch_unknown_version, it) } ?: stringResource(R.string.binder_arch_unknown)
    } else {
        stringResource(
            when (binderArch) {
                BinderArch.Binder8 -> R.string.binder_arch_64_bit
                BinderArch.Binder7 -> R.string.binder_arch_32_bit
                else -> { error("Unreachable") }
            }
        )
    }
    return Detail(
        icon,
        tint,
        stringResource(R.string.binder_arch_title),
        subtitle,
        stringResource(R.string.binder_arch_explanation)
    )
}


private val Red: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Error: Color @Composable get() = Color.Red
private val Orange: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Green: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Blue: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Neutral: Color @Composable get() = Green
