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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import org.sufficientlysecure.donations.DonationsFragment
import tk.hack5.treblecheck.databinding.ActivityScrollingBinding
import tk.hack5.treblecheck.databinding.ContentScrollingBinding

class ScrollingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScrollingBinding
    private lateinit var content: ContentScrollingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrollingBinding.inflate(layoutInflater)
        content = ContentScrollingBinding.bind(binding.root.getChildAt(0))
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        binding.toolbarLayout.setCollapsedTitleTypeface(null) // prevent text going bold when collapsed
        binding.fab.setOnClickListener {
            val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=hackintosh5"))
            try {
                startActivity(telegramIntent)
            } catch (e: ActivityNotFoundException) {
                Log.e(tag, "Launch tg:// failed", e)
                val emailIntent = Intent(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_EMAIL, "treble@hack5.dev")
                    .addCategory(Intent.CATEGORY_APP_EMAIL)
                try {
                    startActivity(emailIntent)
                } catch (e: ActivityNotFoundException) {
                    Log.e(tag, "Launch email failed", e)
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/hackintosh5"))
                    try {
                        startActivity(browserIntent)
                    } catch (e: ActivityNotFoundException) {
                        Log.e(tag, "Launch browser failed", e)
                        Toast.makeText(this, R.string.no_browser, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


        var trebleFail = false
        val treble = try {
            TrebleDetector.getVndkData()
        } catch (e: Exception) {
            Log.e(tag, "Treble checks failed", e)
            trebleFail = true
            null
        }

        val arch = try {
            ArchDetector.getArch().also {
                if (it is Arch.UNKNOWN) {
                    Log.e(tag, "Unknown arch - ${it.cpuBits}:${it.binderBits}")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Arch checks failed", e)
            Arch.UNKNOWN(null, null)
        }

        val sar = try {
            MountDetector.isSAR()
        } catch (e: Exception) {
            Log.e(tag, "SAR checks failed", e)
            null
        }
        val ab = try {
            ABDetector.checkAB()
        } catch (e: Exception) {
            Log.e(tag, "AB checks failed", e)
            false
        }
        val dynamicPartitions = try {
            DynamicPartitionsDetector.isDynamic()
        } catch (e: Exception) {
            Log.e(tag, "Dynamic Partitions checks failed", e)
            null
        }
        val fileName = try {
            FileNameAnalyzer(treble, arch, sar).getFileName()
        } catch (e: Exception) {
            Log.e(tag, "File name detection failed", e)
            null
        }


        content.apply {
            filenameCard.header.text = resources.getText(R.string.filename_header)
            filenameCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    if (fileName == null)
                        R.drawable.filename_unknown
                    else
                        R.drawable.filename_known,
                    theme
                )
            )
            ImageViewCompat.setImageTintList(
                filenameCard.image,
                ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources,
                        if (fileName == null)
                            R.color.filename_unknown
                        else
                            R.color.filename_known,
                        theme
                    )
                )
            )
            filenameCard.content.text = if (fileName == null) {
                resources.getText(R.string.filename_unknown)
            } else {
                resources.getHtml(R.string.filename, Html.escapeHtml(fileName))
            }


            trebleCard.header.text = resources.getText(R.string.treble_header)
            trebleCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, when {
                        trebleFail -> R.drawable.unknown
                        treble == null -> R.drawable.treble_false
                        treble.legacy -> R.drawable.treble_legacy
                        else /* !treble.legacy */ -> R.drawable.treble_modern
                    }, theme
                )
            )
            ImageViewCompat.setImageTintList(
                trebleCard.image,
                ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources, if (trebleFail) R.color.unknown else
                            when (treble?.legacy) {
                                null -> R.color.treble_false
                                true -> R.color.treble_legacy
                                false -> {
                                    if (!treble.lite)
                                        R.color.treble_modern
                                    else
                                        R.color.treble_legacy
                                }
                            }, theme
                    )
                )
            )
            trebleCard.content.text = when {
                trebleFail -> resources.getHtml(R.string.treble_unknown)
                treble == null -> resources.getHtml(R.string.treble_false)
                treble.legacy -> resources.getHtml(
                    R.string.treble_legacy,
                    treble.vndkVersion,
                    treble.vndkSubVersion
                )
                else /* !treble.legacy */ -> resources.getHtml(
                    R.string.treble_modern,
                    treble.vndkVersion,
                    treble.vndkSubVersion
                )
            }

            sarCard.header.text = resources.getText(R.string.system_as_root_header)
            sarCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, when (sar) {
                        null -> R.drawable.unknown
                        false -> R.drawable.sar_false
                        true -> R.drawable.sar_true
                    }, theme
                )
            )
            ImageViewCompat.setImageTintList(
                sarCard.image,
                ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources,
                        when (sar) {
                            true -> R.color.sar_true
                            false -> R.color.sar_false
                            null -> R.color.unknown
                        }, theme
                    )
                )
            )
            sarCard.content.text = resources.getHtml(
                when (sar) {
                    null -> R.string.sar_unknown
                    false -> R.string.sar_false
                    true -> R.string.sar_true
                }
            )

            archCard.header.text = resources.getHtml(R.string.arch_header)
            archCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    when (arch) {
                        Arch.ARM64, Arch.X86_64 -> R.drawable.arch_64_bit
                        Arch.ARM32, Arch.X86 -> R.drawable.arch_32_bit
                        Arch.ARM32_BINDER64, Arch.X86_BINDER64 -> R.drawable.arch_32_64_bit
                        is Arch.UNKNOWN -> R.drawable.unknown
                    }, theme
                )
            )
            ImageViewCompat.setImageTintList(
                archCard.image,
                ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources, when (arch) {
                            Arch.ARM64, Arch.X86_64 -> R.color.arch_64_bit
                            Arch.ARM32, Arch.X86 -> R.color.arch_32_bit
                            Arch.ARM32_BINDER64, Arch.X86_BINDER64 -> R.color.arch_32_64_bit
                            is Arch.UNKNOWN -> R.color.unknown
                        }, theme
                    )
                )
            )
            archCard.content.text = resources.getHtml(
                when (arch) {
                    Arch.ARM64 -> R.string.arch_arm64
                    Arch.ARM32 -> R.string.arch_arm32
                    Arch.ARM32_BINDER64 -> R.string.arch_binder64
                    Arch.X86_64 -> R.string.arch_x86_64
                    Arch.X86_BINDER64 -> R.string.arch_x86_binder64
                    Arch.X86 -> R.string.arch_x86
                    is Arch.UNKNOWN -> R.string.arch_unknown
                }
            )

            abCard.header.text = resources.getHtml(R.string.ab_header)
            abCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    when (ab) {
                        null -> R.drawable.unknown
                        false -> R.drawable.ab_false
                        true -> R.drawable.ab_true
                    },
                    theme
                )
            )
            ImageViewCompat.setImageTintList(
                abCard.image,
                ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources,
                        when (ab) {
                            null -> R.color.unknown
                            false -> R.color.ab_false
                            true -> R.color.ab_true
                        },
                        theme
                    )
                )
            )
            abCard.content.text = resources.getHtml(
                when (ab) {
                    null -> R.string.ab_unknown
                    false -> R.string.ab_false
                    true -> R.string.ab_true
                }
            )

            dynamicpartitionsCard.header.text = resources.getHtml(R.string.dynamicpartitions_header)
            dynamicpartitionsCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    when (dynamicPartitions) {
                        null -> R.drawable.unknown
                        false -> R.drawable.dynamicpartitions_false
                        true -> R.drawable.dynamicpartitions_true
                    },
                    theme
                )
            )
            ImageViewCompat.setImageTintList(
                dynamicpartitionsCard.image,
                ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources,
                        when (dynamicPartitions) {
                            null -> R.color.unknown
                            false -> R.color.dynamicpartitions_false
                            true -> R.color.dynamicpartitions_true
                        },
                        theme
                    )
                )
            )
            dynamicpartitionsCard.content.text = resources.getHtml(
                when (dynamicPartitions) {
                    null -> R.string.dynamicpartitions_unknown
                    false -> R.string.dynamicpartitions_false
                    true -> R.string.dynamicpartitions_ramdisk
                }
            )

            themeCard.header.text = resources.getHtml(R.string.theme_header)
            themeCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.theme,
                    theme
                )
            )
            updateThemeText(false)
            themeCard.root.setOnClickListener { updateThemeText(true) }

            licenseCard.header.text = resources.getHtml(R.string.license_header)
            licenseCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.foss_license,
                    theme
                )
            )
            licenseCard.content.text = resources.getHtml(R.string.license)
            licenseCard.content.setLinkTextColor(content.licenseCard.content.textColors)
            Linkify.addLinks(content.licenseCard.content, Linkify.WEB_URLS)

            supportCard.header.text = resources.getHtml(R.string.support_header)
            supportCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.support,
                    theme
                )
            )
            supportCard.content.text = resources.getHtml(R.string.support)

            donateCard.image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.donate,
                    theme
                )
            )
        }


        val playStoreMode = getPlayStoreMode()

        content.donateCard.header.text = resources.getHtml(R.string.donate)
        content.donateCard.content.visibility = View.GONE
        val container = content.donateCard.frame
        container.visibility = View.VISIBLE
        container.id = View.generateViewId()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val allModes = BuildConfig.DONATIONS_DEBUG
        val donateFragment = DonationsFragment.newInstance(
            BuildConfig.DONATIONS_DEBUG,
            playStoreMode || allModes,
            BuildConfig.GPLAY_PUBK,
            BuildConfig.GPLAY_KEYS,
            BuildConfig.GPLAY_VALS,
            !playStoreMode || allModes,
            BuildConfig.PAYPAL_EMAIL,
            BuildConfig.PAYPAL_CURRENCY,
            BuildConfig.PAYPAL_DESCRIPTION,
            false,
            null
        )
        fragmentTransaction.replace(container.id, donateFragment, "donationsFragment")
        fragmentTransaction.commit()
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            fitToCutout(insets)
            view.onApplyWindowInsets(insets)
            insets
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
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
        with(sharedPrefs.edit()) {
            putInt("daynight", current)
            apply()
        }

        content.themeCard.content.text = resources.getHtml(
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

    private fun fitToCutout(insets: WindowInsets) = insets.run {
        val titleIsRtl =
            ViewCompat.getLayoutDirection(binding.toolbar) == ViewCompat.LAYOUT_DIRECTION_RTL
        val newLayoutParams = binding.toolbarLayout.layoutParams as ViewGroup.MarginLayoutParams
        val systemWindowInsetLeftCompat: Int
        val systemWindowInsetRightCompat: Int
        val systemWindowInsetBottomCompat: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val systemWindowInsets =
                getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            systemWindowInsetLeftCompat = systemWindowInsets.left
            systemWindowInsetRightCompat = systemWindowInsets.right
            systemWindowInsetBottomCompat = systemWindowInsets.bottom
        } else {
            @Suppress("DEPRECATION")
            systemWindowInsetLeftCompat = systemWindowInsetLeft
            @Suppress("DEPRECATION")
            systemWindowInsetRightCompat = systemWindowInsetRight
            @Suppress("DEPRECATION")
            systemWindowInsetBottomCompat = systemWindowInsetBottom
        }
        newLayoutParams.setMargins(
            if (titleIsRtl) 0 else systemWindowInsetLeftCompat, 0,
            if (titleIsRtl) systemWindowInsetRightCompat else 0, 0
        )
        binding.toolbarLayout.layoutParams = newLayoutParams
        val fabLayoutParams = binding.fab.layoutParams as ViewGroup.MarginLayoutParams
        fabLayoutParams.setMargins(
            resources.getDimensionPixelOffset(R.dimen.fab_margin) + systemWindowInsetLeftCompat, 0,
            resources.getDimensionPixelOffset(R.dimen.fab_margin) + systemWindowInsetRightCompat, 0
        )
        binding.fab.layoutParams = fabLayoutParams
        for (i in 0 until content.cards.childCount) {
            (content.cards.getChildAt(i) as ViewGroup).getChildAt(0).setPadding(
                systemWindowInsetLeftCompat,
                0,
                systemWindowInsetRightCompat,
                if (i == content.cards.childCount - 1) systemWindowInsetBottomCompat else 0
            )
        }
    }
}

private const val tag = "TrebleInfo"


fun Resources.getHtml(@StringRes id: Int, vararg formatArgs: Any?): Spanned? {
    val html = getString(id, *formatArgs)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(html)
    }
}