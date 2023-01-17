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

package tk.hack5.treblecheck.data

object FileNameAnalyzer {
    private fun StringBuilder.appendArch(binderArch: BinderArch, cpuArch: CPUArch) = append(
        when (binderArch to cpuArch) {
            BinderArch.Binder8 to CPUArch.ARM64 -> "arm64"
            BinderArch.Binder8 to CPUArch.ARM32 -> "arm32_binder64"
            BinderArch.Binder8 to CPUArch.X86_64 -> "x86_64"
            BinderArch.Binder8 to CPUArch.X86 -> "x86"
            BinderArch.Binder7 to CPUArch.ARM32 -> "arm32"
            BinderArch.Binder7 to CPUArch.X86 -> "x86_binder64"
            else -> "???"
        }
    )

    private fun StringBuilder.appendSar(sar: Boolean?) = append(
        when (sar) {
            null -> "???"
            false -> "aonly"
            true -> "ab"
        }
    )

    private fun StringBuilder.appendVndkLite(sar: Boolean?, treble: TrebleResult?) =
        if (sar != false && (treble?.lite == true || treble?.legacy == true)) {
            append("-vndklite")
        } else {
            this
        }

    fun getFileName(treble: TrebleResult?, binderArch: BinderArch, cpuArch: CPUArch, sar: Boolean?): String = StringBuilder("system-").run {
        appendArch(binderArch, cpuArch)
        append('-')
        appendSar(sar)
        appendVndkLite(sar, treble)
        append(".img.xz")
        toString()
    }
}