package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.ujicoba.gabut.api.Data;

import java.lang.ref.WeakReference;

public class AppClass extends Application {

    private static AppClass instance;
    private RequestQueue requestQueue;
    private WeakReference<Activity> currentActivityRef; // ✅ Aman dari Memory Leak
    private AppOpenObserver appOpenObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // ✅ Cegah crash: Hanya update metadata jika datanya memang valid/ada dari server
        String gamAppId = Methods.getData(this, Data.GAM_APP_ID);
        if (gamAppId != null && !gamAppId.trim().isEmpty()) {
            Methods.updateMetaData(this, "com.google.android.gms.ads.APPLICATION_ID", gamAppId);
        }

        String alKey = Methods.getData(this, Data.AL_SDK_KEY);
        if (alKey != null && !alKey.trim().isEmpty()) {
            Methods.updateMetaData(this, "applovin.sdk.key", alKey);
        }

        // ✅ Otomatis pantau Activity aktif secara aman tanpa memory leak
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                if (currentActivityRef != null && currentActivityRef.get() == activity) {
                    currentActivityRef.clear();
                }
            }

            @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
            @Override public void onActivityStarted(@NonNull Activity activity) {}
            @Override public void onActivityStopped(@NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
            @Override public void onActivityDestroyed(@NonNull Activity activity) {
                if (currentActivityRef != null && currentActivityRef.get() == activity) {
                    currentActivityRef.clear();
                }
            }
        });

        // ✅ Inisialisasi AppOpenObserver
        appOpenObserver = new AppOpenObserver(this);
    }

    /**
     * Singleton instance tanpa butuh parameter Context
     */
    public static synchronized AppClass getInstance() {
        return instance;
    }

    public static AppClass getInstance(Context context) {
        return (AppClass) context.getApplicationContext();
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }

    /**
     * Dapatkan activity yang sedang aktif di layar (bisa null jika app di background)
     */
    public Activity getCurrentActivity() {
        return currentActivityRef != null ? currentActivityRef.get() : null;
    }

    public AppOpenObserver getAppOpenObserver() {
        return appOpenObserver;
    }
    // ✅ Tambahkan method ini agar kompatibel dengan GameListActivity dan activity lainnya
    public void setCurrentActivity(Activity activity) {
        if (activity != null) {
            this.currentActivityRef = new java.lang.ref.WeakReference<>(activity);
        } else if (this.currentActivityRef != null) {
            this.currentActivityRef.clear();
        }
    }
}