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

import org.junit.Assert.*

import org.junit.Test
import tk.hack5.treblecheck.data.*

class FileNameAnalyzerTest {

    @Suppress("BooleanLiteralArgument")
    @Test
    fun getFileName() {
        testGetFileName("system-arm64-aonly.img.xz", TrebleResult(false, false, 31, 0), BinderArch.Binder8, CPUArch.ARM64, false)
        testGetFileName("system-arm64-aonly.img.xz", TrebleResult(false, true, 31, 0), BinderArch.Binder8, CPUArch.ARM64, false)
        testGetFileName("system-arm32_binder64-ab-vndklite.img.xz", TrebleResult(false, true, 31, 0), BinderArch.Binder8, CPUArch.ARM32, true)
        testGetFileName("system-arm32_binder64-ab-vndklite.img.xz", TrebleResult(true, false, 31, 0), BinderArch.Binder8, CPUArch.ARM32, true)
        testGetFileName("system-arm32-???-vndklite.img.xz", TrebleResult(true, false, 31, 0), BinderArch.Binder7, CPUArch.ARM32, null)
        testGetFileName("system-???-???.img.xz", null, BinderArch.Unknown(null), CPUArch.Unknown(null), null)
    }

    private fun testGetFileName(expected: String, treble: TrebleResult?, binderArch: BinderArch, cpuArch: CPUArch, sar: Boolean?) {
        assertEquals(expected, FileNameAnalyzer.getFileName(treble, binderArch, cpuArch, sar))
    }
}