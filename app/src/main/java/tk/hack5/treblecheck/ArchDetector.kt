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
        if (Mock.arch != null)
            return Mock.arch!!
        val binderVersion = try {
            BinderDetector.getBinderVersion()
        } catch (e: UnsatisfiedLinkError) {
            return Arch.UNKNOWN(null, null)
        }

        val cpu = Build.SUPPORTED_ABIS.first()
        return Arch(cpu, binderVersion)
    }
}

@Suppress("ClassName")
sealed class Arch(val cpuBits: Int?, val binderBits: Int? = cpuBits) {
    object ARM64 : Arch(64)
    object ARM32_BINDER64 : Arch(64, 32)
    object ARM32 : Arch(32)
    object X86_64 : Arch(64)
    object X86_BINDER64 : Arch(64, 32)
    object X86 : Arch(32)

    class UNKNOWN(cpuBits: Int?, binderBits: Int?) : Arch(cpuBits, binderBits)

    companion object {
        operator fun invoke(cpuArch: String, binderVersion: Int): Arch =
            when {
                cpuArch == "arm64-v8a" && binderVersion == 7 -> UNKNOWN(64, 32)
                cpuArch == "armeabi-v7a" && binderVersion == 7 -> ARM32
                cpuArch == "arm64-v8a" && binderVersion == 8 -> ARM64
                cpuArch == "armeabi-v7a" && binderVersion == 8 -> ARM32_BINDER64
                cpuArch == "x86_64" && binderVersion == 7 -> UNKNOWN(64, 32)
                cpuArch == "x86_64" && binderVersion == 8 -> X86_64
                cpuArch == "x86" && binderVersion == 7 -> X86
                cpuArch == "x86" && binderVersion == 8 -> X86_BINDER64
                else -> UNKNOWN(null, null)
            }
    }
}