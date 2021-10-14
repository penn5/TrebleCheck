/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2020 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck

class FileNameAnalyzer(private val trebleData: TrebleData?, private val arch: Arch, private val sar: Boolean?) {
    private fun appendArch(sb: StringBuilder) {
        sb.append(
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
    }

    private fun appendSar(sb: StringBuilder) {
        sb.append(
            when (sar) {
                null -> "???"
                false -> "aonly"
                true -> "ab"
            }
        )
    }

    private fun appendVndkLite(sb: StringBuilder) {
        if (sar == true && (trebleData?.lite == true || trebleData?.legacy == true)) {
            sb.append("-vndklite")
        }
    }

    fun getFileName(): String {
        val sb = StringBuilder("system-")
        appendArch(sb)
        sb.append("-")
        appendSar(sb)
        appendVndkLite(sb)
        sb.append(".img.xz")
        return sb.toString()
    }
}