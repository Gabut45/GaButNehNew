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

    private static final String TAG = "ApplovinManager";

    public static MaxAdView adView;
    public static MaxInterstitialAd maxInterstitialAd;
    private static MaxRewardedAd maxRewardedAd;
    private static Runnable rewardedCallback;
    private static boolean hasUserEarnedReward = false; // ✅ Flag pencegah reward palsu

    /**
     * ✅ Inisialisasi Applovin pakai SDK Key dari server
     */
    public static void initApplovin(Activity activity, LinearLayout bannerLayout) {
        String sdkKey = Methods.getData(activity, Data.AL_SDK_KEY);

        if (sdkKey == null || sdkKey.trim().isEmpty()) {
            Log.e(TAG, "SDK Key kosong, inisialisasi AppLovin dibatalkan.");
            return;
        }

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
     * ✅ Banner Ads (Bersih dari tumpukan view ganda)
     */
    public static void loadApplovinBanner(Context ctx, LinearLayout bannerLayout) {
        String bannerId = Methods.getData(ctx, Data.AL_BANNER_ID);
        if (bannerId == null || bannerId.trim().isEmpty()) return;

        if (adView != null) {
            adView.destroy();
        }

        adView = new MaxAdView(bannerId, ctx);
        adView.setListener(new ApplovinBannerListener());

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        final boolean isTablet = AppLovinSdkUtils.isTablet(ctx);
        final int heightPx = AppLovinSdkUtils.dpToPx(ctx, isTablet ? 90 : 50);

        adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx, Gravity.BOTTOM));
        bannerLayout.removeAllViews(); // ✅ Cegah view bertumpuk
        bannerLayout.addView(adView);

        adView.loadAd();
        adView.setVisibility(View.VISIBLE);
        adView.startAutoRefresh();
    }

    /**
     * ✅ Interstitial Ads
     */
    public static void loadApplovinInterstitial(Activity activity) {
        String interstitialId = Methods.getData(activity, Data.AL_INTERSTITIAL_ID);
        if (interstitialId == null || interstitialId.trim().isEmpty()) return;

        maxInterstitialAd = new MaxInterstitialAd(interstitialId, activity);
        maxInterstitialAd.setListener(new ApplovinInterstitialListener(activity));
        maxInterstitialAd.loadAd();
    }

    public static void showApplovinInterstitial(Activity activity) {
        if (maxInterstitialAd != null && maxInterstitialAd.isReady()) {
            maxInterstitialAd.showAd(activity); // ✅ Kirim activity aktif
        } else {
            loadApplovinInterstitial(activity);
        }
    }

    /**
     * ✅ Rewarded Ads (Aman dari kecurangan skip iklan)
     */
    public static void loadApplovinRewarded(Activity activity) {
        String rewardedId = Methods.getData(activity, Data.AL_REWARDED_ID);
        if (rewardedId == null || rewardedId.trim().isEmpty()) return;

        maxRewardedAd = MaxRewardedAd.getInstance(rewardedId, activity);
        hasUserEarnedReward = false;

        maxRewardedAd.setListener(new MaxRewardedAdListener() {
            @Override public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "Rewarded ad ready.");
            }

            @Override public void onAdDisplayed(MaxAd ad) {
                hasUserEarnedReward = false; // Reset saat mulai tayang
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                // ✅ Hadiah HANYA dicairkan jika user benar-benar menonton sampai tuntas
                if (hasUserEarnedReward && rewardedCallback != null) {
                    rewardedCallback.run();
                }
                rewardedCallback = null;
                hasUserEarnedReward = false;
                loadApplovinRewarded(activity); // Siapkan iklan baru
            }

            @Override public void onAdClicked(MaxAd ad) { }

            @Override public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "Rewarded load failed: " + error.getMessage());
                // ✅ JANGAN load ulang instan di sini untuk mencegah loop spam
            }

            @Override public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.e(TAG, "Rewarded display failed: " + error.getMessage());
                hasUserEarnedReward = false;
                rewardedCallback = null;
                loadApplovinRewarded(activity);
            }

            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                // ✅ User resmi menyelesaikan video iklan
                hasUserEarnedReward = true;
            }
        });

        maxRewardedAd.loadAd();
    }

    public static void showRewardedAd(Activity activity, Runnable onRewarded) {
        if (maxRewardedAd != null && maxRewardedAd.isReady()) {
            rewardedCallback = onRewarded;
            hasUserEarnedReward = false;
            maxRewardedAd.showAd(activity); // ✅ Kirim activity aktif
        } else {
            Log.d(TAG, "Rewarded ad not ready");
            loadApplovinRewarded(activity);
            Methods.showToast(activity, "Iklan belum siap, coba lagi nanti.");
        }
    }

    public static boolean isRewardedAdReady() {
        return maxRewardedAd != null && maxRewardedAd.isReady();
    }

    /**
     * ✅ Listener untuk Interstitial (Bebas dari infinite loop spam)
     */
    public static class ApplovinInterstitialListener implements com.applovin.mediation.MaxAdListener {
        private final Activity activity;

        public ApplovinInterstitialListener(Activity activity) {
            this.activity = activity;
        }

        @Override public void onAdLoaded(MaxAd ad) {
            Log.i(TAG, "Interstitial loaded");
        }

        @Override public void onAdDisplayed(MaxAd ad) {}

        @Override public void onAdHidden(MaxAd ad) {
            if (maxInterstitialAd != null) {
                maxInterstitialAd.loadAd();
            }
            if (applovinManagerCallback != null) {
                applovinManagerCallback.onAdHidden();
            }
        }

        @Override public void onAdClicked(MaxAd ad) {}

        @Override public void onAdLoadFailed(String adUnitId, MaxError error) {
            Log.e(TAG, "Interstitial load failed: " + error.getMessage());
            // ✅ Tidak memanggil loadAd() lagi di sini agar tidak looping spam request
        }

        @Override public void onAdDisplayFailed(MaxAd ad, MaxError error) {
            Log.e(TAG, "Interstitial display failed: " + error.getMessage());
            if (maxInterstitialAd != null) {
                maxInterstitialAd.loadAd();
            }
        }
    }

    /**
     * ✅ Listener untuk Banner
     */
    public static class ApplovinBannerListener implements MaxAdViewAdListener {
        @Override public void onAdExpanded(MaxAd ad) {}
        @Override public void onAdCollapsed(MaxAd ad) {}
        @Override public void onAdLoaded(MaxAd ad) { Log.i(TAG, "Banner loaded"); }
        @Override public void onAdDisplayed(MaxAd ad) {}
        @Override public void onAdHidden(MaxAd ad) {}
        @Override public void onAdClicked(MaxAd ad) {}
        @Override public void onAdLoadFailed(String adUnitId, MaxError error) {
            Log.e(TAG, "Banner load failed: " + error.getMessage());
        }
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