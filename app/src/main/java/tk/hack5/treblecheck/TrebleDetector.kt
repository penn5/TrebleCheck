/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2019 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.IOException

data class TrebleData(val legacy: Boolean, val lite: Boolean,
                      val vndkVersion: Int, val vndkSubVersion: Int)

object TrebleDetector {
    private val SELINUX_REGEX = Regex("""\Winit_([0-9]+)_([0-9]+)\W""")
    internal var root: File? = null

    fun getVndkData(): TrebleData? {
        if (Mock.isMocking)
            return Mock.treble

        val trebleEnabled = propertyGet("ro.treble.enabled")
        Log.v(tag, "trebleEnabled: $trebleEnabled")
        if (trebleEnabled != "true")
            return null

        val lite = propertyGet("ro.vndk.lite") == "true"
        Log.v(tag, "lite: $lite")

        val (manifests, legacy) = locateManifestFiles()
        Log.v(tag, "manifests: ${manifests.joinToString { it.absolutePath }}, legacy: $legacy")

        val matrix = locateVendorCompatibilityMatrix()
        matrix?.let {
            Log.v(tag, "matrix: ${matrix.absolutePath}")
            parseMatrix(it)
        }?.let {
            Log.v(tag, "vendor matrix result: $it")
            return TrebleData(legacy, lite, it.first, it.second)
        }

        parseSelinuxData()?.let {
            Log.v(tag, "selinux result: $it")
            return TrebleData(legacy, lite, it.first, it.second)
        }

        manifests
            .asSequence()
            .map {
                parseManifest(it)
                    .also { res -> Log.v(tag, "matrix ${it.absolutePath}: $res") }
            }
            .filterNotNull()
            .firstOrNull()
            ?.let {
                return TrebleData(legacy, lite, it.first, it.second)
            }

        propertyGet("ro.vndk.version")?.let {
            Log.v(tag, "vndk: $it")
            parseVersion(it)
        }?.let {
            Log.v(tag, "vndk result: $it")
            return TrebleData(legacy, lite, it.first, it.second)
        }

        throw ParseException("No method to detect version")
    }

