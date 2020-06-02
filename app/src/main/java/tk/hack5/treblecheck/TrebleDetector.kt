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

data class TrebleData(val legacy: Boolean, val lite: Boolean,
                      val vndkVersion: Int, val vndkSubVersion: Int)

object TrebleDetector {
    private const val MANIFEST_PATH = "/vendor/etc/vintf/manifest.xml"
    private const val MANIFEST_PATH_LEGACY = "/vendor/manifest.xml"
    private const val TARGET_ELEMENT = "sepolicy"
    private const val SELINUX_PATH = "/vendor/etc/selinux/"
    private const val SELINUX_EXT = "cil"
    private val SELINUX_REGEX = Regex("""\Winit_([0-9]+)_([0-9]+)\W""")

    fun getVndkData(): TrebleData? {
        if (propertyGet("ro.treble.enabled") != "true") return null

        val lite = when (propertyGet("ro.vndk.lite")) {
            "true" -> true
            "false" -> false
            else -> false // notset, assume its not lite
        }

        var manifest = File(MANIFEST_PATH)
        val legacy = !manifest.exists() // this will pass even when unreadable
        if (!manifest.exists())
            manifest = File(MANIFEST_PATH_LEGACY)
        if (!manifest.exists() || !manifest.canRead()) {
            // Try to parse props or cils instead, as this can happen when permissions are denied
            return parseSelinuxData(legacy, lite) ?: TrebleData(
                legacy,
                lite,
                propertyGet("ro.vndk.version").toIntOrNull() ?:
                propertyGet("ro.vendor.build.version.sdk").toIntOrNull() ?:
                0, // the props above were added in sdk 28
                0
            )
        }

        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val xpp = factory.newPullParser()
        xpp.setInput(manifest.inputStream().reader())

        val versionBuilder = StringBuilder(4) // 4 is the normal size of the version number, 'xy.z'

        var inTargetTag = false
        var event = xpp.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && xpp.name == TARGET_ELEMENT)
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
        val version = versionBuilder.toString()
        val versions = version.split(".")
        if (versions.size != 2 && versions.size != 1)
            throw ParseException("versions.size != 2,1: $version")
        val vndkVersion = versions[0].toInt(10)
        val vndkSubVersion = versions.getOrNull(1)?.toInt(10) ?: 0

        return TrebleData(legacy, lite, vndkVersion, vndkSubVersion)
    }

    private fun parseSelinuxData(legacy: Boolean, lite: Boolean): TrebleData? {
        var version = Pair(-1, -1)
        @Suppress("RedundantLambdaArrow") // KT-37567
        val it = File(SELINUX_PATH).listFiles { it -> it.canRead() && it.extension == SELINUX_EXT }
        it!!.forEach { file -> // TODO investigate why !! is required (i suspect a language bug)
            file.bufferedReader().lineSequence().forEach { line ->
                SELINUX_REGEX.findAll(line).forEach { match ->
                    Pair(match.groupValues[1].toInt(), match.groupValues[2].toInt()).let {
                        if (it > version) version = it
                    }
                }
            }
        }
        if (version < Pair(0, 0)) return null
        return TrebleData(legacy, lite, version.first, version.second)
    }
}