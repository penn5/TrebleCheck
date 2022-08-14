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

import tk.hack5.treblecheck.data.Arch
import tk.hack5.treblecheck.data.TrebleResult

class Mock(
    val ab: Boolean?,
    val arch: Arch,
    val dynamic: Boolean?,
    val sar: Boolean?,
    val treble: Optional<TrebleResult?>,
    val theme: Int,
) {
    companion object {
        @JvmField // needed for R8 to optimise away properly
        var data: Mock? = null
    }
}
