package com.ujicoba.gabut.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.utils.AdmobOpenManager;
import com.ujicoba.gabut.utils.AppClass;
import com.ujicoba.gabut.utils.AppLovinOpenManager;
import com.ujicoba.gabut.utils.Methods;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private final String TAG = SplashActivity.class.getSimpleName();
    private Dialog blockDialog;
    private CountDownTimer blockTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Methods.setFullScreen(SplashActivity.this);

        if (Methods.isNetworkConnected(SplashActivity.this)) {
            getSettings();
        } else {
            Methods.showNoInternetNotification(this);
        }
    }

    private void getSettings() {
        SantaCoderJsonObjRequest settingsRequest = new SantaCoderJsonObjRequest(
                Request.Method.GET,
                BuildConfig.MAIN_URL + "/aapi_files/api_settings.php",
                null,
                response -> {
                    try {
                        if (response.getString("status").equalsIgnoreCase("1")) {
                            JSONObject settings = response.getJSONObject("settings");
                            Methods.setData(SplashActivity.this, Data.REFER_BONUS, settings.getString("refer_points").trim());
                            Methods.setData(SplashActivity.this, Data.DAILY_SPIN_LIMIT, settings.getString("daily_spin_limit").trim());
                            Methods.setData(SplashActivity.this, Data.PRIVACY_POLICY, settings.getString("privacy_policy").trim());
                            Methods.setData(SplashActivity.this, Data.TERMS_CONDITIONS, settings.getString("terms_conditions").trim());
                            Methods.setData(SplashActivity.this, Data.REFER_DESC, settings.getString("refer_desc").trim());
                            Methods.setData(SplashActivity.this, Data.SHARE_MSG, settings.getString("share_msg").trim());
                            Methods.setData(SplashActivity.this, Data.CONTACT_EMAIL, settings.getString("contact_email").trim());
                            Methods.setData(SplashActivity.this, Data.CPA_LEAD_AFFILIATE_ID, settings.getString("cpa_lead_id").trim());
                            Methods.setData(SplashActivity.this, Data.ADS_NETWORK_NAME, settings.getString("ads_networks").trim());
                            Methods.setData(SplashActivity.this, Data.ADMOB_APP_ID, settings.getString("admob_app_id").trim());
                            Methods.setData(SplashActivity.this, Data.ADMOB_BANNER_ID, settings.getString("admob_banner_id").trim());
                            Methods.setData(SplashActivity.this, Data.ADMOB_INTERSTITIAL_ID, settings.getString("admob_interstitial_id").trim());
                            Methods.setData(SplashActivity.this, Data.ADMOB_REWARDED_ID, settings.getString("admob_rewarded_id").trim());
                            Methods.setData(SplashActivity.this, Data.ADMOB_OPEN_AD_ID, settings.getString("admob_open_ad_id").trim());
                            Methods.setData(SplashActivity.this, Data.GAM_APP_ID, settings.getString("am_app_id").trim());
                            Methods.setData(SplashActivity.this, Data.GAM_BANNER_ID, settings.getString("am_banner_id").trim());
                            Methods.setData(SplashActivity.this, Data.GAM_INTERSTITIAL_ID, settings.getString("am_interstitial_id").trim());
                            Methods.setData(SplashActivity.this, Data.GAM_REWARDED_ID, settings.getString("am_rewarded_id").trim());
                            Methods.setData(SplashActivity.this, Data.GAM_OPEN_AD_ID, settings.getString("am_open_ad_id").trim());
                            Methods.setData(SplashActivity.this, Data.SI_APP_ID, settings.getString("si_app_id").trim());
                            Methods.setData(SplashActivity.this, Data.AL_SDK_KEY, settings.getString("al_sdk_key").trim());
                            Methods.setData(SplashActivity.this, Data.AL_BANNER_ID, settings.getString("al_banner_id").trim());
                            Methods.setData(SplashActivity.this, Data.AL_INTERSTITIAL_ID, settings.getString("al_interstitial_id").trim());
                            Methods.setData(SplashActivity.this, Data.AL_REWARDED_ID, settings.getString("al_rewarded_id").trim());
                            Methods.setData(SplashActivity.this, Data.AL_OPEN_AD_ID, settings.getString("al_open_ad_id").trim());
                            Methods.setData(SplashActivity.this, Data.FACEBOOK_URL, settings.getString("facebook_url").trim());
                            Methods.setData(SplashActivity.this, Data.INSTAGRAM_URL, settings.getString("instagram_url").trim());
                            Methods.setData(SplashActivity.this, Data.TWITTER_URL, settings.getString("twitter_url").trim());
                            Methods.setData(SplashActivity.this, Data.YOUTUBE_URL, settings.getString("youtube_url").trim());
                            Methods.setData(SplashActivity.this, Data.IS_ROOTED_DEVICE_BLOCKED, settings.getString("is_rooted_device_block").trim());
                            Methods.setData(SplashActivity.this, Data.IS_EMULATOR_BLOCKED, settings.getString("is_emulator_block").trim());
                            Methods.setData(SplashActivity.this, Data.IS_VPN_BLOCKED, settings.getString("is_vpn_block").trim());
                            Methods.setData(SplashActivity.this, Data.IS_FORCE_UPDATE_ENABLED, settings.getString("is_force_update").trim());
                            Methods.setData(SplashActivity.this, Data.IS_DEMO_LOGIN, settings.getString("is_demo_login").trim());
                            Methods.setData(SplashActivity.this, Data.APP_VERSION_CODE, settings.getString("app_version_code").trim());

                            // Cek Root (Jika aktif)
                            if (Methods.getData(SplashActivity.this, Data.IS_ROOTED_DEVICE_BLOCKED).equalsIgnoreCase("true") && Methods.isRooted(SplashActivity.this)) {
                                Toast.makeText(SplashActivity.this, getString(R.string.msg_rooted_device), Toast.LENGTH_SHORT).show();
                                finishAffinity();
                                return;
                            }

                            // Cek Update Paksa
                            if (Methods.getData(SplashActivity.this, Data.IS_FORCE_UPDATE_ENABLED).equalsIgnoreCase("true")
                                    && !Methods.getData(SplashActivity.this, Data.APP_VERSION_CODE).equalsIgnoreCase(String.valueOf(BuildConfig.VERSION_CODE))) {
                                Methods.showProgressDialog(SplashActivity.this, "Memeriksa pembaruan...");
                                new Handler().postDelayed(() -> {
                                    Methods.hideProgressDialog();
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                                    finish();
                                }, 1500);
                                return;
                            }

                            // Lanjut ke navigasi utama
                            gotoNextActivity();

                        } else {
                            Toast.makeText(SplashActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            gotoNextActivity();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        gotoNextActivity();
                    }
                },
                error -> {
                    Methods.showErrorResponse(TAG, error, SplashActivity.this);
                    // Tetap beri akses masuk jika server timeout
                    gotoNextActivity();
                }
        );

        AppClass.getInstance(this).addToRequestQueue(settingsRequest, TAG);
    }

    // Menjalankan navigasi & menampilkan App Open Ads secara aman
    private void gotoNextActivity() {
        if (isFinishing() || isDestroyed()) return;

        if (Methods.isUserSigned(this)) {
            String adsNetwork = Methods.getData(this, Data.ADS_NETWORK_NAME);
            if (adsNetwork == null) adsNetwork = "";
            adsNetwork = adsNetwork.toLowerCase();

            if (adsNetwork.equals("admob")) {
                AdmobOpenManager.getInstance(getApplication())
                        .showAdIfAvailable(this, this::checkUserBlockStatus);
            } else if (adsNetwork.equals("applovin")) {
                AppLovinOpenManager.getInstance(getApplication())
                        .showAdIfAvailable(this, this::checkUserBlockStatus);
            } else {
                checkUserBlockStatus();
            }
        } else {
            Methods.gotoNextActivity(this, PermissionActivity.class);
            finish();
        }
    }

    // Cek status blokir aktivitas harian dari server
    private void checkUserBlockStatus() {
        if (isFinishing() || isDestroyed()) return;

        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(this, Data.USER_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SantaCoderJsonObjRequest checkReq = new SantaCoderJsonObjRequest(
                Request.Method.POST,
                BuildConfig.MAIN_URL + "/aapi_files/check_activity_block.php",
                params,
                response -> {
                    try {
                        if (response.getString("status").equalsIgnoreCase("blocked")) {
                            String message = response.optString("message", "Kamu sudah bermain cukup lama hari ini.");
                            String blockUntil = response.optString("block_until", "");
                            showBlockCountdownDialog(message, blockUntil);
                        } else {
                            getUData();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getUData();
                    }
                },
                error -> getUData()
        );

        AppClass.getInstance(this).addToRequestQueue(checkReq, TAG);
    }

    // Ambil data user
    private void getUData() {
        if (isFinishing() || isDestroyed()) return;

        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(this, Data.USER_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SantaCoderJsonObjRequest uRequest = new SantaCoderJsonObjRequest(
                Request.Method.POST,
                BuildConfig.MAIN_URL + "/aapi_files/api_users_data.php",
                params,
                response -> {
                    try {
                        if (response.getString("status").equalsIgnoreCase("1")) {
                            JSONObject uObj = response.getJSONObject("user_data");
                            saveUInfo(uObj);
                            Methods.gotoNextActivity(this, MainActivity.class);
                            finish();
                        } else {
                            Methods.gotoNextActivity(this, MainActivity.class);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Methods.gotoNextActivity(this, MainActivity.class);
                        finish();
                    }
                },
                error -> {
                    Methods.gotoNextActivity(this, MainActivity.class);
                    finish();
                }
        );

        AppClass.getInstance(this).addToRequestQueue(uRequest, TAG);
    }

    private void saveUInfo(JSONObject user_info) {
        try {
            Methods.setData(this, Data.USER_ID, user_info.getString("user_id").trim());
            Methods.setData(this, Data.USER_PROFILE, user_info.getString("user_picture").trim());
            Methods.setData(this, Data.USER_NAME, user_info.getString("user_name").trim());
            Methods.setData(this, Data.USER_BLOCKED, user_info.getString("user_blocked").trim());
            Methods.setData(this, Data.USER_TOTAL_REFERRAL, user_info.getString("user_total_refer").trim());
            Methods.setData(this, Data.USER_COINS, user_info.getString("user_coins").trim());
            Methods.setData(this, Data.AVAILABLE_SPIN, user_info.getString("available_spin").trim());
            Methods.setData(this, Data.LAST_SPIN_TIME, user_info.getString("last_spin_time").trim());
            Methods.setData(this, Data.USER_COUNTRY_CODE, user_info.getString("user_country").trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Pop-up countdown jeda bermain
    private void showBlockCountdownDialog(String message, String blockUntil) {
        if (isFinishing() || isDestroyed()) return;

        blockDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        blockDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        blockDialog.setContentView(R.layout.dialog_block_popup);
        blockDialog.setCancelable(false);

        TextView tvMessage = blockDialog.findViewById(R.id.tvMessage);
        TextView tvCountdown = blockDialog.findViewById(R.id.tvCountdown);
        Button btnClose = blockDialog.findViewById(R.id.btnCloseApp);
        tvMessage.setText(message);

        long remainingMillis = 0;
        try {
            @SuppressLint("SimpleDateFormat")
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long targetTime = sdf.parse(blockUntil).getTime();
            remainingMillis = targetTime - System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (remainingMillis < 0) remainingMillis = 0;

        blockTimer = new CountDownTimer(remainingMillis, 1000) {
            @SuppressLint("DefaultLocale")
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / (1000 * 60 * 60);
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                long seconds = (millisUntilFinished / (1000 * 60)) % 60;
                tvCountdown.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }

            public void onFinish() {
                tvCountdown.setText("00:00:00");
                if (blockDialog != null && blockDialog.isShowing()) {
                    blockDialog.dismiss();
                }
                Methods.gotoNextActivity(SplashActivity.this, MainActivity.class);
                finish();
            }
        }.start();

        btnClose.setOnClickListener(v -> {
            if (blockDialog != null && blockDialog.isShowing()) {
                blockDialog.dismiss();
            }
            finishAffinity(); // Keluar aplikasi secara bersih tanpa System.exit
        });

        Window window = blockDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        blockDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppClass.getInstance(this).cancelPendingRequests(TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (blockTimer != null) {
            blockTimer.cancel();
            blockTimer = null;
        }
        if (blockDialog != null && blockDialog.isShowing()) {
            blockDialog.dismiss();
            blockDialog = null;
        }
    }
}