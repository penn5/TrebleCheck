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

import io.mockk.*
import org.junit.Assert.*

import org.junit.Test
import tk.hack5.treblecheck.data.ABDetector

class ABDetectorTest {
    @Test
    fun checkAB() {
        assertEquals(false, testCheckAB(""))
        assertEquals(true, testCheckAB("a"))
        assertEquals(true, testCheckAB("b"))
        assertEquals(true, testCheckAB("weird-suffix 1.40$"))
        assertEquals(null, testCheckAB(null))
    }

    private fun testCheckAB(slotSuffix: String?): Boolean? {
        var ret: Boolean? = null
        mockkStatic(::propertyGet.declaringKotlinFile) {
            every { propertyGet("ro.boot.slot_suffix") } returns slotSuffix
            ret = ABDetector.checkAB()
        }
        return ret
    }
}