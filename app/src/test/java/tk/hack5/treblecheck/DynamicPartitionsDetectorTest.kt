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

import io.mockk.declaringKotlinFile
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*

import org.junit.Test
import tk.hack5.treblecheck.data.DynamicPartitionsDetector

class DynamicPartitionsDetectorTest {

    @Test
    fun isDynamic() {
        testIsDynamic(false, "")
        testIsDynamic(true, "true")
        testIsDynamic(false, "false")
        testIsDynamic(false, "weird-value 1.40$")
        testIsDynamic(null, null)
    }

    private fun testIsDynamic(expected: Boolean?, slotSuffix: String?) = mockkStatic(::propertyGet.declaringKotlinFile) {
        every { propertyGet("ro.boot.dynamic_partitions") } returns slotSuffix
        assertEquals(expected, DynamicPartitionsDetector.isDynamic())
    }
}