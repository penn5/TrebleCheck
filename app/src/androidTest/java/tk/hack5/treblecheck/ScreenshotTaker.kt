/*
 *     Treble Info
 *     Copyright (C) 2023 Hackintosh Five
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

package tk.hack5.treblecheck

import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import tk.hack5.treblecheck.data.BinderArch
import tk.hack5.treblecheck.data.CPUArch
import tk.hack5.treblecheck.data.TrebleResult
import tk.hack5.treblecheck.ui.MainActivity
import tools.fastlane.screengrab.DecorViewScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(Parameterized::class)
class ScreenshotTaker(
    private val ab: Boolean,
    private val binderArch: BinderArch,
    private val cpuArch: CPUArch,
    private val dynamic: Boolean,
    private val sar: Boolean,
    private val treble: TrebleResult?,
    private val theme: Boolean,
    private val tab: Int,
) {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    companion object {
        @ClassRule
        @JvmField
        val localeTestRule = LocaleTestRule()

        @Suppress("BooleanLiteralArgument") // too much data, use an IDE.
        @Parameterized.Parameters
        @JvmStatic
        fun data() = listOf(
            arrayOf(false, BinderArch.Binder7, CPUArch.ARM32, false, false, null, false, 0),
            arrayOf(false, BinderArch.Binder7, CPUArch.ARM32, false, false, null, false, 1),
            arrayOf(true, BinderArch.Binder8, CPUArch.ARM64, true, true, TrebleResult(false, false, 33, 0), false, 0),
            arrayOf(true, BinderArch.Binder8, CPUArch.ARM64, true, true, TrebleResult(false, false, 33, 0), false, 1),
            arrayOf(false, BinderArch.Binder8, CPUArch.ARM32, false, false, TrebleResult(true, true, 26, 0), false, 0),
            arrayOf(false, BinderArch.Binder8, CPUArch.ARM32, false, false, TrebleResult(true, true, 26, 0), false, 1),
            arrayOf(true, BinderArch.Binder8, CPUArch.ARM64, false, true, TrebleResult(false, false, 28, 0), false, 2),
            arrayOf(true, BinderArch.Binder8, CPUArch.ARM64, false, true, TrebleResult(false, false, 28, 0), true, 3),
        )
    }

    private fun takeIdleScreenshot(i: Int) {
        runBlocking(Dispatchers.IO) {
            composeTestRule.waitForIdle()
        }

        Screengrab.screenshot(
            "$ab-${binderArch.bits}-${cpuArch.bits}-$dynamic-$sar-" +
                    "${treble?.legacy}-${treble?.lite}-${treble?.vndkVersion}-" +
                    "${treble?.vndkSubVersion}-${theme}-${i}",
            DecorViewScreenshotStrategy(composeTestRule.activity)
        )
    }

    @Test
    fun takeScreenshots() {
        Mock.data = Mock(ab, binderArch, cpuArch, dynamic, sar, Optional.Value(treble), theme)
        Snapshot.sendApplyNotifications()
        composeTestRule.activityRule.scenario.recreate()

        runBlocking(Dispatchers.Main) {
            composeTestRule.activity.setTurnScreenOn(true)
            composeTestRule.activity.window.addFlags(FLAG_KEEP_SCREEN_ON)
        }

        val tabs = composeTestRule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
        tabs.get(tab).performClick()
        takeIdleScreenshot(tab)
    }
}
