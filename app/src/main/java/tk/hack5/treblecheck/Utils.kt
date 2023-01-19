/*
 *     Treble Info
 *     Copyright (C) 2020-2023 Hackintosh Five
 *     Copyright (C) 2019 Pierre-Hugues Husson
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
// SPDX-License-Identifier: GPL-3.0-or-later

package tk.hack5.treblecheck

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import tk.hack5.treblecheck.data.TrebleResult


@SuppressLint("PrivateApi") // Oh well.
fun propertyGet(prop: String): String? {
    return try {
        val c = Class.forName("android.os.SystemProperties")
        val g = c.getMethod("get", String::class.java, String::class.java)
        g.invoke(null, prop, "") as String
    } catch (e: Exception) {
        Log.e(tag, "Failed to get property $prop", e)
        null
    }
}

operator fun <A1 : Comparable<A2>, B1 : Comparable<B2>, A2, B2>Pair<A1, B1>.compareTo(other: Pair<A2, B2>): Int =
    if (first == other.first)
        second.compareTo(other.second)
    else
        first.compareTo(other.first)

sealed class Optional<out T> {
    class Value<T>(val value: T) : Optional<T>()
    object Nothing : Optional<kotlin.Nothing>()
}

inline fun <reified T>Optional<T>.get() = when (this) {
    is Optional.Value -> value
    else -> throw NullPointerException("Optional is empty")
}

inline fun <reified T>Optional<T>.getOrNull() = getOrElse(null)

inline fun <reified T>Optional<T>.getOrElse(otherwise: T) = when (this) {
    is Optional.Value -> value
    Optional.Nothing -> otherwise
}

val Optional<TrebleResult?>.supported: Boolean?
    inline get() = when (this) {
        is Optional.Value -> value != null
        Optional.Nothing -> null
    }

fun PaddingValues.horizontal(): PaddingValues = HorizontalPaddingValues(this)
fun PaddingValues.vertical(): PaddingValues = PaddingValues(0.dp, calculateTopPadding(), 0.dp, calculateBottomPadding())

private class HorizontalPaddingValues(private val paddingValues: PaddingValues) : PaddingValues by paddingValues {
    override fun calculateTopPadding() = 0.dp
    override fun calculateBottomPadding() = 0.dp
}

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = SumPaddingValues(this, other)

class SumPaddingValues(private val left: PaddingValues, private val right: PaddingValues) : PaddingValues {
    override fun calculateTopPadding() = left.calculateTopPadding() + right.calculateTopPadding()
    override fun calculateLeftPadding(layoutDirection: LayoutDirection) = left.calculateLeftPadding(layoutDirection) + right.calculateLeftPadding(layoutDirection)
    override fun calculateBottomPadding() = left.calculateBottomPadding() + right.calculateBottomPadding()
    override fun calculateRightPadding(layoutDirection: LayoutDirection) = left.calculateRightPadding(layoutDirection) + right.calculateRightPadding(layoutDirection)
}

private const val tag = "Utils"