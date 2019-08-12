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

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.*


class ScrollingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        fab.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://t.me/hackintosh5"))
            startActivity(browserIntent)
        }
        sar_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.system_as_root_header)
        arch_card.findViewById<TextView>(R.id.header).text = resources.getText(R.string.arch_header)
        val arch = ArchDetector.getArch()
        val sar = MountDetector.isSAR()

        val archText = resources.getText(
            when (arch) {
                Arch.ARM64 -> R.string.arch_arm64
                Arch.ARM32 -> R.string.arch_arm32
                Arch.ARM32BINDER64 -> R.string.arch_binder64
                Arch.UNKNOWN -> R.string.arch_unknown
            }
        )
        val sarText = resources.getText(if (sar) R.string.sar_true else R.string.sar_false)

        val archImage = resources.getDrawable(
            when (arch) {
                Arch.ARM64 -> R.drawable.arch_64_bit
                Arch.ARM32 -> R.drawable.arch_32_bit
                Arch.ARM32BINDER64 -> R.drawable.arch_32_64_bit
                Arch.UNKNOWN -> R.drawable.arch_unknown
            }, theme
        )
        val sarImage = resources.getDrawable(if (sar) R.drawable.sar_true else R.drawable.sar_false, theme)

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

        arch_card.findViewById<TextView>(R.id.content).text = archText
        sar_card.findViewById<TextView>(R.id.content).text = sarText

        arch_card.findViewById<ImageView>(R.id.image).setImageDrawable(archImage)
        sar_card.findViewById<ImageView>(R.id.image).setImageDrawable(sarImage)

        ImageViewCompat.setImageTintList(arch_card.findViewById(R.id.image), archTint)
        ImageViewCompat.setImageTintList(sar_card.findViewById(R.id.image), sarTint)
    }
}
