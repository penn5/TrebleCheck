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

import android.util.Log
import androidx.test.core.app.ActivityScenario
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import tools.fastlane.screengrab.DecorViewScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.cleanstatusbar.BluetoothState
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.cleanstatusbar.MobileDataType
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.concurrent.CompletableFuture

@RunWith(Parameterized::class)
class ScreenshotTaker(
    private val ab: Boolean,
    private val arch: Arch,
    private val dynamic: Boolean,
    private val sar: Boolean,
    private val trebleData: TrebleData?,
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
            arrayOf(true, Arch.ARM64, true, true, TrebleData(false, false, 30, 0), 0),
            arrayOf(true, Arch.ARM64, true, true, TrebleData(false, false, 30, 0), 1),
            arrayOf(false, Arch.ARM32_BINDER64, false, false, TrebleData(true, true, 26, 0), 0),
            arrayOf(false, Arch.ARM32_BINDER64, false, false, TrebleData(true, true, 26, 0), 1),
            arrayOf(false, Arch.ARM32_BINDER64, false, false, TrebleData(false, true, 28, 0), 0),
            arrayOf(true, Arch.ARM64, false, true, TrebleData(false, false, 28, 0), 1),
        )

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            Log.e("DemoMode", "on")
            CleanStatusBar()
                .setClock("1000")
                .setMobileNetworkDataType(MobileDataType.FOURG)
                .setBluetoothState(BluetoothState.DISCONNECTED)
                .enable()
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            CleanStatusBar.disable()
        }
    }

    @Test
    fun takeScreenshot() {
        Mock.ab = ab
        Mock.arch = arch
        Mock.dynamic = dynamic
        Mock.sar = sar
        Mock.treble = TrebleDataOrNull(trebleData)
        Mock.theme = theme

        val future = CompletableFuture<ScrollingActivity>()

        val activityScenario = ActivityScenario.launch(ScrollingActivity::class.java)
        activityScenario.onActivity {
            it.window.decorView.post {
                future.complete(it)
            }
        }

        val activity = future.get()
        Screengrab.screenshot(
            "$ab-${arch.cpuBits}-${arch.binderBits}-$dynamic-$sar-" +
                    "${trebleData?.legacy}-${trebleData?.lite}-${trebleData?.vndkVersion}-" +
                    "${trebleData?.vndkSubVersion}",
            DecorViewScreenshotStrategy(activity)
        )

        activityScenario.close()
    }
}