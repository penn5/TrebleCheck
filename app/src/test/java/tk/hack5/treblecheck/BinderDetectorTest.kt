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

import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Test
import tk.hack5.treblecheck.data.BinderArch
import tk.hack5.treblecheck.data.BinderDetector

class BinderDetectorTest {
    @Test
    fun getBinder() {
        assertEquals(BinderArch.Unknown(6), testGetArch(6))
        assertEquals(BinderArch.Unknown(null), testGetArch(null))
        assertEquals(BinderArch.Binder7, testGetArch(7))
        assertEquals(BinderArch.Binder8, testGetArch(8))
        assertEquals(BinderArch.Unknown(9), testGetArch(9))
    }

    private fun testGetArch(binderVersion: Int?): BinderArch {
        lateinit var ret: BinderArch
        mockkObject(BinderDetector) {
            if (binderVersion != null) {
                every { BinderDetector.getBinderVersion() } returns binderVersion
            } else {
                every { BinderDetector.getBinderVersion() } throws UnsatisfiedLinkError()
            }

            ret = BinderDetector.getBinderArch()
        }

        return ret
    }
}
