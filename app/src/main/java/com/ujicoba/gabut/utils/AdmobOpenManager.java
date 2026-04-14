package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.ujicoba.gabut.api.Data;

import java.util.Date;

public class AdmobOpenManager {
    private static final String TAG = "AdmobOpenManager";
    private static AdmobOpenManager instance;

    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false; // ✅ cegah show ganda
    private Date loadTime = null;
    private Application application;

    private AdmobOpenManager() {}

    public static AdmobOpenManager getInstance(Application application) {
        if (instance == null) {
            instance = new AdmobOpenManager();
        }
        instance.application = application;
        return instance;
    }

    /** Inisialisasi awal */
    public void init(Application app) {
        this.application = app;
        loadAd();
    }

    /** Cek apakah iklan masih valid (<4 jam) */
    private boolean isAdAvailable() {
        return appOpenAd != null && loadTime != null &&
                (new Date().getTime() - loadTime.getTime()) < 4 * 60 * 60 * 1000;
    }

    /** Load App Open Ad */
    public void loadAd() {
        if (isLoadingAd || isAdAvailable()) return;

        isLoadingAd = true;
        String adUnitId = Methods.getData(application, Data.ADMOB_OPEN_AD_ID);
        AdRequest request = new AdRequest.Builder().build();

        AppOpenAd.load(application, adUnitId, request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        loadTime = new Date();
                        isLoadingAd = false;
                        Log.d(TAG, "✅ AdMob Open Ad loaded.");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        isLoadingAd = false;
                        Log.e(TAG, "❌ AdMob Open Ad failed: " + error.getMessage());
                    }
                });
    }

    /** Tampilkan Open Ad (hanya kalau tersedia) */
    public void showAdIfAvailable(Activity activity, Runnable onAdDismissed) {
        if (isShowingAd) {
            Log.d(TAG, "⚠️ Open Ad already showing.");
            return;
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "⚠️ No Ad available. Reloading...");
            loadAd();
            if (onAdDismissed != null) onAdDismissed.run();
            return;
        }

        isShowingAd = true;

        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "✅ Open Ad dismissed.");
                appOpenAd = null;
                isShowingAd = false;
                loadAd();
                if (onAdDismissed != null) onAdDismissed.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError error) {
                Log.e(TAG, "❌ Failed to show Open Ad: " + error.getMessage());
                appOpenAd = null;
                isShowingAd = false;
                loadAd();
                if (onAdDismissed != null) onAdDismissed.run();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(TAG, "✅ Open Ad is showing.");
            }
        });

        appOpenAd.show(activity);
    }
}
