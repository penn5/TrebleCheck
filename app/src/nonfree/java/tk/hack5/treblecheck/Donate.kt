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
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.Purchase.PurchaseState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GoogleIABHelper(private val activity: Activity, private val listener: IABListener, private val scope: CoroutineScope) : PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    private var connected = false

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || purchases?.singleOrNull() == null)
            listener.paymentFailed()
        else {
            Log.d(tag, purchases.toString())
            for (purchase in purchases) {
                Log.d(tag, "Purchased ${purchase.products} successfully. State is ${purchase.purchaseState}")
                if (purchase.purchaseState != PurchaseState.PURCHASED)
                    continue
                scope.launch {
                    connect()
                    val result = billingClient.consumePurchase(
                        ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                    )
                    Log.d(tag, "Consumed")
                    if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.e(tag, "Consumption failed!")
                        listener.paymentFailed()
                    } else {
                        Log.d(tag, "Consumption successful")
                        for (product in purchase.products) {
                            listener.paymentSuccess()
                        }
                    }
                }
            }
        }
    }

    suspend fun connect() {
        while (!connected) {
            val job = Job()
            try {
                billingClient =
                    BillingClient.newBuilder(activity).setListener(this).enablePendingPurchases()
                        .build()
                billingClient.startConnection(object : BillingClientStateListener {
                    /**
                     * Called to notify that connection to billing service was lost
                     *
                     *
                     * Note: This does not remove billing service connection itself - this binding to the service
                     * will remain active, and you will receive a call to [.onBillingSetupFinished] when billing
                     * service is next running and setup is complete.
                     */
                    override fun onBillingServiceDisconnected() {
                        Log.e(tag, "Connection lost!")
                        connected = false
                        scope.launch { connect() }
                    }

                    /**
                     * Called to notify that setup is complete.
                     *
                     * @param billingResult The [BillingResult] which returns the status of the setup process.
                     */
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        connected = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                        if (connected) {
                            Log.d(tag, "Connected")
                        } else {
                            Log.e(
                                tag,
                                "Connection was not successful (${billingResult.responseCode}:${billingResult.debugMessage})"
                            )
                        }
                        job.complete()
                    }
                })
            } catch (e: Exception) {
                connected = false
                job.complete()
            }

            job.join()

            if (!connected) {
                delay(10000)
            }
        }
    }

    suspend fun makePayment() {
        connect()

        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(BuildConfig.GPLAY_PRODUCT)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val query = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
        val products = billingClient.queryProductDetails(query)
        if (products.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            throw RuntimeException("Query product details failed with ${products.billingResult}")
        }
        val productDetails = products.productDetailsList?.singleOrNull()
        productDetails ?: throw RuntimeException("Query product details failed with $products")
        val productDetailsParams = ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .setIsOfferPersonalized(false)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            throw RuntimeException("Launch billing flow failed with $billingResult")
        }

        /*

        Log.d(tag, "Getting SKUs for $productId")
        val skuDetails = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(productId))
            .setType(BillingClient.SkuType.INAPP)
            .build()
        ensureConnected {
            billingClient.querySkuDetailsAsync(skuDetails) {
                    billingResult: BillingResult, skuDetails: MutableList<SkuDetails>? ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK)
                    listener.donationFailed()
                else {
                    if (skuDetails?.size != 1) {
                        Log.e(tag, "No SKU available for donation. Check you are passing correct productId and that it is valid on Google servers $skuDetails")
                        listener.donationFailed()
                    } else {
                        val params = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails[0])
                            .build()
                        ensureConnected {
                            billingClient.launchBillingFlow(activity, params)
                        }
                    }
                }
            }
        }*/
    }
}

interface IABListener {
    fun paymentFailed()
    fun paymentSuccess()
}


typealias IABHelper = GoogleIABHelper

private const val tag = "GoogleDonate"
