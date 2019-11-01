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

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.*
import org.sufficientlysecure.donations.DonationsFragment

class ScrollingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, theme)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        fab.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://t.me/hackintosh5"))
            startActivity(browserIntent)
        }
        treble_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.treble_header)
        sar_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.system_as_root_header)
        arch_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.arch_header)
        ab_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.ab_header)
        theme_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.theme_header)
        license_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.license_header)
        support_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.support_header)
        license_card.findViewById<TextView>(R.id.content).text = resources.getText(R.string.license)

        license_card.findViewById<TextView>(R.id.content)
            .setLinkTextColor(license_card.findViewById<TextView>(R.id.content).textColors)
        Linkify.addLinks(license_card.findViewById<TextView>(R.id.content), Linkify.WEB_URLS)
        support_card.findViewById<TextView>(R.id.content).text = resources.getText(R.string.support)
        updateThemeText(false)
        theme_card.setOnClickListener { updateThemeText(true) }
        license_card.findViewById<ImageView>(R.id.image).setImageDrawable(resources.getDrawable(R.drawable.foss_license, theme))
        support_card.findViewById<ImageView>(R.id.image).setImageDrawable(resources.getDrawable(R.drawable.support, theme))
        theme_card.findViewById<ImageView>(R.id.image).setImageDrawable(resources.getDrawable(R.drawable.theme, theme))
        donate_card.findViewById<ImageView>(R.id.image).setImageDrawable(resources.getDrawable(R.drawable.donate, theme))


        var trebleFail = false
        val treble = try {
            TrebleDetector.getVndkData()
        } catch (e: ParseException) {
            Log.e(tag, "Treble checks failed", e)
            trebleFail = true
            null
        }
        val arch = ArchDetector.getArch()
        val sar = try {
            MountDetector.isSAR()
        } catch (e: ParseException) {
            Log.e(tag, "SAR checks failed", e)
            null
        }
        val ab = ABDetector.checkAB()

        var trebleText = resources.getText(if (trebleFail) R.string.treble_unknown else when (treble?.legacy) {
            null -> R.string.treble_false
            true -> R.string.treble_legacy
            false -> R.string.treble_modern
        }) as String
        treble?.let {
            trebleText = trebleText.format(it.vndkVersion, it.vndkSubVersion, if (it.lite) "Lite " else "")
        }
        val archText = resources.getText(
            when (arch) {
                Arch.ARM64 -> R.string.arch_arm64
                Arch.ARM32 -> R.string.arch_arm32
                Arch.ARM32BINDER64 -> R.string.arch_binder64
                Arch.UNKNOWN -> R.string.arch_unknown
            }
        )
        val sarText = resources.getText(
            when (sar) {
                true -> R.string.sar_true
                false -> R.string.sar_false
                null -> R.string.sar_unknown
            }
        )
        val abText = resources.getText(if (ab) R.string.ab_true else R.string.ab_false)

        val trebleImage = resources.getDrawable(if (trebleFail) R.drawable.unknown else
            when (treble?.legacy) {
                null -> R.drawable.treble_false
                true -> R.drawable.treble_legacy
                false -> if (!treble.lite) R.drawable.treble_modern else R.drawable.treble_legacy
            }, theme
        )
        val archImage = resources.getDrawable(
            when (arch) {
                Arch.ARM64 -> R.drawable.arch_64_bit
                Arch.ARM32 -> R.drawable.arch_32_bit
                Arch.ARM32BINDER64 -> R.drawable.arch_32_64_bit
                Arch.UNKNOWN -> R.drawable.unknown
            }, theme
        )
        val sarImage = resources.getDrawable(
            when (sar) {
                true -> R.drawable.sar_true
                false -> R.drawable.sar_false
                null -> R.drawable.unknown
            }, theme)
        val abImage = resources.getDrawable(if (ab) R.drawable.ab_true else R.drawable.ab_false, theme)

        val trebleTint = ColorStateList.valueOf(
            ResourcesCompat.getColor(
                resources, if (trebleFail) R.color.unknown else
                    when (treble?.legacy) {
                        null -> R.color.treble_false
                        true -> R.color.treble_legacy
                        false -> if (!treble.lite) R.color.treble_modern else R.color.treble_legacy
                    }, theme
            )
        )
        val archTint = ColorStateList.valueOf(
            ResourcesCompat.getColor(
                resources, when (arch) {
                    Arch.ARM64 -> R.color.arch_64_bit
                    Arch.ARM32 -> R.color.arch_32_bit
                    Arch.ARM32BINDER64 -> R.color.arch_32_64_bit
                    Arch.UNKNOWN -> R.color.unknown
                }, theme
            )
        )
        val sarTint = ColorStateList.valueOf(
            ResourcesCompat.getColor(
                resources,
                when (sar) {
                    true -> R.color.sar_true
                    false -> R.color.sar_false
                    null -> R.color.unknown
                }, theme
            )
        )
        val abTint = ColorStateList.valueOf(ResourcesCompat.getColor(
            resources, if (ab) R.color.ab_true else R.color.ab_false, theme))

        treble_card.findViewById<TextView>(R.id.content).text = trebleText
        arch_card.findViewById<TextView>(R.id.content).text = archText
        sar_card.findViewById<TextView>(R.id.content).text = sarText
        ab_card.findViewById<TextView>(R.id.content).text = abText

        treble_card.findViewById<ImageView>(R.id.image).setImageDrawable(trebleImage)
        arch_card.findViewById<ImageView>(R.id.image).setImageDrawable(archImage)
        sar_card.findViewById<ImageView>(R.id.image).setImageDrawable(sarImage)
        ab_card.findViewById<ImageView>(R.id.image).setImageDrawable(abImage)

        ImageViewCompat.setImageTintList(treble_card.findViewById(R.id.image), trebleTint)
        ImageViewCompat.setImageTintList(arch_card.findViewById(R.id.image), archTint)
        ImageViewCompat.setImageTintList(sar_card.findViewById(R.id.image), sarTint)
        ImageViewCompat.setImageTintList(ab_card.findViewById(R.id.image), abTint)

        val playStoreMode = getPlayStoreMode()

        donate_card.findViewById<TextView>(R.id.header).visibility = View.GONE
        donate_card.findViewById<TextView>(R.id.content).visibility = View.GONE
        val container = donate_card.findViewById<FrameLayout>(R.id.frame)
        container.visibility = View.VISIBLE
        container.id = View.generateViewId()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val allModes = BuildConfig.DONATIONS_DEBUG
        val donateFragment = DonationsFragment.newInstance(BuildConfig.DONATIONS_DEBUG, playStoreMode || allModes, BuildConfig.GPLAY_PUBK, BuildConfig.GPLAY_KEYS, BuildConfig.GPLAY_VALS, !playStoreMode || allModes, BuildConfig.PAYPAL_EMAIL, BuildConfig.PAYPAL_CURRENCY, BuildConfig.PAYPAL_DESCRIPTION, false, null)
        fragmentTransaction.replace(container.id, donateFragment, "donationsFragment")
        fragmentTransaction.commit()
    }

    private fun getPlayStoreMode(): Boolean {
        return try {
            packageManager.getApplicationInfo("com.android.vending", 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun updateThemeText(change: Boolean) {
        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        var current = sharedPrefs.getInt("daynight", 2)

        if (change)
            current = (current + 1) % 3
            with (sharedPrefs.edit()) {
                putInt("daynight", current)
                apply()
            }

        theme_card.findViewById<TextView>(R.id.content).text = resources.getText(
            when (current) {
                0 -> R.string.theme_day
                1 -> R.string.theme_night
                else -> R.string.theme_auto
            }
        )
        AppCompatDelegate.setDefaultNightMode(
            when (current) {
                0 -> AppCompatDelegate.MODE_NIGHT_NO
                1 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            }
        )
    }

    companion object {
        private const val tag = "TrebleInfo"
    }
}
