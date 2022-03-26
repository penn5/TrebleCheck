/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2020 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck

import android.util.Log

object DynamicPartitionsDetector {
    fun isDynamic(): Boolean? {
        if (Mock.isMocking)
            return Mock.dynamic
        val dynamicPartitions = propertyGet("ro.boot.dynamic_partitions")

        Log.v(tag, "dynamicPartitions: $dynamicPartitions")
        return (dynamicPartitions ?: return null) == "true"
    }
}

private const val tag = "DynamicPartitionsDetect"