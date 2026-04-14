package com.ujicoba.gabut.utils;
//
//import android.content.Context;
//import android.util.Log;
//import android.view.View;
//import android.widget.LinearLayout;
//
//import com.santacoder.kingmoney.api.Data;
//import com.startapp.sdk.ads.banner.Banner;
//import com.startapp.sdk.ads.banner.BannerListener;
//import com.startapp.sdk.adsbase.Ad;
//import com.startapp.sdk.adsbase.StartAppAd;
//import com.startapp.sdk.adsbase.StartAppSDK;
//import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
//
//public class StartAppManager {
//    public static StartAppAd startAppAd;
//
//    public interface StartAppCallback {
//        void onAdHidden();
//    }
//    private static StartAppCallback startAppCallback;
//    public static void setStartAppCallback(StartAppCallback listener) {
//        startAppCallback = listener;
//    }
//
//
//    public static void initStartAppAds(Context ctx){
//        StartAppSDK.init(ctx, Methods.getData(ctx, Data.SI_APP_ID), false);
//        StartAppSDK.setTestAdsEnabled(false);
//        StartAppAd.disableSplash();// Hide splash ads for Start io
//    }
//
//    public static void loadStartAppBanner(Context ctx, LinearLayout bannerLyt) {
//        Banner banner = new Banner(ctx, new BannerListener() {
//            @Override
//            public void onReceiveAd(View view) {
//                bannerLyt.addView(view);
//                Log.i("checkAds", "Banner: onReceiveAd");
//            }
//
//            @Override
//            public void onFailedToReceiveAd(View view) {
//                Log.i("checkAds", "Banner: onFailedToReceiveAd");
//            }
//
//            @Override
//            public void onImpression(View view) {
//                Log.i("checkAds", "Banner: onImpression");
//            }
//
//            @Override
//            public void onClick(View view) {
//                Log.i("checkAds", "Banner: onClick");
//            }
//        });
//        banner.loadAd();
//    }
//
//    public static void loadStartAppInterstitial(Context ctx) {
//        if (startAppAd == null) {
//            startAppAd = new StartAppAd(ctx);
//            startAppAd.loadAd(StartAppAd.AdMode.AUTOMATIC);
//        } else {
//            startAppAd.loadAd(StartAppAd.AdMode.AUTOMATIC);
//        }
//    }
//
//    public static void showStartAppInterstitial(Context ctx) {
//        if (startAppAd != null && startAppAd.isReady()) {
//            startAppAd.showAd(new AdDisplayListener() {
//                @Override
//                public void adHidden(Ad ad) {
//                    Log.i("checkAds", "Interstitial: adHidden");
//                    if (startAppCallback != null) {
//                        startAppCallback.onAdHidden();
//                    }
//                }
//
//                @Override
//                public void adDisplayed(Ad ad) {
//                    Log.i("checkAds", "Interstitial: adDisplayed");
//                }
//
//                @Override
//                public void adClicked(Ad ad) {
//                    Log.i("checkAds", "Interstitial: adClicked");
//                }
//
//                @Override
//                public void adNotDisplayed(Ad ad) {
//                    Log.i("checkAds", "Interstitial: adNotDisplayed");
//                }
//            });
//        } else {
//            loadStartAppInterstitial(ctx);
//        }
//    }
//}
