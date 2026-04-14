package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.ujicoba.gabut.api.Data;

public class AppLovinOpenManager {

    private static AppLovinOpenManager instance;
    private MaxAppOpenAd appOpenAd;
    private boolean isAdAvailable = false;
    private boolean isShowingAd = false;
    private Application application;
    private String adUnitId;
    private Runnable onAdDismissedCallback;

    private AppLovinOpenManager(Application application) {
        this.application = application;
        this.adUnitId = Methods.getData(application, Data.AL_OPEN_AD_ID); // ambil ID dari server
        loadAd();
    }

    public static synchronized AppLovinOpenManager getInstance(Application application) {
        if (instance == null) {
            instance = new AppLovinOpenManager(application);
        }
        return instance;
    }

    private void loadAd() {
        if (adUnitId == null || adUnitId.isEmpty()) return;

        appOpenAd = new MaxAppOpenAd(adUnitId, application);
        appOpenAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(@NonNull MaxAd ad) {
                isAdAvailable = true;
            }

            @Override
            public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                isAdAvailable = false;
            }

            @Override
            public void onAdDisplayed(@NonNull MaxAd ad) {
                isShowingAd = true;
            }

            @Override
            public void onAdHidden(@NonNull MaxAd ad) {
                isShowingAd = false;
                isAdAvailable = false;
                loadAd(); // reload setelah ditutup
                if (onAdDismissedCallback != null) {
                    onAdDismissedCallback.run();
                    onAdDismissedCallback = null;
                }
            }

            @Override
            public void onAdClicked(@NonNull MaxAd ad) { }

            @Override
            public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
                isShowingAd = false;
                isAdAvailable = false;
                loadAd(); // reload kalau gagal tampil
                if (onAdDismissedCallback != null) {
                    onAdDismissedCallback.run();
                    onAdDismissedCallback = null;
                }
            }
        });

        appOpenAd.loadAd();
    }

    /**
     * Show Open Ad jika tersedia (hanya saat kembali dari background)
     */
    public void showAdIfAvailable(Activity activity, Runnable onAdDismissed) {
        if (isAdAvailable && appOpenAd != null && !isShowingAd) {
            this.onAdDismissedCallback = onAdDismissed;
            appOpenAd.showAd(); // ✅ hanya ini yang valid untuk MaxAppOpenAd
        } else {
            if (onAdDismissed != null) onAdDismissed.run();
        }
    }

}
