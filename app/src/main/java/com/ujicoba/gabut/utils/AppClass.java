package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.ujicoba.gabut.api.Data;

public class AppClass extends Application {
    private static AppClass instance;
    private RequestQueue requestQueue;
    private Activity currentActivity;
    private AppOpenObserver appOpenObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // ✅ Update metadata ID iklan dari server
        Methods.updateMetaData(getApplicationContext(),
                "com.google.android.gms.ads.APPLICATION_ID",
                Methods.getData(getApplicationContext(), Data.GAM_APP_ID));
        Methods.updateMetaData(getApplicationContext(),
                "applovin.sdk.key",
                Methods.getData(getApplicationContext(), Data.AL_SDK_KEY));

        // ✅ Preload Open Ads
        AdmobOpenManager.getInstance(this);
        AppLovinOpenManager.getInstance(this);

        // ✅ Init AppOpenObserver
        appOpenObserver = new AppOpenObserver(this);
    }

    public static synchronized AppClass getInstance(Context context) {
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

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public AppOpenObserver getAppOpenObserver() {
        return appOpenObserver;
    }

    // ✅ Update currentActivity secara global
    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }
}
