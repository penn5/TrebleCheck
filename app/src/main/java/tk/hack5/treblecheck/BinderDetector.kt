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

object BinderDetector {
    private var loaded = false

    @Synchronized
    fun getBinderVersion(): Int {
        if (Mock.binderVersion != null)
            return Mock.binderVersion!!
        if (!loaded) {
            System.loadLibrary("binderdetector")
            loaded = true
        }
        return getBinderVersionNative()
    }

    @JvmName("get_binder_version")
    private external fun getBinderVersionNative(): Int
}