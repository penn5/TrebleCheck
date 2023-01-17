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

package tk.hack5.treblecheck

import tk.hack5.treblecheck.data.BinderArch
import tk.hack5.treblecheck.data.CPUArch
import tk.hack5.treblecheck.data.TrebleResult
import tk.hack5.treblecheck.data.VABResult

class Mock(
    val ab: Boolean?,
    val binderArch: BinderArch,
    val cpuArch: CPUArch,
    val dynamic: Boolean?,
    val sar: Boolean?,
    val treble: Optional<TrebleResult?>,
    val vab: VABResult?,
    val theme: Int,
) {
    companion object {
        @JvmField // needed for R8 to optimise away properly
        var data: Mock? = null
    }
}
