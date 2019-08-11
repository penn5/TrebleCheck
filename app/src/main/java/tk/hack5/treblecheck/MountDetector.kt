/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2019 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck

import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

object MountDetector {
    private const val MOUNTS_PATH = "/proc/mounts"
    fun isSAR(): Boolean {
        val mountsStream: BufferedReader
        try {
            mountsStream = File(MOUNTS_PATH).inputStream().bufferedReader()
        } catch (e: FileNotFoundException) {
            throw ParseException("The host is not running Linux or procfs is broken", e)
        } catch (e: IOException) {
            throw ParseException("Failed to open and read /proc/mounts", e)
        }
        val lines = ArrayList<Mount>()
        for (line in mountsStream.lineSequence()) {
            lines.add(parseLine(line))
        }
        return lines.any { it.device == "/dev/root" }
    }

    private fun parseLine(line: String): Mount {
        val fields = line.split(" ")
        if (fields.size != 6) throw ParseException("Incorrect /proc/mounts format")
        return Mount(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5])
    }
}

data class Mount(
    val device: String, val mountpoint: String,
    val type: String, val flags: List<String>,
    val dummy0: Int, val dummy1: Int
) {
    constructor(device: String, mountpoint: String, type: String, flags: String, dummy0: String, dummy1: String) :
            this(device, mountpoint, type, flags.split(","), dummy0.toInt(), dummy1.toInt())
}

class ParseException(reason: String, exception: Exception? = null) : RuntimeException(reason, exception)