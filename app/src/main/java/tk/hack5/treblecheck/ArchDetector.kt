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
import android.util.Log

object ArchDetector {
    internal val SUPPORTED_ABIS get() = Build.SUPPORTED_ABIS

    fun getArch(): Arch {
        Mock.data?.let { return it.arch }

        val binderVersion = try {
            BinderDetector.getBinderVersion()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(tag, "Native library unavailable", e)
            null
        }

        val cpu = SUPPORTED_ABIS.firstOrNull()

        Log.v(tag, "binderVersion: $binderVersion, cpu: $cpu")

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

    data class UNKNOWN(val cpuName: String?, val binderVersion: Int?) : Arch(cpuName?.let(::getCpuBits), binderVersion?.let(::getBinderBits))

    companion object {
        operator fun invoke(cpuArch: String?, binderVersion: Int?): Arch =
            when {
                cpuArch == "armeabi-v7a" && binderVersion == 7 -> ARM32
                cpuArch == "arm64-v8a" && binderVersion == 8 -> ARM64
                cpuArch == "armeabi-v7a" && binderVersion == 8 -> ARM32_BINDER64
                cpuArch == "x86_64" && binderVersion == 8 -> X86_64
                cpuArch == "x86" && binderVersion == 7 -> X86
                cpuArch == "x86" && binderVersion == 8 -> X86_BINDER64
                else -> UNKNOWN(cpuArch, binderVersion)
            }

        fun getCpuBits(cpuArch: String) = when (cpuArch) {
            "arm64-v8a" -> 64
            "armeabi-v7a" -> 32
            "x86_64" -> 64
            "x86" -> 32
            else -> null
        }

        fun getBinderBits(binderVersion: Int) = when (binderVersion) {
            7 -> 32
            8 -> 64
            else -> null
        }
    }
}

private const val tag = "ArchDetector"