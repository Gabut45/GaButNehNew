package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.MaxReward;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.applovin.sdk.AppLovinSdkUtils;
import com.ujicoba.gabut.api.Data;

public class ApplovinManager {

    public static MaxAdView adView;
    public static MaxInterstitialAd maxInterstitialAd;
    private static MaxRewardedAd maxRewardedAd;
    private static Runnable rewardedCallback;

    /**
     * ✅ Inisialisasi Applovin pakai SDK Key dari server (bukan Manifest)
     */
    public static void initApplovin(Activity activity, LinearLayout bannerLayout) {
        String sdkKey = Methods.getData(activity, Data.AL_SDK_KEY); // 🔑 Ambil SDK Key dari server

        AppLovinSdkInitializationConfiguration initConfig =
                AppLovinSdkInitializationConfiguration.builder(sdkKey, activity)
                        .setMediationProvider("max")
                        .build();

        AppLovinSdk.getInstance(activity).initialize(initConfig, configuration -> {
            if (bannerLayout != null) {
                loadApplovinBanner(activity, bannerLayout);
            }
            loadApplovinInterstitial(activity);
            loadApplovinRewarded(activity);
        });
    }

    /**
     * ✅ Banner Ads
     */
    public static void loadApplovinBanner(Context ctx, LinearLayout bannerLayout) {
        adView = new MaxAdView(Methods.getData(ctx, Data.AL_BANNER_ID), ctx);
        adView.setListener(new ApplovinBannerListener());

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        final boolean isTablet = AppLovinSdkUtils.isTablet(ctx);
        final int heightPx = AppLovinSdkUtils.dpToPx(ctx, isTablet ? 90 : 50);

        adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx, Gravity.BOTTOM));
        bannerLayout.addView(adView);

        adView.loadAd();
        adView.setVisibility(View.VISIBLE);
        adView.startAutoRefresh();
    }

    /**
     * ✅ Interstitial Ads
     */
    public static void loadApplovinInterstitial(Activity activity) {
        maxInterstitialAd = new MaxInterstitialAd(Methods.getData(activity, Data.AL_INTERSTITIAL_ID), activity);
        maxInterstitialAd.setListener(new ApplovinInterstitialListener());
        maxInterstitialAd.loadAd();
    }

    public static void showApplovinInterstitial(Activity activity) {
        if (maxInterstitialAd != null && maxInterstitialAd.isReady()) {
            maxInterstitialAd.showAd();
        } else {
            loadApplovinInterstitial(activity);
        }
    }

    /**
     * ✅ Rewarded Ads
     */
    public static void loadApplovinRewarded(Activity activity) {
        maxRewardedAd = MaxRewardedAd.getInstance(Methods.getData(activity, Data.AL_REWARDED_ID), activity);

        maxRewardedAd.setListener(new MaxRewardedAdListener() {
            @Override public void onAdLoaded(MaxAd ad) { }
            @Override public void onAdDisplayed(MaxAd ad) { }

            @Override
            public void onAdHidden(MaxAd ad) {
                if (rewardedCallback != null) {
                    rewardedCallback.run();
                    rewardedCallback = null;
                }
                loadApplovinRewarded(activity); // Reload iklan baru
            }

            @Override public void onAdClicked(MaxAd ad) { }
            @Override public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e("Applovin", "Rewarded load failed: " + error.getMessage());
            }
            @Override public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.e("Applovin", "Rewarded display failed: " + error.getMessage());
            }

            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                if (rewardedCallback != null) {
                    rewardedCallback.run();
                    rewardedCallback = null;
                }
            }
        });

        maxRewardedAd.loadAd();
    }

    public static void showRewardedAd(Activity activity, Runnable onRewarded) {
        if (maxRewardedAd != null && maxRewardedAd.isReady()) {
            rewardedCallback = onRewarded;
            maxRewardedAd.showAd();
        } else {
            Log.d("Applovin", "Rewarded ad not ready");
            loadApplovinRewarded(activity);
            Methods.showToast(activity, "Iklan belum siap, coba lagi nanti.");
        }
    }

    public static boolean isRewardedAdReady() {
        return maxRewardedAd != null && maxRewardedAd.isReady();
    }

    /**
     * ✅ Listener untuk Interstitial
     */
    public static class ApplovinInterstitialListener implements com.applovin.mediation.MaxAdListener {
        @Override public void onAdLoaded(MaxAd ad) { Log.i("Applovin", "Interstitial loaded"); }
        @Override public void onAdDisplayed(MaxAd ad) {}
        @Override public void onAdHidden(MaxAd ad) {
            maxInterstitialAd.loadAd();
            if (applovinManagerCallback != null) {
                applovinManagerCallback.onAdHidden();
            }
        }
        @Override public void onAdClicked(MaxAd ad) {}
        @Override public void onAdLoadFailed(String adUnitId, MaxError error) { maxInterstitialAd.loadAd(); }
        @Override public void onAdDisplayFailed(MaxAd ad, MaxError error) { maxInterstitialAd.loadAd(); }
    }

    /**
     * ✅ Listener untuk Banner
     */
    public static class ApplovinBannerListener implements MaxAdViewAdListener {
        @Override public void onAdExpanded(MaxAd ad) {}
        @Override public void onAdCollapsed(MaxAd ad) {}
        @Override public void onAdLoaded(MaxAd ad) { Log.i("Applovin", "Banner loaded"); }
        @Override public void onAdDisplayed(MaxAd ad) {}
        @Override public void onAdHidden(MaxAd ad) {}
        @Override public void onAdClicked(MaxAd ad) {}
        @Override public void onAdLoadFailed(String adUnitId, MaxError error) {}
        @Override public void onAdDisplayFailed(MaxAd ad, MaxError error) {}
    }

    /**
     * ✅ Callback untuk notifikasi Interstitial selesai
     */
    public interface ApplovinManagerCallback {
        void onAdHidden();
    }

    private static ApplovinManagerCallback applovinManagerCallback;

    public static void setApplovinManagerCallback(ApplovinManagerCallback callback) {
        applovinManagerCallback = callback;
    }
}
