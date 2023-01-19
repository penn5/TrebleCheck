/*
 *     Treble Info
 *     Copyright (C) 2022-2023 Hackintosh Five
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
// SPDX-License-Identifier: GPL-3.0-or-later

package tk.hack5.treblecheck

import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Test
import tk.hack5.treblecheck.data.ArchDetector
import tk.hack5.treblecheck.data.CPUArch

class ArchDetectorTest {
    @Test
    fun getArch() {
        assertEquals(CPUArch.ARM32, testGetArch(arrayOf("armeabi-v7a")))
        assertEquals(CPUArch.ARM64, testGetArch(arrayOf("arm64-v8a")))
        assertEquals(CPUArch.X86_64, testGetArch(arrayOf("x86_64")))
        assertEquals(CPUArch.X86, testGetArch(arrayOf("x86")))

        assertEquals(CPUArch.Unknown("fancy new cpu"), testGetArch(arrayOf("fancy new cpu", "x86_64", "x86")))
    }

    private fun testGetArch(supportedAbis: Array<String>): CPUArch {
        lateinit var ret: CPUArch

        mockkObject(ArchDetector) {
            every { ArchDetector.SUPPORTED_ABIS } returns supportedAbis
            ret = ArchDetector.getCPUArch()
        }

        return ret
    }
}
