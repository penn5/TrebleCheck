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

import android.util.Log
import androidx.annotation.Keep
import tk.hack5.treblecheck.Mock

object BinderDetector {
    private var loaded = false

    fun getBinderArch(): BinderArch {
        Mock.data?.let { return it.binderArch }

        val binderVersion = try {
            getBinderVersion()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(tag, "Native library unavailable", e)
            null
        }

        return BinderArch(binderVersion)
    }

    @Synchronized
    fun getBinderVersion(): Int {
        if (!loaded) {
            System.loadLibrary("binderdetector")
            loaded = true
        }
        return getBinderVersionNative()
    }

    @Keep
    @JvmName("get_binder_version")
    private external fun getBinderVersionNative(): Int
}

sealed class BinderArch(val bits: Int?) {
    object Binder8 : BinderArch(64)
    object Binder7 : BinderArch(32)

    data class Unknown(val binderVersion: Int?) : BinderArch(null)

    companion object {
        operator fun invoke(binderVersion: Int?) = when (binderVersion) {
            7 -> Binder7
            8 -> Binder8
            else -> Unknown(binderVersion)
        }
    }
}

private const val tag = "BinderDetector"