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
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.ImageView
import android.widget.TextView
import android.text.util.Linkify
import android.view.View
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.*
import org.sufficientlysecure.donations.DonationsFragment

class ScrollingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        fab.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://t.me/hackintosh5"))
            startActivity(browserIntent)
        }
        val playStoreMode = getPlayStoreMode()

        treble_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.treble_header)
        sar_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.system_as_root_header)
        arch_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.arch_header)

        license_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.license_header)
        support_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.support_header)
        theme_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.theme_header)
        license_card.findViewById<TextView>(R.id.content).text = resources.getText(R.string.license)
        Linkify.addLinks(license_card.findViewById<TextView>(R.id.content), Linkify.WEB_URLS)
        license_card.findViewById<TextView>(R.id.content).setLinkTextColor(license_card.findViewById<TextView>(R.id.content).getTextColors())
        support_card.findViewById<TextView>(R.id.content).text = resources.getText(R.string.support)
        updateThemeText(false)
        theme_card.setOnClickListener { updateThemeText(true) }
        license_card.findViewById<ImageView>(R.id.image).setImageDrawable(resources.getDrawable(R.drawable.foss_license, theme))
        support_card.findViewById<ImageView>(R.id.image).setImageDrawable(resources.getDrawable(R.drawable.support, theme))
        theme_card.findViewById<ImageView>(R.id.image).setImageDrawable(resources.getDrawable(R.drawable.theme, theme))

        val treble = TrebleDetector.getVndkData()
        val arch = ArchDetector.getArch()
        val sar = MountDetector.isSAR()

        var trebleText = resources.getText(when (treble?.first) {
            null -> R.string.treble_false
            true -> R.string.treble_legacy
            false -> R.string.treble_modern
        }) as String
        treble?.let {
            trebleText = trebleText.format(it.second, it.third)
        }
        val archText = resources.getText(
            when (arch) {
                Arch.ARM64 -> R.string.arch_arm64
                Arch.ARM32 -> R.string.arch_arm32
                Arch.ARM32BINDER64 -> R.string.arch_binder64
                Arch.UNKNOWN -> R.string.arch_unknown
            }
        )
        val sarText = resources.getText(if (sar) R.string.sar_true else R.string.sar_false)

        val trebleImage = resources.getDrawable(
            when (treble?.first) {
                null -> R.drawable.treble_false
                true -> R.drawable.treble_legacy
                false -> R.drawable.treble_modern
            }, theme
        )
        val archImage = resources.getDrawable(
            when (arch) {
                Arch.ARM64 -> R.drawable.arch_64_bit
                Arch.ARM32 -> R.drawable.arch_32_bit
                Arch.ARM32BINDER64 -> R.drawable.arch_32_64_bit
                Arch.UNKNOWN -> R.drawable.arch_unknown
            }, theme
        )
        val sarImage = resources.getDrawable(if (sar) R.drawable.sar_true else R.drawable.sar_false, theme)

        val trebleTint = ColorStateList.valueOf(
            ResourcesCompat.getColor(
                resources, when (treble?.first) {
                    null -> R.color.treble_false
                    true -> R.color.treble_legacy
                    false -> R.color.treble_modern
                }, theme
            )
        )
        val archTint = ColorStateList.valueOf(
            ResourcesCompat.getColor(
                resources, when (arch) {
                    Arch.ARM64 -> R.color.arch_64_bit
                    Arch.ARM32 -> R.color.arch_32_bit
                    Arch.ARM32BINDER64 -> R.color.arch_32_64_bit
                    Arch.UNKNOWN -> R.color.arch_unknown
                }, theme
            )
        )
        val sarTint = ColorStateList.valueOf(
            ResourcesCompat.getColor(
                resources,
                if (sar) R.color.sar_true else R.color.sar_false, theme
            )
        )
        treble_card.findViewById<TextView>(R.id.content).text = trebleText
        arch_card.findViewById<TextView>(R.id.content).text = archText
        sar_card.findViewById<TextView>(R.id.content).text = sarText

        treble_card.findViewById<ImageView>(R.id.image).setImageDrawable(trebleImage)
        arch_card.findViewById<ImageView>(R.id.image).setImageDrawable(archImage)
        sar_card.findViewById<ImageView>(R.id.image).setImageDrawable(sarImage)

        ImageViewCompat.setImageTintList(treble_card.findViewById(R.id.image), trebleTint)
        ImageViewCompat.setImageTintList(arch_card.findViewById(R.id.image), archTint)
        ImageViewCompat.setImageTintList(sar_card.findViewById(R.id.image), sarTint)

        if (!playStoreMode) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val allModes = BuildConfig.BUILD_TYPE == "debug" || BuildConfig.DONATIONS_DEBUG
            val donateFragment = DonationsFragment.newInstance(BuildConfig.DONATIONS_DEBUG, playStoreMode || allModes, BuildConfig.GPLAY_PUBK, BuildConfig.GPLAY_KEYS, BuildConfig.GPLAY_VALS, !playStoreMode || allModes, BuildConfig.PAYPAL_EMAIL, BuildConfig.PAYPAL_CURRENCY, BuildConfig.PAYPAL_DESCRIPTION, false, null, null, false, null)
            fragmentTransaction.replace(R.id.donate_container, donateFragment, "donationsFragment")
            fragmentTransaction.commit()
        } else
            donate_card.visibility = View.GONE
    }

    private fun getPlayStoreMode(): Boolean {
        val referrer: String? = applicationContext.packageManager.getInstallerPackageName(applicationContext.packageName)
        return referrer?.startsWith("com.android.vending") ?: false
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
}
