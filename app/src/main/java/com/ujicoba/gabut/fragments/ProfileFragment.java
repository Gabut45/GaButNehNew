package com.ujicoba.gabut.fragments;

import static com.ujicoba.gabut.securepref.SecurePrefManager.PREF_NAME;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.activities.SplashActivity;
import com.ujicoba.gabut.activities.TransactionsActivity;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.FragmentProfileBinding;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
import com.ujicoba.gabut.databinding.LayoutProfileDialogBinding;
import com.ujicoba.gabut.utils.AppClass;
import com.ujicoba.gabut.utils.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    LayoutProfileBinding profileBinding;
    private final String TAG = ProfileFragment.class.getSimpleName();
    private ProgressDialog progressDialog;

    // ✅ Blacklist list
    private List<String> blacklistWords = new ArrayList<>();

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        initProfile();
        initCodes();

        // ✅ Load blacklist dari server
        fetchBlacklistFromServer();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Deleting account...");
        progressDialog.setCancelable(false);

        return binding.getRoot();
    }

    private void initProfile() {
        profileBinding = LayoutProfileBinding.bind(binding.profileLayout.getRoot());
        Methods.initUserProfile(getContext(), profileBinding, "Ganti Nama Profile");
        profileBinding.profileButton.setOnClickListener(view -> {
            if (Methods.isNetworkConnected(requireContext())) {
                updateDialog();
            } else {
                Methods.showNoInternetNotification(requireContext());
            }
        });
    }

    private void initCodes() {
        binding.deleteAccountBtn.setOnClickListener(view -> showDeleteAccountConfirmation());

        binding.transaction.setOnClickListener(view ->
                Methods.gotoNextActivity(requireActivity(), TransactionsActivity.class));

        binding.contactUs.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + Methods.getData(getContext(), Data.CONTACT_EMAIL)));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Email subject");
            intent.putExtra(Intent.EXTRA_TEXT, "Email body");
            startActivity(Intent.createChooser(intent, "Send email"));
        });

        binding.privacyPolicy.setOnClickListener(view ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Methods.getData(getContext(), Data.PRIVACY_POLICY)))));

        binding.termsConditions.setOnClickListener(view ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Methods.getData(getContext(), Data.TERMS_CONDITIONS)))));

        binding.rateUs.setOnClickListener(view ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName()))));

        binding.logOut.setOnClickListener(view -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.app_name) + PREF_NAME, Context.MODE_PRIVATE);
                    sharedPreferences.edit().clear().apply();
                    Methods.gotoNextActivity(requireActivity(), SplashActivity.class);
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), getString(R.string.msg_something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showDeleteAccountConfirmation() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_confirm_delete_account);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            deleteUserAccount();
        });

        dialog.show();
    }

    private void updateDialog() {
        Dialog dialog = new Dialog(getContext());
        LayoutProfileDialogBinding dialogBinding = LayoutProfileDialogBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        layoutParams.height = 1800;
        dialog.getWindow().setAttributes(layoutParams);

        dialogBinding.blurView.setSciFiDarkOverlay();
        Glide.with(requireContext())
                .load(Methods.getData(getContext(), Data.USER_PROFILE))
                .override(256, 256)
                .placeholder(R.drawable.logo)
                .into(dialogBinding.imageView);

        dialogBinding.imageView.setOnClickListener(view -> {
            Toast.makeText(getContext(), "You can't update your photo.", Toast.LENGTH_SHORT).show();
        });

        dialogBinding.title.setText(getString(R.string.text_update_profile));
        dialogBinding.desc.setText(getString(R.string.text_update_profile_desc));
        dialogBinding.editText1.setHint(getString(R.string.enter_your_name));

        dialogBinding.btn1.setOnClickListener(view -> dialog.dismiss());

        dialogBinding.btn2.setOnClickListener(view -> {
            String inputName = dialogBinding.editText1.getText().toString().trim();
            if (inputName.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.enter_your_name), Toast.LENGTH_SHORT).show();
            } else if (!isNicknameAllowed(inputName)) {
                Toast.makeText(getContext(), "Nama tidak boleh mengandung kata terlarang ya kak.", Toast.LENGTH_LONG).show();
            } else {
                dialog.dismiss();
                Methods.showProgressDialog(getActivity(), getString(R.string.msg_please_wait));
                new Handler().postDelayed(() -> {
                    updateUserData(inputName);
                    Methods.hideProgressDialog();
                }, 1000);
            }
        });

        dialog.show();
    }

    private void updateUserData(String uname) {
        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(getContext(), Data.USER_ID));
            params.put("uname", uname);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SantaCoderJsonObjRequest updateRequest = new SantaCoderJsonObjRequest(Request.Method.POST,
                BuildConfig.MAIN_URL + "/aapi_files/api_profile.php", params,
                response -> {
                    try {
                        if (response.getString("status").equalsIgnoreCase("1")) {
                            String unameResponse = response.getString("uname");
                            Methods.setData(getContext(), Data.USER_NAME, unameResponse);
                            profileBinding.userName.setText(unameResponse);
                        }
                        Methods.showSuccessResponse(TAG, response.getString("message"), getContext());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, getContext())
        );

        AppClass.getInstance(getContext()).addToRequestQueue(updateRequest, TAG);
    }

    private void deleteUserAccount() {
        progressDialog.show();

        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(getContext(), Data.USER_ID));
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }

        SantaCoderJsonObjRequest deleteRequest = new SantaCoderJsonObjRequest(Request.Method.POST,
                BuildConfig.MAIN_URL + "/aapi_files/api_delete_account.php", params,
                response -> {
                    progressDialog.dismiss();
                    try {
                        Methods.showSuccessResponse(TAG, response.getString("message"), getContext());
                        if ("1".equalsIgnoreCase(response.getString("status"))) {
                            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.app_name) + PREF_NAME, Context.MODE_PRIVATE);
                            sharedPreferences.edit().clear().apply();
                            Methods.gotoNextActivity(requireActivity(), SplashActivity.class);
                            requireActivity().finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Methods.showErrorResponse(TAG, error, getContext());
                });

        AppClass.getInstance(getContext()).addToRequestQueue(deleteRequest, TAG);
    }

    // ✅ Fungsi untuk ambil blacklist dari server
    private void fetchBlacklistFromServer() {
        String url = BuildConfig.MAIN_URL + "/aapi_files/get_blacklist_words.php";
        SantaCoderJsonObjRequest request = new SantaCoderJsonObjRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("1")) {
                            blacklistWords.clear();
                            JSONArray arr = response.getJSONArray("blacklist");
                            for (int i = 0; i < arr.length(); i++) {
                                blacklistWords.add(arr.getString(i).toLowerCase());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, getContext())
        );
        AppClass.getInstance(getContext()).addToRequestQueue(request, TAG);
    }

    // ✅ Filter nickname berdasarkan blacklist
    private boolean isNicknameAllowed(String nickname) {
        nickname = nickname.toLowerCase();
        for (String bad : blacklistWords) {
            if (nickname.contains(bad)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        AppClass.getInstance(getContext()).cancelPendingRequests(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
