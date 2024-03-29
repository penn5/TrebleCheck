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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import tk.hack5.treblecheck.Optional
import tk.hack5.treblecheck.R
import tk.hack5.treblecheck.data.BinderArch
import tk.hack5.treblecheck.data.CPUArch
import tk.hack5.treblecheck.data.TrebleResult

data class Detail(val icon: Painter, val title: String, val subtitle: String, val body: String)


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
        stringResource(R.string.treble_title),
        subtitle,
        stringResource(R.string.treble_explanation)
    )
}

@Composable
fun trebleVersionEntry(treble: TrebleResult): Detail {
    val icon = painterResource(R.drawable.treble_version)
    val subtitle = "${treble.vndkVersion}.${treble.vndkSubVersion}"
    return Detail(
        icon,
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
    val subtitle = stringResource(
        when (treble.lite) {
            false -> R.string.treble_lite_false
            true -> R.string.treble_lite_true
        }
    )
    return Detail(
        icon,
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
    val subtitle = stringResource(
        when (treble.legacy) {
            false -> R.string.treble_legacy_false
            true -> R.string.treble_legacy_true
        }
    )
    return Detail(
        icon,
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
    val subtitle = stringResource(
        when (sar) {
            null -> R.string.sar_unknown
            false -> R.string.sar_false
            true -> R.string.sar_true
        }
    )
    return Detail(
        icon,
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
    val subtitle = stringResource(
        when (ab) {
            null -> R.string.ab_unknown
            false -> R.string.ab_false
            true -> R.string.ab_true
        }
    )
    return Detail(
        icon,
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
    val subtitle = stringResource(
        when (dynamic) {
            null -> R.string.dynamicpartitions_unknown
            false -> R.string.dynamicpartitions_false
            true -> R.string.dynamicpartitions_true
        }
    )
    return Detail(
        icon,
        stringResource(R.string.dynamicpartitions_title),
        subtitle,
        stringResource(R.string.dynamicpartitions_explanation)
    )
}


@Composable
fun cpuArchEntry(cpuArch: CPUArch): Detail {
    val icon = painterResource(
        when (cpuArch) {
            CPUArch.ARM64, CPUArch.X86_64 -> R.drawable.cpu_arch_64_bit
            CPUArch.ARM32, CPUArch.X86 -> R.drawable.cpu_arch_32_bit
            is CPUArch.Unknown -> R.drawable.unknown
        }
    )
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
        stringResource(R.string.binder_arch_title),
        subtitle,
        stringResource(R.string.binder_arch_explanation)
    )
}
