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
import android.util.Log

object TrebleDetector {
    fun property_get(prop: String): String {
        val c = Class.forName("android.os.SystemProperties")
        val g = c.getMethod("get", String::class.java, String::class.java)
        return g.invoke(null, prop, "") as String
    }

    private const val MANIFEST_PATH = "/vendor/etc/vintf/manifest.xml"
    private const val MANIFEST_PATH_LEGACY = "/vendor/manifest.xml"
    private const val TARGET_ELEMENT = "sepolicy"
    fun getVndkData(): Triple<Boolean /*legacy*/, Int /*VNDK*/, Int /*subversion*/>? {
        if(property_get("ro.treble.enabled") != "true") return null

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

        return Triple(legacy, vndkVersion, vndkSubVersion)
    }
}
