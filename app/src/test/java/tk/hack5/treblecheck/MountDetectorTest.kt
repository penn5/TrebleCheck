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
import org.junit.Assert.assertEquals
import org.junit.Test
import tk.hack5.treblecheck.data.Mount
import tk.hack5.treblecheck.data.MountDetector
import java.io.*

class MountDetectorTest {
    @Test
    fun checkMounts() {
        assertEquals(
            listOf(
                Mount(
                    "/dev/block/dm-0",
                    "/",
                    "ext4",
                    listOf("ro", "seclabel", "relatime", "discard"),
                    0,
                    0
                )
            ),
            testCheckMounts(
                ConstantAnswer("/dev/block/dm-0 / ext4 ro,seclabel,relatime,discard 0 0"),
                false
            )
        )
        assertEquals(
            listOf(
                Mount(
                    "none",
                    "/dev/cpuset",
                    "cgroup",
                    listOf("rw", "nosuid", "nodev", "noexec", "relatime", "cpuset", "noprefix", "release_agent=/sbin/cpuset_release_agent"),
                    0,
                    0
                ),
                Mount(
                    "sysfs",
                    "/sys",
                    "sysfs",
                    listOf("rw", "seclabel", "relatime"),
                    0,
                    0
                )
            ),
            testCheckMounts(
                ConstantAnswer("none /dev/cpuset cgroup rw,nosuid,nodev,noexec,relatime,cpuset,noprefix,release_agent=/sbin/cpuset_release_agent 0 0\n\nsysfs /sys sysfs rw,seclabel,relatime 0 0\n"),
                false
            )
        )
    }

    private fun testCheckMounts(mountsFile: Answer<String>, result: Boolean): List<Mount>? {
        var ret: List<Mount>? = null
        mockkObject(MountDetector) {
            every { MountDetector.getMountsStream() } answers { call -> BufferedReader(StringReader(mountsFile.answer(call))) }
            assertEquals(result, MountDetector.checkMounts {
                ret = it
                result
            })
        }
        return ret
    }

    @Test
    fun isSAR() {
        testIsSAR(true, ThrowingAnswer(IllegalStateException()), "true", "false")
        testIsSAR(true, ThrowingAnswer(IllegalStateException()), "false", "true")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts1.txt")), "false", "")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts2.txt")), "", "")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts3.txt")), "", "")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts4.txt")), "", "")
        testIsSAR(false, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts5.txt")), "", "")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts6.txt")), "", "")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts7.txt")), "", "")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts8.txt")), "", "")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts9.txt")), "", "")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts10.txt")), "", "")
        testIsSAR(true, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts11.txt")), "", "")
        testIsSAR(false, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts12.txt")), "", "")
        testIsSAR(false, ConstantAnswer(javaClass.classLoader!!.getResourceAsStream("mounts13.txt")), "", "")
    }

    private fun testIsSAR(expected: Boolean, mountsFile: Answer<InputStream>, sar: String?, dynamicPartitions: String?) {
        mockkObject(MountDetector) {
            every { MountDetector.getMountsStream() } answers { call -> BufferedReader(InputStreamReader(mountsFile.answer(call))).also { it.readLine() } }
            mockkStatic(::propertyGet.declaringKotlinFile) {
                every { propertyGet("ro.build.system_root_image") } returns sar
                every { propertyGet("ro.boot.dynamic_partitions") } returns dynamicPartitions
                assertEquals(expected, MountDetector.isSAR())
            }
        }
    }
}