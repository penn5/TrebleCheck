/*
 *     Treble Info
 *     Copyright (C) 2023 Hackintosh Five
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
// SPDX-License-Identifier: GPL-3.0-or-later

package tk.hack5.treblecheck

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope

class PaypalIABHelper(private val activity: Activity, private val listener: IABListener, private val scope: CoroutineScope) {

    suspend fun connect() {
    }

    suspend fun makePayment() {
        val uriBuilder = Uri.Builder()
        uriBuilder.scheme("https").authority("www.paypal.com").path("cgi-bin/webscr")
        uriBuilder.appendQueryParameter("cmd", "_donations")

        uriBuilder.appendQueryParameter("business", BuildConfig.PAYPAL_EMAIL)
        uriBuilder.appendQueryParameter("lc", "US")
        uriBuilder.appendQueryParameter("item_name", BuildConfig.PAYPAL_DESCRIPTION)
        uriBuilder.appendQueryParameter("no_note", "1")
        uriBuilder.appendQueryParameter("no_shipping", "1")
        uriBuilder.appendQueryParameter("currency_code", BuildConfig.PAYPAL_CURRENCY)
        val payPalUri = uriBuilder.build()

        val intent = Intent(Intent.ACTION_VIEW, payPalUri)
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(tag, "Launch browser failed", e)
            Toast.makeText(activity, R.string.no_browser, Toast.LENGTH_LONG).show()
        }
    }

}
interface IABListener {
    fun paymentFailed()
    fun paymentSuccess()
}

typealias IABHelper = PaypalIABHelper

private const val tag = "PaypalDonate"