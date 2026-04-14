package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;


import com.ujicoba.gabut.api.Data;

public class AdsManager {

    public static AdManagerInterstitialAd mAdManagerInterstitialAd;
    public static RewardedAd mRewardedAd;
    public static AdManagerAdView adView;



    public static boolean isRewardedAdReady() {
        return mRewardedAd != null;
    }

    public interface AdsManagerCallback {
        void onAdDismissed();
    }

    private static AdsManagerCallback adsManagerCallback;

    public static void setAdsManagerCallback(AdsManagerCallback callback) {
        adsManagerCallback = callback;
    }

    public static void initAds(Context ctx) {
        MobileAds.initialize(ctx, initializationStatus -> {});
    }

    // ===================== BANNER ============================
    public static void loadBannerAds(Context ctx, LinearLayout bannerLayout) {
        adView = new AdManagerAdView(ctx);
        adView.setAdSizes(AdSize.BANNER);
        adView.setAdUnitId(Methods.getData(ctx, Data.GAM_BANNER_ID));

        bannerLayout.removeAllViews();
        bannerLayout.addView(adView);

        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public static void adManagerBannerCallback(Context ctx, LinearLayout bannerLayout) {
        adView.setAdListener(new AdListener() {
            @Override public void onAdClicked() {}
            @Override public void onAdClosed() {
                loadBannerAds(ctx, bannerLayout);
            }
            @Override public void onAdFailedToLoad(LoadAdError adError) {
                Log.e("checkAds", "Banner failed: " + adError.getMessage());
                loadBannerAds(ctx, bannerLayout);
            }
            @Override public void onAdImpression() {}
            @Override public void onAdLoaded() {
                super.onAdLoaded();
            }
            @Override public void onAdOpened() {}
        });
    }

    public static void destroyBannerAd() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }

    // ===================== INTERSTITIAL ============================
    public static void loadInterstitialAds(Context ctx) {
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();

        AdManagerInterstitialAd.load(ctx, Methods.getData(ctx, Data.GAM_INTERSTITIAL_ID), adRequest,
                new AdManagerInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                        mAdManagerInterstitialAd = interstitialAd;
                        Log.i("checkAds", "onAdLoaded");
                        adsManagerInterCallback(ctx);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e("checkAds", "Interstitial failed: " + loadAdError.getMessage());
                        mAdManagerInterstitialAd = null;
                    }
                });
    }

    public static void adsManagerInterCallback(Context ctx) {
        mAdManagerInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                Log.d("checkAds", "Interstitial clicked.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d("checkAds", "Interstitial dismissed.");
                mAdManagerInterstitialAd = null;
                loadInterstitialAds(ctx);
                if (adsManagerCallback != null) {
                    adsManagerCallback.onAdDismissed();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                Log.e("checkAds", "Interstitial failed to show.");
                mAdManagerInterstitialAd = null;
                loadInterstitialAds(ctx);
            }

            @Override public void onAdImpression() {}
            @Override public void onAdShowedFullScreenContent() {}
        });
    }

    public static void showInterstitialAds(Activity activity) {
        if (mAdManagerInterstitialAd != null) {
            mAdManagerInterstitialAd.show(activity);
        } else {
            Log.d("checkAds", "Interstitial not ready.");
        }
    }

    // ===================== REWARDED ============================
    public static void loadRewardedAd(Context ctx) {
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        String adUnitId = Methods.getData(ctx, Data.GAM_REWARDED_ID);

        RewardedAd.load(ctx, adUnitId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                mRewardedAd = ad;
                Log.d("checkAds", "Rewarded loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e("checkAds", "Rewarded failed: " + loadAdError.getMessage());
                mRewardedAd = null;
            }
        });
    }

    public static void showRewardedAd(Activity activity, OnUserEarnedRewardListener rewardListener) {
        if (mRewardedAd != null) {
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d("checkAds", "Rewarded dismissed.");
                    mRewardedAd = null;
                    loadRewardedAd(activity);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.e("checkAds", "Rewarded failed to show.");
                    mRewardedAd = null;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d("checkAds", "Rewarded shown.");
                }
            });

            mRewardedAd.show(activity, rewardListener);
        } else {
            Log.d("checkAds", "Rewarded ad not ready.");
        }
    }
}
