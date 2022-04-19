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

object ABDetector {
    fun checkAB(): Boolean? {
        Mock.data?.let { return it.ab }

        val slotSuffix = propertyGet("ro.boot.slot_suffix")
        Log.v(tag, "slotSuffix: $slotSuffix")
        return slotSuffix?.isNotEmpty()
    }
}

private const val tag = "ABDetector"