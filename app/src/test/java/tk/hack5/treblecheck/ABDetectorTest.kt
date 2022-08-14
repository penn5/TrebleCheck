/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2022 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
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