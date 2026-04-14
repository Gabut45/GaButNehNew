package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppOpenObserver implements Application.ActivityLifecycleCallbacks {

    private static final long MIN_BACKGROUND_DURATION = 120_000; // 120 detik
    private long lastBackgroundTime = 0;
    private boolean fromGame = false;
    private boolean justShowedInterstitial = false;
    private final Application application;

    private int startedActivityCount = 0; // ✅ track activity aktif

    public AppOpenObserver(Application application) {
        this.application = application;
        application.registerActivityLifecycleCallbacks(this);
    }

    /** Panggil saat user membuka game (CustomTabsIntent) */
    public void markFromGame() {
        fromGame = true;
    }

    /** Panggil setelah interstitial ditampilkan */
    public void markInterstitialShown() {
        justShowedInterstitial = true;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        startedActivityCount++;
        if (startedActivityCount == 1) { // ✅ hanya saat app balik foreground
            long timeInBackground = SystemClock.elapsedRealtime() - lastBackgroundTime;

            if (!fromGame && !justShowedInterstitial &&
                    lastBackgroundTime > 0 && timeInBackground > MIN_BACKGROUND_DURATION) {

                // ✅ tampilkan Open Ads sesuai rekomendasi Google
                AdmobOpenManager.getInstance(application).showAdIfAvailable(activity, () -> {
                    AppLovinOpenManager.getInstance(application).showAdIfAvailable(activity, null);
                });
            }

            justShowedInterstitial = false; // reset sekali pakai
        }

        fromGame = false; // reset flag tiap kali masuk foreground
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        startedActivityCount--;
        if (startedActivityCount == 0) {
            // ✅ benar-benar background
            lastBackgroundTime = SystemClock.elapsedRealtime();
        }
    }

    // Lifecycle wajib, tapi kosong
    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
    @Override public void onActivityResumed(@NonNull Activity activity) {}
    @Override public void onActivityPaused(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) {}
}
