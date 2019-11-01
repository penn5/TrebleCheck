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

import android.annotation.SuppressLint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File

data class TrebleData(val legacy: Boolean, val lite: Boolean,
                      val vndkVersion: Int, val vndkSubVersion: Int)

object TrebleDetector {
    private const val MANIFEST_PATH = "/vendor/etc/vintf/manifest.xml"
    private const val MANIFEST_PATH_LEGACY = "/vendor/manifest.xml"
    private const val TARGET_ELEMENT = "sepolicy"
    fun getVndkData(): TrebleData? {
        if (propertyGet("ro.treble.enabled") != "true") return null

        val lite = when (propertyGet("ro.vndk.lite")) {
            "true" -> true
            "false" -> false
            else -> false // notset, assume its not lite
        }

        var legacy = false
        var manifest = File(MANIFEST_PATH)
        if (!manifest.isFile) {
            manifest = File(MANIFEST_PATH_LEGACY)
            legacy = true
        }
        // Return if there is no manifest
        if (!manifest.isFile)
            return null

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
            else if (event == XmlPullParser.END_TAG)
                inTargetTag = false
            else if (event == XmlPullParser.TEXT && inTargetTag) {
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
        if (versions.size != 2)
            throw ParseException("versions.size != 2: $version")
        val vndkVersion = versions[0].toInt(10)
        val vndkSubVersion = versions[1].toInt(10)

        return TrebleData(legacy, lite, vndkVersion, vndkSubVersion)
    }
}