    internal fun parseMatrix(matrix: File) = parseXml(matrix) { xpp ->
        val versions = mutableListOf<String>()
        val versionBuilder = StringBuilder(2) // 2 is the normal size of the version number, 'xy'

        var inVndkTag = false
        var inVersionTag = false
        var event = xpp.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                if (xpp.name == "vendor-ndk") {
                    inVndkTag = true
                } else if (inVndkTag && xpp.name == "version") {
                    inVersionTag = true
                }
            } else if (event == XmlPullParser.END_TAG) {
                if (inVersionTag) {
                    inVersionTag = false
                    versions += versionBuilder.toString()
                    versionBuilder.clear()
                } else if (inVndkTag) {
                    break
                }
            } else if (event == XmlPullParser.TEXT && inVersionTag) {
                // This is the version number
                versionBuilder.append(xpp.text.trim())
            }
            xpp.next()
            event = xpp.eventType
        }
        versions
    }

    internal fun parseManifest(manifest: File) = parseXml(manifest) { xpp ->
        val versionBuilder = StringBuilder(4) // 4 is the normal size of the version number, 'xy.z'

        var inTargetTag = false
        var event = xpp.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && xpp.name == "sepolicy")
                inTargetTag = true
            else if (event == XmlPullParser.END_TAG && inTargetTag) {
                break
            } else if (event == XmlPullParser.TEXT && inTargetTag) {
                // This is the version number
                versionBuilder.append(xpp.text.trim())
            }
            xpp.next()
            event = xpp.eventType
        }
        if (versionBuilder.isEmpty())
            throw ParseException("versionBuilder is empty")
        listOf(versionBuilder.toString())
    }

    internal fun parseXml(file: File, block: (XmlPullParser) -> List<String>): Pair<Int, Int>? {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val xpp = factory.newPullParser()
        try {
            xpp.setInput(file.inputStream().reader())
        } catch (e: IOException) {
            return null
        }

        val versions = block(xpp)

        return versions
            .mapNotNull { parseVersion(it) }
            .maxWithOrNull { left, right -> left.compareTo(right) }
    }

    internal fun parseVersion(string: String): Pair<Int, Int>? {
        val split = string.split('.').map(String::trim)
        if (split.size != 1 && split.size != 2) {
            return null
        }
        if (split[0].any { it !in '0'..'9' } || split[0].isEmpty()) {
            // ASCII only by design
            return null
        }
        val first = split[0].toInt(10)
        if (split.size == 1 || split[1].any { it !in '0'..'9' } || split[1].isEmpty()) {
            // ASCII only by design
            return first to 0
        }
        val second = split[1].toInt(10)
        return first to second
    }

    internal fun locateManifestFiles(): Pair<List<File>, Boolean> {
        val ret = mutableListOf<File>()
        var legacy = false
        locateVendorManifest(propertyGet("ro.boot.product.vendor.sku"))?.let {
            ret += it
            ret += locateVendorManifestFragments() ?: return@let
        }
        locateOdmManifest(propertyGet("ro.boot.product.hardware.sku"))?.let {
            ret += it
            ret += locateOdmManifestFragments() ?: return@let
        }
        locateLegacyManifest()?.let {
            if (ret.isNotEmpty())
                legacy = true
            ret += it
        }
        return ret to legacy
    }

    internal fun locateVendorManifest(sku: String?): File? {
        sku?.let {
            File(root, "/vendor/etc/vintf/manifest_$it.xml")
        }?.let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        File(root, "/vendor/etc/vintf/manifest.xml").let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        return null
    }

    internal fun locateVendorManifestFragments(): List<File>? {
        val dir = File(root, "/vendor/etc/manifest")
        return (dir.listFiles() ?: return null).filter { it.canRead() }
    }

    internal fun locateOdmManifest(sku: String?): File? {
        sku?.let {
            File(root, "/odm/etc/vintf/manifest_$it.xml")
        }?.let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        File(root, "/odm/etc/vintf/manifest.xml").let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        sku?.let {
            File(root, "/odm/etc/$it.xml")
        }?.let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        File(root, "/odm/etc/manifest.xml").let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        return null
    }

    internal fun locateOdmManifestFragments(): List<File>? {
        val dir = File(root, "/odm/etc/manifest")
        return (dir.listFiles() ?: return null).toList()
    }

    internal fun locateLegacyManifest(): File? {
        File(root, "/vendor/manifest.xml").let {
            if (it.exists() && it.canRead())
                return it
        }
        return null
    }

    internal fun locateVendorCompatibilityMatrix(): File? {
        File(root, "/vendor/etc/vintf/compatibility_matrix.xml").let {
            if (it.exists() && it.canRead())
                return it
        }
        return null
    }

    internal fun parseSelinuxData(): Pair<Int, Int>? {
        // https://android.googlesource.com/platform/system/core/+/refs/tags/android-12.1.0_r2/init/selinux.cpp#281
        val sepolicyVersionFile = File(root, "/vendor/etc/selinux/plat_sepolicy_vers.txt")
        if (sepolicyVersionFile.exists()) {
            return parseVersion(sepolicyVersionFile.bufferedReader().readLine())
        }

        val files = File(root, "/vendor/etc/selinux/").listFiles { it -> it.canRead() && it.extension == "cil" }
        return files?.let { parseSelinuxData(it) }
    }

    internal fun parseSelinuxData(files: Array<File>): Pair<Int, Int>? {
        var version = Pair(-1, -1)

        files.forEach { file ->
            file.bufferedReader().lineSequence().forEach { line ->
                SELINUX_REGEX.findAll(line).forEach { match ->
                    Pair(match.groupValues[1].toInt(), match.groupValues[2].toInt()).let {
                        if (it > version) version = it
                    }
                }
            }
        }
        if (version < Pair(0, 0)) return null
        return version
    }
}

private const val tag = "TrebleDetector"