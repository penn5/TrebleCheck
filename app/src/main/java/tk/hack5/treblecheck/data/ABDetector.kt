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

import android.util.Log
import tk.hack5.treblecheck.Mock
import tk.hack5.treblecheck.propertyGet

object ABDetector {
    fun checkAB(): Boolean? {
        Mock.data?.let { return it.ab }

        val slotSuffix = propertyGet("ro.boot.slot_suffix")
        Log.v(tag, "slotSuffix: $slotSuffix")
        return slotSuffix?.isNotEmpty()
    }
}

private const val tag = "ABDetector"