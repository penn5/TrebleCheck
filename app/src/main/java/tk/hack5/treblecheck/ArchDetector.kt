/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2019 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck

import android.os.Build

object ArchDetector {
    fun getArch(): Arch {
        val binderArch = when (BinderDetector.get_binder_version()) {
            7 -> Arch.ARM32
            8 -> Arch.ARM64
            else -> Arch.UNKNOWN
        }

        val cpu = Build.SUPPORTED_ABIS
        var cpuArch = Arch.UNKNOWN
        if (cpu.any { it == "arm64-v8a" }) cpuArch = Arch.ARM64
        else if (cpu.any { it == "armeabi-v7a" }) cpuArch = Arch.ARM32

        if (cpuArch == Arch.UNKNOWN || binderArch == Arch.UNKNOWN)
            return Arch.UNKNOWN
        if (cpuArch == Arch.ARM32 && binderArch == Arch.ARM64) {
            return Arch.ARM32BINDER64
        }
        return cpuArch
    }
}

enum class Arch {
    ARM64,
    ARM32BINDER64,
    ARM32,
    UNKNOWN
}