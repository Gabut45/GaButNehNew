package com.ujicoba.gabut.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.ActivityLoginBinding;
import com.ujicoba.gabut.databinding.LayoutReferDialogBinding;
import com.ujicoba.gabut.utils.AppClass;
import com.ujicoba.gabut.utils.Methods;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    private final String TAG = LoginActivity.class.getSimpleName();
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Methods.setFullScreen(this);

        // Tampilkan teks HTML dan aktifkan link
        binding.tvKebijakan.setText(
                Html.fromHtml(getString(R.string.teks_login), Html.FROM_HTML_MODE_LEGACY)
        );
        binding.tvKebijakan.setMovementMethod(LinkMovementMethod.getInstance());

        if (Methods.isNetworkConnected(this)) {
            initCodes();
            demoLogin();
        } else {
            Methods.showNoInternetNotification(this);
        }
    }

    private void demoLogin() {
        if (Methods.getData(this, Data.IS_DEMO_LOGIN).equalsIgnoreCase("true")) {
            binding.demoLogin.setOnClickListener(view -> {
                if (!binding.checkboxAgree.isChecked()) {
                    Toast.makeText(this, "Silakan centang persetujuan terlebih dahulu.", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendTokenToServer("", true);
            });
        } else {
            binding.demoLogin.setVisibility(View.GONE);
            binding.demoLogin.setClickable(false);
            binding.demoLogin.setEnabled(false);
        }
    }

    private void initCodes() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.signInButton.setOnClickListener(view -> {
            if (!binding.checkboxAgree.isChecked()) {
                Toast.makeText(this, "Silakan centang persetujuan terlebih dahulu.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    });

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            if (Methods.isNetworkConnected(this)) {
                sendTokenToServer(idToken, false);
            } else {
                Toast.makeText(this, R.string.msg_no_internet, Toast.LENGTH_SHORT).show();
                Methods.showNoInternetNotification(this);
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void saveUserInfo(JSONObject user_info) {
        try {
            Methods.setData(this, Data.USER_ID, user_info.getString("user_id").trim());
            Methods.setData(this, Data.USER_PROFILE, user_info.getString("user_picture").trim());
            Methods.setData(this, Data.USER_NAME, user_info.getString("user_name").trim());
            Methods.setData(this, Data.USER_EMAIL, user_info.getString("user_email").trim());
            Methods.setData(this, Data.USER_BLOCKED, user_info.getString("user_blocked").trim());
            Methods.setData(this, Data.USER_REFERRAL_CODE, user_info.getString("user_refer_code").trim());
            Methods.setData(this, Data.USER_TOTAL_REFERRAL, user_info.getString("user_total_refer").trim());
            Methods.setData(this, Data.USER_COINS, user_info.getString("user_coins").trim());
            Methods.setData(this, Data.AVAILABLE_SPIN, user_info.getString("available_spin").trim());
            Methods.setData(this, Data.USER_COUNTRY_CODE, user_info.getString("user_country").trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendTokenToServer(String t, boolean isDemoLogin) {
        JSONObject params = new JSONObject();
        try {
            if (isDemoLogin) {
                params.put("demo_login", "true");
            } else {
                params.put("id_token", t);
                params.put("user_device_id", Methods.getUserDeviceId(this));
                params.put("user_token", Methods.getData(LoginActivity.this, Data.USER_TOKEN));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SantaCoderJsonObjRequest jsonObjRequest = new SantaCoderJsonObjRequest(
                Request.Method.POST,
                BuildConfig.MAIN_URL + "/aapi_files/api_register.php",
                params,
                response -> {
                    try {
                        String status = response.getString("status");
                        String msg = response.getString("message");
                        if (status.equalsIgnoreCase("user_exists")) {
                            JSONObject user_info = response.getJSONObject("user_info");
                            saveUserInfo(user_info);
                            Methods.gotoNextActivity(LoginActivity.this, MainActivity.class);
                            finish();
                        } else if (status.equalsIgnoreCase("new_user")) {
                            JSONObject user_info = response.getJSONObject("user_info");
                            saveUserInfo(user_info);
                            referDialog();
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, LoginActivity.this)
        );

        AppClass.getInstance(this).addToRequestQueue(jsonObjRequest, TAG);
    }

    private void referDialog() {
        Dialog dialog = new Dialog(this);
        LayoutReferDialogBinding dialogBinding = LayoutReferDialogBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setAttributes(layoutParams);

        dialogBinding.btn1.setOnClickListener(view -> {
            Methods.gotoNextActivity(LoginActivity.this, MainActivity.class);
            finish();
            dialog.dismiss();
        });

        dialogBinding.btn2.setOnClickListener(view -> {
            if (dialogBinding.editText1.getText().length() > 0) {
                submitRefer(dialogBinding.editText1.getText().toString());
            } else {
                Toast.makeText(this, getString(R.string.msg_please_enter_refferal_code), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void submitRefer(String rc) {
        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(this, Data.USER_ID));
            params.put("urw", rc);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SantaCoderJsonObjRequest referRequest = new SantaCoderJsonObjRequest(
                Request.Method.POST,
                BuildConfig.MAIN_URL + "/aapi_files/api_refer.php",
                params,
                response -> {
                    try {
                        if (response.getString("status").equalsIgnoreCase("1")) {
                            Methods.gotoNextActivity(LoginActivity.this, MainActivity.class);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, LoginActivity.this)
        );

        AppClass.getInstance(this).addToRequestQueue(referRequest, TAG);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppClass.getInstance(this).cancelPendingRequests(TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
