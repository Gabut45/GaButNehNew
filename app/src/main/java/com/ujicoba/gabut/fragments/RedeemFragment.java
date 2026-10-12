package com.ujicoba.gabut.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.activities.RedeemHistoryActivity;
import com.ujicoba.gabut.adapters.RedeemAdapter;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.FragmentRedeemBinding;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
import com.ujicoba.gabut.databinding.LayoutRedeemDialogBinding;
import com.ujicoba.gabut.models.RedeemModel;
import com.ujicoba.gabut.utils.AppClass;
import com.ujicoba.gabut.utils.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RedeemFragment extends Fragment {
    public RedeemFragment() {
        // Required empty public constructor
    }

    private FragmentRedeemBinding binding;
    private LayoutProfileBinding profileBinding;
    private final String TAG = RedeemFragment.class.getSimpleName();
    private List<RedeemModel> redeemModelList;
    private RedeemAdapter redeemAdapter;
    private int pos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRedeemBinding.inflate(inflater, container, false);

        // 🔥 tampilkan popup Kiwo otomatis saat user buka menu Redeem
        new Handler().postDelayed(this::showAnimatedDialog, 500);

        initProfile();   // tampilkan profil di atas daftar redeem
        loadRedeemItems();

        return binding.getRoot();
    }

    private void initProfile() {
        profileBinding = binding.profileLayout;
        Methods.initUserProfile(requireActivity(), profileBinding, "Riwayat Kirim Berlian");
        profileBinding.profileButton.setOnClickListener(v -> {
            Methods.gotoNextActivity(requireActivity(), RedeemHistoryActivity.class);
        });
    }

    private void loadRedeemItems() {
        redeemModelList = new ArrayList<>();
        if (Methods.isNetworkConnected(requireContext())) {
            new Handler().postDelayed(this::getRedeemItems, Methods.shimmerTime);
        } else {
            Methods.showNoInternetNotification(requireContext());
        }

        redeemAdapter = new RedeemAdapter(redeemModelList);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerView.setAdapter(redeemAdapter);
        redeemAdapter.setOnRedeemItemClickListener(p -> {
            pos = p;
            redeemDialog();
        });
    }

    private void getRedeemItems() {
        SantaCoderJsonObjRequest redeemItemsRequest = new SantaCoderJsonObjRequest(
                Request.Method.GET,
                BuildConfig.MAIN_URL + "/aapi_files/api_redeem_store.php",
                null,
                response -> {
                    try {
                        if (response.getString("status").equalsIgnoreCase("1")) {
                            JSONArray itemsArray = response.getJSONArray("redeem_store_items");
                            for (int i = 0; i < itemsArray.length(); i++) {
                                JSONObject itemsObj = itemsArray.getJSONObject(i);
                                RedeemModel model = new RedeemModel(
                                        itemsObj.getInt("id"),
                                        itemsObj.getString("coin_image"),
                                        itemsObj.getString("redeem_coins"),
                                        itemsObj.getString("redeem_desc"),
                                        itemsObj.getString("payment_method"),
                                        itemsObj.getString("button_text")
                                );
                                redeemModelList.add(model);
                            }
                            redeemAdapter.showShimmer = false;
                            redeemAdapter.notifyDataSetChanged();
                        } else {
                            Methods.showSuccessResponse(TAG, response.getString("message"), getContext());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, getContext())
        );

        AppClass.getInstance(getContext()).addToRequestQueue(redeemItemsRequest, TAG);
    }

    private void redeemDialog() {
        Dialog dialog = new Dialog(getContext());
        LayoutRedeemDialogBinding dialogBinding = LayoutRedeemDialogBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setAttributes(layoutParams);

        WindowManager.LayoutParams layoutParams2 = dialog.getWindow().getAttributes();
        layoutParams2.height = 1100;
        dialog.getWindow().setAttributes(layoutParams2);

        dialogBinding.blurView.setSciFiDarkOverlay();
        dialogBinding.imageView.setImageResource(R.drawable.redeem_coin);
        dialogBinding.title.setText(getString(R.string.text_redeem));
        dialogBinding.desc.setText(getString(R.string.text_redeem_desc));
        dialogBinding.editText1.setHint(
                getString(R.string.text_enter) + " " +
                        getString(R.string.text_details));

        dialogBinding.btn1.setOnClickListener(view -> dialog.dismiss());

        dialogBinding.btn2.setOnClickListener(view -> {
            if (dialogBinding.editText1.getText().length() > 0 &&
                    !dialogBinding.editText1.getText().toString().equals("")) {
                if (Integer.parseInt(Methods.getData(getContext(), Data.USER_COINS)) >=
                        Integer.parseInt(redeemModelList.get(pos).getRedeem_coins())) {
                    dialog.dismiss();
                    Methods.showProgressDialog(getActivity(), getString(R.string.msg_please_wait));
                    new Handler().postDelayed(() -> {
                        Methods.sendRequestToServer(TAG, getContext(),
                                redeemModelList.get(pos).getId(),
                                getString(R.string.redeem_text),
                                null, null,
                                dialogBinding.editText1.getText().toString());
                        Methods.hideProgressDialog();
                    }, 1000);
                } else {
                    Toast.makeText(getContext(),
                            getString(R.string.text_insufficient_coins),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(),
                        "Please enter " + redeemModelList.get(pos).getPayment_method() + " details",
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // 💡 Fungsi popup Kiwo
    @SuppressLint("SetJavaScriptEnabled")
    private void showAnimatedDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_custom_popup_redeem, null);

        WebView webView = dialogView.findViewById(R.id.webContent);
        TextView tvEmailInfo = dialogView.findViewById(R.id.tvEmailInfo);
        Button btnClose = dialogView.findViewById(R.id.btnClose);
        CheckBox cbAgree = dialogView.findViewById(R.id.cbAgree); // ✅ Tambah ini

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.gabutgame.my.id/redeem_qna.html");

        tvEmailInfo.setText(Html.fromHtml(
                "Jika belum jelas atau mengalami masalah apapun, silahkan hubungi kami melalui email berikut:<br><b>cs@gabutgame.my.id</b>",
                Html.FROM_HTML_MODE_LEGACY
        ));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }

        // ✅ Tambahkan logika ini:
        btnClose.setOnClickListener(v -> {
            if (!cbAgree.isChecked()) {
                Toast.makeText(requireContext(), "Mohon centang dulu kotak persetujuan.", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();
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
