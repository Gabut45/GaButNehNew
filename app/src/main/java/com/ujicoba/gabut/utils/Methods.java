package com.ujicoba.gabut.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.activities.MainActivity;
import com.ujicoba.gabut.activities.NetworkActivity;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
import com.ujicoba.gabut.noti.NotificationUtils;
import com.ujicoba.gabut.securepref.SecurePrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Methods {
    public static long shimmerTime = 2000;
    private static SantaProgressDialog progressDialog;
    private static SecurePrefManager securePrefManager;

    public static void gotoNextActivity(Activity curActivity, Class<? extends Activity> nextActivity){
        curActivity.startActivity(new Intent(curActivity, nextActivity));
        curActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public static void showProgressDialog(Activity activity, String msg){
        progressDialog = new SantaProgressDialog(activity);
        progressDialog.show();
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public static void hideProgressDialog(){
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static boolean isNetworkConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void showNoInternetNotification(Context context){
        NotificationUtils.showNotification(context, 1, "No Internet!", "Please check your internet connection!");
        Methods.gotoNextActivity((Activity) context, NetworkActivity.class);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static void initUserProfile(Context context, LayoutProfileBinding binding, String btnText){
        binding.userCoins.setText(Methods.getData(context, Data.USER_COINS));
        Glide.with(context)
                .load(Methods.getData(context, Data.USER_PROFILE))
                .placeholder(R.drawable.logo)
                .into(binding.userPicture);
        binding.userName.setText(Methods.getData(context, Data.USER_NAME));
        binding.userEmail.setText(Methods.getData(context, Data.USER_EMAIL));
        binding.profileButtonText.setText(btnText);
    }

    public static boolean isUserSigned(Context ctx){
        String userId = Methods.getData(ctx, Data.USER_ID);
        String userImg = Methods.getData(ctx, Data.USER_PROFILE);
        String userName = Methods.getData(ctx, Data.USER_NAME);
        String userEmail = Methods.getData(ctx, Data.USER_EMAIL);
        String userReferralCode = Methods.getData(ctx, Data.USER_REFERRAL_CODE);
        String userCoins = Methods.getData(ctx, Data.USER_COINS);
        return !userId.isEmpty() && !userImg.isEmpty() && !userName.isEmpty() && !userEmail.isEmpty() && !userReferralCode.isEmpty() && !userCoins.isEmpty();
    }

    public static void updateMetaData(Context context, String metaKey, String metaValue){
        ApplicationInfo appInfo = context.getApplicationInfo();
        Bundle metaData = appInfo.metaData;
        if (metaData == null) {
            metaData = new Bundle();
        }
        metaData.putString(metaKey, metaValue);
        appInfo.metaData = metaData;

        String updatedValue = metaData.getString(metaKey);
        if (updatedValue.equals(metaValue)) {
            Log.d("checkMetaData", "Meta Data Updated: " + updatedValue);
        } else {
            Log.d("checkMetaData", "Meta Data Not Updated");
        }
    }

    public static void showErrorResponse(String TAG, VolleyError volleyError, Context context){
        String message;
        if (volleyError instanceof NetworkError) {
            message = "Cannot connect to Internet...Please check your connection!";
        } else if (volleyError instanceof ServerError) {
            message = "The server could not be found. Please try again after some time!!";
        } else if (volleyError instanceof AuthFailureError) {
            message = "Cannot connect to Internet...Please check your connection!";
        } else if (volleyError instanceof ParseError) {
            message = "Parsing error! Please try again after some time!!";
        } else if (volleyError instanceof TimeoutError) {
            message = "Connection TimeOut! Please check your internet connection.";
        } else {
            message = "Something went wrong.";
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }

    public static void showSuccessResponse(String TAG, String msg, Context context){
        Log.d(TAG, msg);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void setFullScreen(Activity context) {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            Methods.setWindowFlag(context, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            context.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Methods.setWindowFlag(context, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            context.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static boolean isRooted(Context ctx) {
        boolean isRooted = false;

        String[] paths = {"/system/bin/su", "/system/xbin/su", "/sbin/su"};
        for (String path : paths) {
            if (new File(path).exists()) {
                isRooted = true;
                break;
            }
        }

        String[] packages = {"com.noshufou.android.su", "eu.chainfire.supersu"};
        for (String packageName : packages) {
            try {
                if (ctx.getPackageManager().getPackageInfo(packageName, 0) != null) {
                    isRooted = true;
                    break;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("SplashActivity", ctx.getResources().getString(R.string.msg_package_not_found));
            }
        }

        return isRooted;
    }

    public static boolean isVPNConnected(){
        List<String> networkList = new ArrayList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp())
                    networkList.add(networkInterface.getName());
            }
        } catch (Exception ex) {
            // ignore
        }
        return networkList.contains("tun0");
    }

    @SuppressLint("SetTextI18n")
    public static void showBlockPopup(Activity activity, String message, String blockUntil) {
        SantaCustomDialog blockDialog = new SantaCustomDialog(
                activity,
                "tidur",
                1200,
                R.drawable.ic_exit,
                "Waktunya Istirahat 😴",
                message,
                "null",
                "Tutup Aplikasi",
                "OK",
                "null",
                "null"
        );
        blockDialog.show();

        // Countdown waktu blokir
        TextView tvDesc = blockDialog.findViewById(R.id.desc);
        Handler handler = new Handler();

        long targetMillis = 0;
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta")); // 🕒 penting!
            targetMillis = sdf.parse(blockUntil).getTime();

        } catch (Exception e) {
            e.printStackTrace();
        }

        long finalTargetMillis = targetMillis;
        Runnable countdown = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                long diff = finalTargetMillis - System.currentTimeMillis();
                if (diff > 0) {
                    long hours = diff / (1000 * 60 * 60);
                    long minutes = (diff / (1000 * 60)) % 60;
                    long seconds = (diff / 1000) % 60;
                    tvDesc.setText(
                            message + "\nSisa waktu: " + String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    );
                    handler.postDelayed(this, 1000);
                } else {
                    blockDialog.dismiss();
                    activity.finishAffinity();
                }
            }
        };
        handler.post(countdown);

        // Tutup aplikasi jika ditekan
        blockDialog.setOnDialogButtonClickListener(() -> {
            blockDialog.dismiss();
            activity.finishAffinity();
        });
    }


    // ✅ Versi besar dan mengambang (countdown real-time)
    @SuppressLint("SetTextI18n")
    public static void showFloatingBlockCountdown(Activity activity, String message, String blockUntil) {
        Dialog dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_block_popup);
        dialog.setCancelable(false);

        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        TextView tvCountdown = dialog.findViewById(R.id.tvCountdown);
        Button btnClose = dialog.findViewById(R.id.btnCloseApp);

        tvMessage.setText(message);

        long targetMillis = 0;
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            targetMillis = sdf.parse(blockUntil).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long remaining = targetMillis - System.currentTimeMillis();
        if (remaining < 0) remaining = 0;

        Handler handler = new Handler();
        long finalTargetMillis = targetMillis;

        Runnable countdown = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                long diff = finalTargetMillis - System.currentTimeMillis();
                if (diff > 0) {
                    long hours = diff / (1000 * 60 * 60);
                    long minutes = (diff / (1000 * 60)) % 60;
                    long seconds = (diff / 1000) % 60;
                    tvCountdown.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                    handler.postDelayed(this, 1000);
                } else {
                    tvCountdown.setText("00:00:00");
                    dialog.dismiss();
                    Methods.gotoNextActivity(activity, MainActivity.class);
                    activity.finish();
                }
            }
        };
        handler.post(countdown);

        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            activity.finishAffinity();
            System.exit(0);
        });

        // Buat tampil besar dan mengambang di tengah layar
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        dialog.show();
    }




    public static void checkVpnService(Activity context){
        if (Methods.getData(context, Data.IS_VPN_BLOCKED).equalsIgnoreCase("true") && Methods.isVPNConnected()){
            SantaCustomDialog vpnDialog = new SantaCustomDialog(
                    context,
                    "internet",
                    810, R.drawable.ic_vpn,
                    context.getString(R.string.msg_vpn_title),
                    context.getString(R.string.msg_vpn_desc),
                    "0",
                    "No",
                    "OK",
                    "null",
                    "null"
            );
            vpnDialog.show();
            vpnDialog.setOnDialogButtonClickListener(context::finishAffinity);
        }
    }

    public static String textCapitalize(String text){
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static void sendRequestToServer(String TAG, Context context, int id, String from, TextView coinTextView, TextView availableSpin, String details){
        JSONObject params = new JSONObject();
        try {
            params.put("id", id);
            params.put("uid", Methods.getData(context, Data.USER_ID));
            params.put("from", from);
            if (details != null)
                params.put("details", details);
        } catch (JSONException e){
            e.printStackTrace();
        }
        SantaCoderJsonObjRequest request = new SantaCoderJsonObjRequest(Request.Method.POST, BuildConfig.MAIN_URL + "/aapi_files/api_request.php", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equalsIgnoreCase("1")){
                        Methods.setData(context, Data.USER_COINS, response.getString("user_coins"));
                        if (availableSpin != null){
                            Methods.setData(context, Data.AVAILABLE_SPIN, response.getString("available_spin"));
                            availableSpin.setText(String.format("%s %s", response.getString("available_spin"), context.getString(R.string.text_spin_available)));
                        }
                        if (coinTextView != null){
                            coinTextView.setText(response.getString("user_coins"));
                        }
                    }
                    Methods.showSuccessResponse(TAG, response.getString("message") ,context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Methods.showErrorResponse(TAG, error, context));
        AppClass.getInstance(context).addToRequestQueue(request, TAG);
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static String getUserDeviceId(Context context) {
        if (securePrefManager == null) {
            securePrefManager = new SecurePrefManager(context);
        }

        // Coba ambil ID dari penyimpanan aman terlebih dahulu
        String savedId = securePrefManager.getString("USER_DEVICE_ID");
        if (savedId != null && !savedId.trim().isEmpty()) {
            return savedId;
        }

        // Ambil Android ID bawaan sistem
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Jika gagal, buat fallback ID unik sementara
        if (androidId == null || androidId.trim().isEmpty()) {
            androidId = "device_" + System.currentTimeMillis();
        }

        // Simpan ke SecurePref agar konsisten
        securePrefManager.setString("USER_DEVICE_ID", androidId);

        return androidId;
    }


    public static String getNumberFromText(String text){
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            builder.append(matcher.group());
        }
        return builder.toString();
    }

    public static void setData(Context context, String prefKey, String value) {
        if (securePrefManager == null) {
            securePrefManager = new SecurePrefManager(context);
        }
        securePrefManager.setString(prefKey, value);
    }

    public static String getData(Context context, String prefKey) {
        if (securePrefManager == null) {
            securePrefManager = new SecurePrefManager(context);
        }
        return securePrefManager.getString(prefKey);
    }

    public static String removeSpaces(String text){
        return text.replaceAll("\\s+", "");
    }

    // --- Tambahan method showToast yang kamu butuhkan ---
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void checkUserBlockNow(Context context) {
        try {
            // 🔹 Ambil UID user dari SecurePrefManager
            String uid = Methods.getData(context, Data.USER_ID);

            // 🔹 Cek apakah UID kosong
            if (uid == null || uid.trim().isEmpty()) {
                Log.e("BlockCheck", "❌ UID kosong! Tidak bisa cek blokir. Pastikan user sudah login.");
                return;
            }

            // 🔹 Kirim ke server untuk pengecekan blokir
            JSONObject params = new JSONObject();
            params.put("uid", uid);
            Log.d("BlockCheck", "✅ Cek blokir dikirim ke server. UID: " + uid);

            SantaCoderJsonObjRequest req = new SantaCoderJsonObjRequest(
                    Request.Method.POST,
                    BuildConfig.MAIN_URL + "/aapi_files/check_activity_block.php",
                    params,
                    response -> {
                        try {
                            Log.d("BlockCheck", "Response: " + response.toString());

                            if (response.getString("status").equalsIgnoreCase("blocked")) {
                                String message = response.optString(
                                        "message",
                                        "Kamu sedang dalam waktu istirahat 😊"
                                );
                                String blockUntil = response.optString("block_until", "");
                                showBlockPopup((Activity) context, message, blockUntil);
                            }
                        } catch (JSONException e) {
                            Log.e("BlockCheck", "❌ JSON Error: " + e.getMessage());
                        }
                    },
                    error -> {
                        Log.e("BlockCheck", "❌ Error checking block: " + error.toString());
                    }
            );

            AppClass.getInstance(context).addToRequestQueue(req, "BlockCheck");

        } catch (Exception e) {
            Log.e("BlockCheck", "❌ Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
