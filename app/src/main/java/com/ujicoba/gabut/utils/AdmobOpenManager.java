package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.app.Application;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
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
    private boolean isShowingAd = false;
    private Date loadTime = null;
    private Application application;

    // ✅ Catat kapan terakhir kali Open Ads tayang untuk proteksi anti-spam
    public static long lastOpenAdDismissedTime = 0;

    private AdmobOpenManager() {}

    public static synchronized AdmobOpenManager getInstance(Application application) {
        if (instance == null) {
            instance = new AdmobOpenManager();
        }
        instance.application = application;

        // Otomatis preload iklan jika belum ada di memori
        if (!instance.isAdAvailable() && !instance.isLoadingAd) {
            instance.loadAd();
        }
        return instance;
    }

    public void init(Application app) {
        this.application = app;
        loadAd();
    }

    private boolean isAdAvailable() {
        return appOpenAd != null && loadTime != null &&
                (new Date().getTime() - loadTime.getTime()) < (4 * 60 * 60 * 1000);
    }

    public void loadAd() {
        if (isLoadingAd || isAdAvailable()) return;

        if (application == null) return;
        String adUnitId = Methods.getData(application, Data.ADMOB_OPEN_AD_ID);
        if (adUnitId == null || adUnitId.trim().isEmpty()) {
            Log.w(TAG, "AdMob Open Ad ID masih kosong.");
            return;
        }

        isLoadingAd = true;
        AdRequest request = new AdRequest.Builder().build();

        AppOpenAd.load(
                application,
                adUnitId.trim(),
                request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        loadTime = new Date();
                        isLoadingAd = false;
                        Log.d(TAG, "✅ AdMob Open Ad berhasil dimuat.");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        appOpenAd = null;
                        isLoadingAd = false;
                        Log.e(TAG, "❌ AdMob Open Ad gagal: " + error.getMessage());
                    }
                }
        );
    }

    public void showAdIfAvailable(Activity activity, Runnable onAdDismissed) {
        if (isShowingAd) {
            return;
        }

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            if (onAdDismissed != null) onAdDismissed.run();
            return;
        }

        if (!isAdAvailable()) {
            loadAd();
            if (onAdDismissed != null) onAdDismissed.run();
            return;
        }

        isShowingAd = true;

        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                lastOpenAdDismissedTime = SystemClock.elapsedRealtime(); // ✅ Catat waktu tutup iklan
                loadAd(); // Muat lagi untuk kesempatan berikutnya
                if (onAdDismissed != null) onAdDismissed.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                appOpenAd = null;
                isShowingAd = false;
                loadAd();
                if (onAdDismissed != null) onAdDismissed.run();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                isShowingAd = true;
            }
        });

        try {
            appOpenAd.show(activity);
        } catch (Exception e) {
            isShowingAd = false;
            appOpenAd = null;
            loadAd();
            if (onAdDismissed != null) onAdDismissed.run();
        }
    }
}