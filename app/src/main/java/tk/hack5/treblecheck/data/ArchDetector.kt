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

import android.os.Build
import android.util.Log
import tk.hack5.treblecheck.Mock

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

    data class UNKNOWN(val cpuName: String?, val binderVersion: Int?) : Arch(cpuName?.let(Companion::getCpuBits), binderVersion?.let(
        Companion::getBinderBits
    ))

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