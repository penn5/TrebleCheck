/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2022 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection


class ScaledPaddingValues private constructor(private val base: PaddingValues, private val multiplier: Float) : PaddingValues {
    override fun calculateBottomPadding() = base.calculateBottomPadding() * multiplier

    override fun calculateLeftPadding(layoutDirection: LayoutDirection) = base.calculateLeftPadding(layoutDirection) * multiplier

    override fun calculateRightPadding(layoutDirection: LayoutDirection) = base.calculateRightPadding(layoutDirection) * multiplier

    override fun calculateTopPadding() = base.calculateTopPadding() * multiplier

    companion object {
        operator fun PaddingValues.times(multiplier: Float): PaddingValues {
            return if (this is ScaledPaddingValues) {
                ScaledPaddingValues(this.base, this.multiplier * multiplier)
            } else {
                ScaledPaddingValues(this, multiplier)
            }
        }
    }
}