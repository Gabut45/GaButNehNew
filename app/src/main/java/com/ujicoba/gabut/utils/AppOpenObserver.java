package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ujicoba.gabut.activities.SplashActivity;

public class AppOpenObserver implements Application.ActivityLifecycleCallbacks {

    private static final long MIN_BACKGROUND_DURATION = 5_000; // 120 detik (2 menit)
    private static volatile AppOpenObserver instance;

    private long lastBackgroundTime = 0;
    private boolean fromGame = false;
    private boolean justShowedInterstitial = false;
    private final Application application;

    private int startedActivityCount = 0; // Track jumlah activity aktif

    public AppOpenObserver(Application application) {
        this.application = application;
        instance = this;
        application.registerActivityLifecycleCallbacks(this);
    }

    /**
     * Singleton instance agar mudah dipanggil dari GameActivity / TaskActivity / SpinActivity
     */
    public static AppOpenObserver getInstance() {
        return instance;
    }

    /** Panggil saat user membuka game web (CustomTabsIntent) */
    public void markFromGame() {
        this.fromGame = true;
    }

    /** Panggil tepat setelah iklan Interstitial ditampilkan/ditutup */
    public void markInterstitialShown() {
        this.justShowedInterstitial = true;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        startedActivityCount++;

        // Hanya dieksekusi saat aplikasi pertama kali kembali ke FOREGROUND dari background
        if (startedActivityCount == 1) {
            long timeInBackground = SystemClock.elapsedRealtime() - lastBackgroundTime;

            // Syarat aman penayangan iklan:
            // 1. Bukan kembali dari bermain game (CustomTabs)
            // 2. Bukan baru saja melihat iklan Interstitial
            // 3. Pernah masuk background (lastBackgroundTime > 0)
            // 4. Berada di background lebih dari 120 detik
            // 5. Bukan di SplashActivity
            // 6. Activity tidak sedang dalam proses tutup/hancur
            if (!fromGame && !justShowedInterstitial &&
                    lastBackgroundTime > 0 && timeInBackground > MIN_BACKGROUND_DURATION) {

                if (!(activity instanceof SplashActivity) && !activity.isFinishing() && !activity.isDestroyed()) {
                    // Tampilkan Open Ad AdMob, jika gagal/kosong fallback ke AppLovin
                    AdmobOpenManager.getInstance(application).showAdIfAvailable(activity, () -> {
                        if (!activity.isFinishing() && !activity.isDestroyed()) {
                            AppLovinOpenManager.getInstance(application).showAdIfAvailable(activity, null);
                        }
                    });
                }
            }

            justShowedInterstitial = false; // Reset sekali pakai
        }

        fromGame = false; // Reset flag setiap kali masuk foreground
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        startedActivityCount--;
        if (startedActivityCount == 0) {
            // Aplikasi benar-benar diminimize / masuk ke background
            lastBackgroundTime = SystemClock.elapsedRealtime();
        }
    }

    // Lifecycle wajib Android ActivityLifecycleCallbacks
    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
    @Override public void onActivityResumed(@NonNull Activity activity) {}
    @Override public void onActivityPaused(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) {}
}