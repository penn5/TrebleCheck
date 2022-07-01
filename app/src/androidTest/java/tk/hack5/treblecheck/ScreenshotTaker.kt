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

import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.test.core.app.ActivityScenario
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
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