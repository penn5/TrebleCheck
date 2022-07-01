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

import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Test

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