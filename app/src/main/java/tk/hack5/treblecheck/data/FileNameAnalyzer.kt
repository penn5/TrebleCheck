/*
 *     Treble Info
 *     Copyright (C) 2019 Hackintosh Five
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
    private fun StringBuilder.appendArch(arch: Arch) = append(
        when (arch) {
            Arch.ARM64 -> "arm64"
            Arch.ARM32_BINDER64 -> "arm32_binder64"
            Arch.ARM32 -> "arm32"
            Arch.X86_64 -> "x86_64"
            Arch.X86_BINDER64 -> "x86_binder64"
            Arch.X86 -> "x86"
            is Arch.UNKNOWN -> "???"
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

    fun getFileName(treble: TrebleResult?, arch: Arch, sar: Boolean?): String = StringBuilder("system-").run {
        appendArch(arch)
        append('-')
        appendSar(sar)
        appendVndkLite(sar, treble)
        append(".img.xz")
        toString()
    }
}