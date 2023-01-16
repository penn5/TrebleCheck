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

package tk.hack5.treblecheck

import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Test
import tk.hack5.treblecheck.data.Arch
import tk.hack5.treblecheck.data.ArchDetector
import tk.hack5.treblecheck.data.BinderDetector

class ArchDetectorTest {
    @Test
    fun getArch() {
        assertEquals(Arch.ARM32, testGetArch(arrayOf("armeabi-v7a"), 7))
        assertEquals(Arch.ARM64, testGetArch(arrayOf("arm64-v8a"), 8))
        assertEquals(Arch.ARM32_BINDER64, testGetArch(arrayOf("armeabi-v7a"), 8))
        assertEquals(Arch.X86_64, testGetArch(arrayOf("x86_64"), 8))
        assertEquals(Arch.X86, testGetArch(arrayOf("x86"), 7))
        assertEquals(Arch.X86_BINDER64, testGetArch(arrayOf("x86"), 8))

        assertEquals(Arch.UNKNOWN("arm64-v8a", 7), testGetArch(arrayOf("arm64-v8a"), 7))
        assertEquals(Arch.UNKNOWN("x86_64", 7), testGetArch(arrayOf("x86_64", "x86"), 7))

        assertEquals(Arch.UNKNOWN("fancy new cpu", 9), testGetArch(arrayOf("fancy new cpu", "x86_64", "x86"), 9))
    }

    private fun testGetArch(supportedAbis: Array<String>, binderVersion: Int?): Arch {
        lateinit var ret: Arch

        mockkObject(BinderDetector) {
            if (binderVersion != null) {
                every { BinderDetector.getBinderVersion() } returns binderVersion
            } else {
                every { BinderDetector.getBinderVersion() } throws UnsatisfiedLinkError()
            }

            mockkObject(ArchDetector) {
                every { ArchDetector.SUPPORTED_ABIS } returns supportedAbis
                ret = ArchDetector.getArch()
            }

            /*mockField(Build::class, "SUPPORTED_ABIS", supportedAbis) {
                ret = ArchDetector.getArch()
            }*/
        }
        return ret
    }
}