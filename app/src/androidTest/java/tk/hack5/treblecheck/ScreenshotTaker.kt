/*
 *     Treble Info
 *     Copyright (C) 2019 Hackintosh Five
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
import tk.hack5.treblecheck.data.Arch
import tk.hack5.treblecheck.data.TrebleResult
import tools.fastlane.screengrab.DecorViewScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.concurrent.CompletableFuture

@RunWith(Parameterized::class)
class ScreenshotTaker(
    private val ab: Boolean,
    private val arch: Arch,
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
            arrayOf(false, Arch.ARM32, false, false, null, 0),
            arrayOf(false, Arch.ARM32, false, false, null, 1),
            arrayOf(true, Arch.ARM64, true, true, TrebleResult(false, false, 30, 0), 0),
            arrayOf(true, Arch.ARM64, true, true, TrebleResult(false, false, 30, 0), 1),
            arrayOf(false, Arch.ARM32_BINDER64, false, false, TrebleResult(true, true, 26, 0), 0),
            arrayOf(false, Arch.ARM32_BINDER64, false, false, TrebleResult(true, true, 26, 0), 1),
            arrayOf(false, Arch.ARM32_BINDER64, false, false, TrebleResult(false, true, 28, 0), 0),
            arrayOf(true, Arch.ARM64, false, true, TrebleResult(false, false, 28, 0), 1),
        )
    }

    @Test
    fun takeScreenshot() {
        Mock.data = Mock(ab, arch, dynamic, sar, Optional.Value(treble), theme)

        val future = CompletableFuture<Void>()
        val activityScenario = ActivityScenario.launch(ScrollingActivity::class.java)
        activityScenario.onActivity {
            it.setTurnScreenOn(true)
            it.window.addFlags(FLAG_KEEP_SCREEN_ON)
            try {
                Screengrab.screenshot(
                    "$ab-${arch.cpuBits}-${arch.binderBits}-$dynamic-$sar-" +
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