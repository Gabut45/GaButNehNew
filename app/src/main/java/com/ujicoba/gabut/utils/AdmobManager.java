package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.ujicoba.gabut.api.Data;

public class AdmobManager {

    private static final String TAG = "AdmobManager";

    private static InterstitialAd interstitialAd;
    private static RewardedAd rewardedAd;
    private static AppOpenAd appOpenAd;
    private static boolean isLoadingAppOpenAd = false;

    // --- INTERSTITIAL ---
    public static void loadInterstitialAd(Activity activity) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                activity,
                Methods.getData(activity, Data.ADMOB_INTERSTITIAL_ID),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        Log.d(TAG, "Interstitial loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "Interstitial failed: " + loadAdError.getMessage());
                        interstitialAd = null;
                    }
                });
    }

    public static void showInterstitialAd(Activity activity) {
        if (interstitialAd != null) {
            interstitialAd.show(activity);
        } else {
            loadInterstitialAd(activity);
        }
    }

    // --- REWARDED ---
    public static void loadRewardedAd(Activity activity) {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(
                activity,
                Methods.getData(activity, Data.ADMOB_REWARDED_ID),
                adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(TAG, "Rewarded loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "Rewarded failed: " + loadAdError.getMessage());
                        rewardedAd = null;
                    }
                });
    }

    public static void showRewardedAd(Activity activity, com.google.android.gms.ads.OnUserEarnedRewardListener listener) {
        if (rewardedAd != null) {
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedAd = null;
                    loadRewardedAd(activity);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    rewardedAd = null;
                    loadRewardedAd(activity);
                }
            });

            rewardedAd.show(activity, listener);
        } else {
            loadRewardedAd(activity);
            Methods.showToast(activity, "Iklan belum siap, coba lagi nanti.");
        }
    }

    // --- BANNER ---

    // --- INIT ADMOB (BANNER UTAMA UNTUK ACTIVITY) ---
    public static void initAdmob(Activity activity, ViewGroup bannerContainer) {
        MobileAds.initialize(activity, initializationStatus -> {});

        com.google.android.gms.ads.AdView adView = new com.google.android.gms.ads.AdView(activity);
        adView.setAdUnitId(Methods.getData(activity, Data.ADMOB_BANNER_ID));
        adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);

        bannerContainer.removeAllViews();
        bannerContainer.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public static void loadBannerAd(Activity activity, FrameLayout bannerContainer) {
        com.google.android.gms.ads.admanager.AdManagerAdView adView =
                new com.google.android.gms.ads.admanager.AdManagerAdView(activity);
        adView.setAdUnitId(Methods.getData(activity, Data.ADMOB_BANNER_ID));
        adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        bannerContainer.removeAllViews();
        bannerContainer.addView(adView);
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    // --- APP OPEN ADS ---
    public static void loadAppOpenAd(Application application) {
        if (isLoadingAppOpenAd || appOpenAd != null) return;

        isLoadingAppOpenAd = true;

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                application.getApplicationContext(),
                Methods.getData(application, Data.ADMOB_OPEN_AD_ID),
                request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        isLoadingAppOpenAd = false;
                        Log.d(TAG, "AppOpenAd loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        appOpenAd = null;
                        isLoadingAppOpenAd = false;
                        Log.e(TAG, "AppOpenAd failed: " + loadAdError.getMessage());
                    }
                }
        );
    }


    public static void showAppOpenAdIfAvailable(Activity activity) {
        if (appOpenAd != null) {
            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    appOpenAd = null;
                    loadAppOpenAd(activity.getApplication());
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    appOpenAd = null;
                    loadAppOpenAd(activity.getApplication());
                }
            });

            appOpenAd.show(activity);
        } else {
            loadAppOpenAd(activity.getApplication());
        }
    }

    public static boolean isRewardedAdReady() {
        return rewardedAd != null;
    }
}
