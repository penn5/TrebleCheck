/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2021 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck

class Mock(
    val ab: Boolean?,
    val arch: Arch,
    val dynamic: Boolean?,
    val sar: Boolean?,
    val treble: Optional<TrebleData?>,
    val theme: Int,
) {
    companion object {
        @JvmField // needed for R8 to optimise away properly
        var data: Mock? = null
    }
}

sealed class Optional<in T> {
    class Value<T>(val value: T) : Optional<T>()
    object Nothing : Optional<Any?>()
}

fun <T>Optional<T>.get() = when (this) {
    is Optional.Value -> value
    else -> throw NullPointerException("Optional is empty")
}