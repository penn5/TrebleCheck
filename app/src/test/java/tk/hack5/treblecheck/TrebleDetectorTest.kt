/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2022 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck

import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import tk.hack5.treblecheck.data.TrebleDetector
import tk.hack5.treblecheck.data.TrebleResult
import java.io.File
import kotlin.reflect.KClass

typealias AnswerScope<T> = MockKAnswerScope<T, T>.(Call) -> T

sealed class Result<T> {
    companion object {
        fun <T> success(result: T) = Success(result)
        inline fun <T, reified E : Throwable> failure() = Failure<T, E>(E::class)
    }

    data class Success<T>(val value: T) : Result<T>()
    data class Failure<T, E : Throwable>(val throwableClass: KClass<E>) : Result<T>()

    fun fold(onSuccess: (T) -> Unit, onFailure: (KClass<out Throwable>) -> Unit) {
        when (this) {
            is Success<T> -> onSuccess(value)
            is Failure<*, *> -> onFailure(throwableClass)
        }
    }
}

@RunWith(Parameterized::class)
class TrebleDetectorTest(
    private val result: Result<TrebleResult?>,
    private val trebleEnabled: String?,
    private val vndkLite: String?,
    private val vendorSku: String?,
    private val odmSku: String?,
    private val vndkVersion: String?,
    private val testName: String
) {
    companion object {
        @Suppress("BooleanLiteralArgument")
        @Parameterized.Parameters
        @JvmStatic
        fun data() = listOf(
            // data-free tests
            arrayOf(Result.success(null), "", "", null, null, null, ""),
            arrayOf(Result.success(null), "false", "", null, null, null, ""),
            arrayOf(Result.failure<Nothing?, ParseException>(), "true", "false", null, null, null, ""),
            // tests with cepheus data
            arrayOf(Result.success(TrebleResult(false, false, 30, 0)), "true", "false", "", "", "30", "vndk1a"),
            arrayOf(Result.success(TrebleResult(false, true, 30, 0)), "true", "true", "", "", null, "vndk1b"),
            arrayOf(Result.success(TrebleResult(false, false, 30, 0)), "true", "false", "", "", null, "vndk1c"),
            arrayOf(Result.success(TrebleResult(false, true, 30, 0)), "true", "true", "", "", null, "vndk1d"),
            arrayOf(Result.success(TrebleResult(false, false, 30, 0)), "true", "false", "", "", null, "vndk1e"),
            // tests with TP1803 data
            arrayOf(Result.success(TrebleResult(false, true, 30, 0)), "true", "true", "", "", "30", "vndk2a"),
            arrayOf(Result.success(TrebleResult(false, false, 30, 0)), "true", "false", "", "", null, "vndk2b"),
            arrayOf(Result.success(TrebleResult(false, true, 30, 0)), "true", "true", "", "", null, "vndk2c"),
            arrayOf(Result.success(TrebleResult(false, false, 30, 0)), "true", "false", "", "", null, "vndk2d"),
            arrayOf(Result.success(TrebleResult(false, true, 30, 0)), "true", "true", "", "", null, "vndk2e"),
        )
    }

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun getVndkData() {
        val function = if (testName.isEmpty()) {
            { testGetVndkData(trebleEnabled, vndkLite, vendorSku, odmSku, { emptyList<File>() to false }, { null }, { null }, vndkVersion) }
        } else {
            extractFiles(testName, vendorSku!!, odmSku!!);
            { testGetVndkData(trebleEnabled, vndkLite, vendorSku, odmSku, { callOriginal() }, { callOriginal() }, { callOriginal() }, vndkVersion) }
        }
        result.fold(
            { expected ->
                assertEquals(expected, function())
            },
            { expectedThrowable ->
                assertThrows(expectedThrowable.java) { function() }
            }
        )
    }


    private fun extractFiles(name: String, vendorSku: String, odmSku: String) {
        temporaryFolder.delete()
        val classLoader = this::class.java.classLoader!!
        val files = mapOf(
            "vendor/etc/vintf/manifest_sku.xml" to "vendor/etc/vintf/manifest_$vendorSku.xml",
            "vendor/etc/vintf/manifest.xml" to null,
            "vendor/etc/manifest/" to ".xml",
            "vendor/manifest.xml" to null,
            "odm/etc/vintf/manifest_sku.xml" to "odm/etc/vintf/manifest_$odmSku.xml",
            "odm/etc/vintf/manifest.xml" to null,
            "odm/etc/sku.xml" to "odm/etc/$odmSku.xml",
            "odm/etc/manifest.xml" to null,
            "odm/etc/manifest/" to ".xml",
            "vendor/etc/vintf/compatibility_matrix.xml" to null,
            "vendor/etc/selinux/" to ".cil",
            "vendor/etc/selinux/plat_sepolicy_vers.txt" to null
        )

        for (file in files.entries) {
            if (file.key.endsWith('/')) {
                var i = 0
                children@while (true) {
                    val sourceStream = classLoader.getResourceAsStream("$name/${file.key}$i") ?: break@children
                    val destFile = temporaryFolder.root.resolve(file.key).resolve(i.toString() + file.value!!)
                    destFile.parentFile!!.mkdirs()
                    destFile.outputStream().use {
                        sourceStream.copyTo(it)
                    }
                    i++
                }
            } else {
                val sourceStream = classLoader.getResourceAsStream("$name/${file.key}") ?: continue
                val destFile = temporaryFolder.root.resolve(file.value ?: file.key)
                destFile.parentFile!!.mkdirs()
                destFile.outputStream().use {
                    sourceStream.copyTo(it)
                }
            }
        }
    }

    private fun testGetVndkData(trebleEnabled: String?, vndkLite: String?, vendorSku: String?, odmSku: String?, manifestFiles: AnswerScope<Pair<List<File>, Boolean>>, vendorCompatibilityMatrix: AnswerScope<File?>, selinuxData: AnswerScope<Pair<Int, Int>?>, vndkVersion: String?): TrebleResult? {
        var ret: TrebleResult? = null
        TrebleDetector.root = temporaryFolder.root
        mockkStatic(::propertyGet.declaringKotlinFile) {
            every { propertyGet("ro.treble.enabled") } returns trebleEnabled
            every { propertyGet("ro.vndk.lite") } returns vndkLite
            every { propertyGet("ro.vndk.version") } returns vndkVersion
            every { propertyGet("ro.boot.product.vendor.sku") } returns vendorSku
            every { propertyGet("ro.boot.product.hardware.sku") } returns odmSku
            mockkObject(TrebleDetector) {
                every { TrebleDetector.locateManifestFiles() } answers manifestFiles
                every { TrebleDetector.locateVendorCompatibilityMatrix() } answers vendorCompatibilityMatrix
                every { TrebleDetector.parseSelinuxData() } answers selinuxData
                ret = TrebleDetector.getVndkData()
            }
        }
        return ret
    }
/*
    @Test
    fun parseMatrix() {
    }

    @Test
    fun parseManifest() {
    }
*/
    @Test
    fun parseVersion() {
        assertEquals(30 to 1, TrebleDetector.parseVersion("30.1"))
        assertEquals(30 to 1, TrebleDetector.parseVersion("\n\n 30.1\u00A0 "))
        assertEquals(30 to 0, TrebleDetector.parseVersion("30"))
        assertEquals(3 to 0, TrebleDetector.parseVersion("3.a"))
        assertEquals(null, TrebleDetector.parseVersion("3b"))
        assertEquals(3 to 0, TrebleDetector.parseVersion("3.\u00A0e"))
        assertEquals(null, TrebleDetector.parseVersion("-3"))
        assertEquals(1 to 0, TrebleDetector.parseVersion("1.+3"))
        assertEquals(null, TrebleDetector.parseVersion("+1.+3"))
        assertEquals(null, TrebleDetector.parseVersion("\u0DEF"))
    }
/*
    @Test
    fun parseSelinuxData() {
    }
*/
}