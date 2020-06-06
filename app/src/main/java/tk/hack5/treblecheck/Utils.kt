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

import android.annotation.SuppressLint


@SuppressLint("PrivateApi") // Oh well.
fun propertyGet(prop: String): String {
    return try {
        val c = Class.forName("android.os.SystemProperties")
        val g = c.getMethod("get", String::class.java, String::class.java)
        g.invoke(null, prop, "") as String
    } catch (e: Exception) {
        ""
    }
}

operator fun <A1 : Comparable<A2>, B1 : Comparable<B2>, A2, B2>Pair<A1, B1>.compareTo(other: Pair<A2, B2>): Int =
    if (first == other.first)
        second.compareTo(other.second)
    else
        first.compareTo(other.first)