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

import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

object MountDetector {
    private const val MOUNTS_PATH = "/proc/mounts"
    internal fun getMountsStream(): BufferedReader = File(MOUNTS_PATH).inputStream().bufferedReader()

    internal fun checkMounts(check: (List<Mount>) -> Boolean): Boolean {
        val mountsStream: BufferedReader
        try {
            mountsStream = getMountsStream()
        } catch (e: FileNotFoundException) {
            throw ParseException("The host is not running Linux or procfs is broken", e)
        } catch (e: IOException) {
            throw ParseException("Failed to open and read /proc/mounts", e)
        }
        val lines = mountsStream.lineSequence().mapNotNull {
            parseLine(it)
        }
        return check(lines.toList())
    }

    fun isSAR(): Boolean? {
        Mock.data?.let { return it.sar }

        val systemRootImage = propertyGet("ro.build.system_root_image")
        val dynamicPartitions = propertyGet("ro.boot.dynamic_partitions")
        Log.v(tag, "systemRootImage: $systemRootImage, dynamicPartitions: $dynamicPartitions")

        return when {
            dynamicPartitions == "true" -> true
            systemRootImage == "true" -> true
            else -> checkMounts { lines ->
                val rootMounted = lines.any { it.device == "/dev/root" && it.mountpoint == "/" }
                val noSystemPartition = lines.none { it.mountpoint == "/system" && it.type != "tmpfs" && it.device != "none" }
                val systemRoot = lines.any { it.mountpoint == "/system_root" && it.type != "tmpfs" }
                Log.v(tag, "rootMounted: $rootMounted, noSystemPartition: $noSystemPartition, systemRoot: $systemRoot")
                rootMounted || noSystemPartition || systemRoot
            }
        }
    }

    internal fun parseLine(line: String): Mount? {
        if (line.isBlank() || line.startsWith(' ')) {
            return null
        }
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
    constructor(device: String, mountpoint: String, type: String, flags: String, dump: String, fsckOrder: String) :
            this(device, mountpoint, type, flags.split(","), dump.toInt(), fsckOrder.toInt())
}


private const val tag = "MountDetector"