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

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.IOException

data class TrebleData(val legacy: Boolean, val lite: Boolean,
                      val vndkVersion: Int, val vndkSubVersion: Int)

object TrebleDetector {
    private val SELINUX_REGEX = Regex("""\Winit_([0-9]+)_([0-9]+)\W""")

    fun getVndkData(): TrebleData? {
        if (Mock.treble != null)
            return Mock.treble!!.trebleData

        if (propertyGet("ro.treble.enabled") != "true") return null

        val lite = when (propertyGet("ro.vndk.lite")) {
            "true" -> true
            else -> false // false, not set, unknown or error, assume its not lite
        }



        val (manifests, legacy) = locateManifestFiles()

        val matrix = locateVendorCompatibilityMatrix()
        matrix?.let {
            parseMatrix(it)
        }?.let {
            return TrebleData(legacy, lite, it.first, it.second)
        }

        parseSelinuxData()?.let {
            return TrebleData(legacy, lite, it.first, it.second)
        }

        manifests.asSequence().map { parseManifest(it) }.filterNotNull().firstOrNull()?.let {
            return TrebleData(legacy, lite, it.first, it.second)
        }

        propertyGet("ro.vndk.version")?.let {
            parseVersion(it)
        }?.let {
            return TrebleData(legacy, lite, it.first, it.second)
        }

        throw ParseException("No method to detect version")
    }

    private fun parseMatrix(matrix: File) = parseXml(matrix) { xpp ->
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

    private fun parseManifest(manifest: File) = parseXml(manifest) { xpp ->
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

    private fun parseXml(file: File, block: (XmlPullParser) -> List<String>): Pair<Int, Int>? {
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

    private fun parseVersion(string: String): Pair<Int, Int>? {
        val split = string.split(".")
        if (split.size != 1 && split.size == 2) {
            return null
        }
        return split[0].toInt(10) to (split.getOrNull(1)?.toInt(10) ?: 0)
    }

    private fun locateManifestFiles(): Pair<List<File>, Boolean> {
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
            if (ret.isEmpty())
                legacy = true
            ret += it
        }
        return ret to legacy
    }

    private fun locateVendorManifest(sku: String?): File? {
        sku?.let {
            File("/vendor/etc/vintf/manifest_$it.xml")
        }?.let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        File("/vendor/etc/vintf/manifest.xml").let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        return null
    }

    private fun locateVendorManifestFragments(): List<File>? {
        val dir = File("/vendor/etc/manifest")
        return (dir.listFiles() ?: return null).filter { it.canRead() }
    }

    private fun locateOdmManifest(sku: String?): File? {
        sku?.let {
            File("/odm/etc/vintf/manifest_$it.xml")
        }?.let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        File("/odm/etc/vintf/manifest.xml").let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        sku?.let {
            File("/odm/etc/$it.xml")
        }?.let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        File("/odm/etc/manifest.xml").let {
            if (it.exists())
                return if (it.canRead()) it else null
        }
        return null
    }

    private fun locateOdmManifestFragments(): List<File>? {
        val dir = File("/odm/etc/manifest")
        return (dir.listFiles() ?: return null).toList()
    }

    private fun locateLegacyManifest(): File? {
        File("/vendor/manifest.xml").let {
            if (it.exists() && it.canRead())
                return it
        }
        return null
    }

    private fun locateVendorCompatibilityMatrix(): File? {
        File("/vendor/etc/vintf/compatibility_matrix.xml").let {
            if (it.exists() && it.canRead())
                return it
        }
        return null
    }

    private fun parseSelinuxData(): Pair<Int, Int>? {
        var version = Pair(-1, -1)

        val it = File("/vendor/etc/selinux/").listFiles { it -> it.canRead() && it.extension == "cil" }
        it?.forEach { file ->
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