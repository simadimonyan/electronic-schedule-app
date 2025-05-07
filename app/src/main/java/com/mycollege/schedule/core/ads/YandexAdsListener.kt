package com.mycollege.schedule.core.ads

import android.util.Log
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

class YandexAdsListener : BannerAdEventListener {

    override fun onAdClicked() {
        Log.i("YandexAds", "Clicked")
    }

    override fun onAdFailedToLoad(error: AdRequestError) {
        Log.i("YandexAds", "Error: $error")
    }

    override fun onAdLoaded() {
        Log.i("YandexAds", "Loaded")
    }

    override fun onImpression(impressionData: ImpressionData?) {
        Log.i("YandexAds", "Impression")
    }

    override fun onLeftApplication() {
        Log.i("YandexAds", "LeftApp")
    }

    override fun onReturnedToApplication() {
        Log.i("YandexAds", "ReturnedToApp")
    }

}