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

import android.os.Build
import android.util.Log
import tk.hack5.treblecheck.Mock

object ArchDetector {

    fun getCPUArch(): CPUArch {
        Mock.data?.let { return it.cpuArch }

        val cpuArch = Build.SUPPORTED_ABIS.firstOrNull()

        Log.v(tag, "cpuArch: $cpuArch")

        return CPUArch(cpuArch)
    }
}

sealed class CPUArch(val bits: Int?) {
    object ARM64 : CPUArch(64)
    object ARM32 : CPUArch(32)
    object X86_64 : CPUArch(64)
    object X86 : CPUArch(32)

    data class Unknown(val archName: String?) : CPUArch(null)

    companion object {
        operator fun invoke(archName: String?) = when (archName) {
            "arm64-v8a" -> ARM64
            "armeabi-v7a" -> ARM32
            "x86_64" -> X86_64
            "x86" -> X86
            else -> Unknown(archName)
        }
    }
}

private const val tag = "ArchDetector"