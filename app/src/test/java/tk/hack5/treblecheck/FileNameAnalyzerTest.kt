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

import org.junit.Assert.*

import org.junit.Test
import tk.hack5.treblecheck.data.Arch
import tk.hack5.treblecheck.data.FileNameAnalyzer
import tk.hack5.treblecheck.data.TrebleResult

class FileNameAnalyzerTest {

    @Suppress("BooleanLiteralArgument")
    @Test
    fun getFileName() {
        testGetFileName("system-arm64-aonly.img.xz", TrebleResult(false, false, 31, 0), Arch.ARM64, false)
        testGetFileName("system-arm64-aonly.img.xz", TrebleResult(false, true, 31, 0), Arch.ARM64, false)
        testGetFileName("system-arm32_binder64-ab-vndklite.img.xz", TrebleResult(false, true, 31, 0), Arch.ARM32_BINDER64, true)
        testGetFileName("system-arm32_binder64-ab-vndklite.img.xz", TrebleResult(true, false, 31, 0), Arch.ARM32_BINDER64, true)
        testGetFileName("system-arm32-???-vndklite.img.xz", TrebleResult(true, false, 31, 0), Arch.ARM32, null)
        testGetFileName("system-???-???.img.xz", null, Arch.UNKNOWN(null, null), null)
    }

    private fun testGetFileName(expected: String, treble: TrebleResult?, arch: Arch, sar: Boolean?) {
        assertEquals(expected, FileNameAnalyzer.getFileName(treble, arch, sar))
    }
}