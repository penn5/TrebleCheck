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
import androidx.test.core.app.ActivityScenario
import org.junit.ClassRule
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
import java.util.concurrent.CompletableFuture

@RunWith(Parameterized::class)
class ScreenshotTaker(
    private val ab: Boolean,
    private val binderArch: BinderArch,
    private val cpuArch: CPUArch,
    private val dynamic: Boolean,
    private val sar: Boolean,
    private val treble: TrebleResult?,
    private val theme: Int
) {
    companion object {
        @ClassRule
        @JvmField
        val localeTestRule = LocaleTestRule()

        @Suppress("BooleanLiteralArgument") // too much data, use an IDE.
        @Parameterized.Parameters
        @JvmStatic
        fun data() = listOf(
            arrayOf(false, BinderArch.Binder7, CPUArch.ARM32, false, false, null, 0),
            arrayOf(false, BinderArch.Binder7, CPUArch.ARM32, false, false, null, 1),
            arrayOf(true, BinderArch.Binder8, CPUArch.ARM64, true, true, TrebleResult(false, false, 30, 0), 0),
            arrayOf(true, BinderArch.Binder8, CPUArch.ARM64, true, true, TrebleResult(false, false, 30, 0), 1),
            arrayOf(false, BinderArch.Binder8, CPUArch.ARM32, false, false, TrebleResult(true, true, 26, 0), 0),
            arrayOf(false, BinderArch.Binder8, CPUArch.ARM32, false, false, TrebleResult(true, true, 26, 0), 1),
            arrayOf(false, BinderArch.Binder8, CPUArch.ARM32, false, false, TrebleResult(false, true, 28, 0), 0),
            arrayOf(true, BinderArch.Binder8, CPUArch.ARM64, false, true, TrebleResult(false, false, 28, 0), 1),
        )
    }

    @Test
    fun takeScreenshot() {
        Mock.data = Mock(ab, binderArch, cpuArch, dynamic, sar, Optional.Value(treble), theme)

        val future = CompletableFuture<Void>()
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        activityScenario.onActivity {
            it.setTurnScreenOn(true)
            it.window.addFlags(FLAG_KEEP_SCREEN_ON)
            // TODO change tabs
            try {
                Screengrab.screenshot(
                    "$ab-${binderArch.bits}-${cpuArch.bits}-$dynamic-$sar-" +
                            "${treble?.legacy}-${treble?.lite}-${treble?.vndkVersion}-" +
                            "${treble?.vndkSubVersion}-${theme}",
                    DecorViewScreenshotStrategy(it)
                )
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }

            future.complete(null)
        }

        future.get()
        activityScenario.close()
    }
}