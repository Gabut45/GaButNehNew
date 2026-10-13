package com.ujicoba.gabut.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.adapters.LeaderboardAdapter;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.FragmentLeaderboardBinding;
import com.ujicoba.gabut.models.LeaderboardModel;
import com.ujicoba.gabut.utils.AppClass;
import com.ujicoba.gabut.utils.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    FragmentLeaderboardBinding binding;
    private final String TAG = LeaderboardFragment.class.getSimpleName();
    private List<LeaderboardModel> leaderboardModelList;
    private LeaderboardAdapter leaderboardAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);

        if (Methods.isNetworkConnected(requireContext()))
            getTenLeaderboard();
        else
            Methods.showNoInternetNotification(requireContext());

        initCodes();

        // Tombol Kiwo klik listener
        binding.btnKiwo.setOnClickListener(v -> showAnimatedDialog());

        // 🔥 Tombol Tengen klik listener
        binding.btnTengen.setOnClickListener(v -> showTengenPopup());

        return binding.getRoot();
    }

    private void initCodes() {
        leaderboardModelList = new ArrayList<>();
        if (Methods.isNetworkConnected(requireContext()))
            new Handler().postDelayed(this::getAllLeaderboard, Methods.shimmerTime);
        else
            Methods.showNoInternetNotification(requireContext());

        leaderboardAdapter = new LeaderboardAdapter(leaderboardModelList, getContext());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(leaderboardAdapter);
    }

    private void getTenLeaderboard() {
        SantaCoderJsonObjRequest tenLeaderboardRequest = new SantaCoderJsonObjRequest(Request.Method.GET,
                BuildConfig.MAIN_URL + "/aapi_files/api_leaderboard_ten.php",
                null,
                response -> {
                    try {
                        if (response.getString("status").equalsIgnoreCase("1")) {
                            ImageView[] userImages = new ImageView[15];
                            for (int i = 0; i < userImages.length; i++) {
                                String imageViewId = "user" + (i + 1);
                                int resId = getResources().getIdentifier(imageViewId, "id", getContext().getPackageName());
                                userImages[i] = getView().findViewById(resId);
                            }

                            JSONArray lArray = response.getJSONArray("leaderboard");
                            for (int i = 0; i < userImages.length && i < lArray.length(); i++) {
                                JSONObject user = lArray.getJSONObject(i);
                                Glide.with(requireContext())
                                        .load(user.getString("user_picture"))
                                        .override(i == 0 ? 256 : 128, i == 0 ? 256 : 128)
                                        .placeholder(R.drawable.logo)
                                        .into(userImages[i]);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, getContext())
        );
        AppClass.getInstance(getContext()).addToRequestQueue(tenLeaderboardRequest, TAG);
    }

    private void getAllLeaderboard() {
        SantaCoderJsonObjRequest allLeaderRequest = new SantaCoderJsonObjRequest(Request.Method.GET,
                BuildConfig.MAIN_URL + "/aapi_files/api_leaderboard_all.php",
                null,
                response -> {
                    try {
                        if (response.getString("status").equalsIgnoreCase("1")) {
                            JSONArray leaderArray = response.getJSONArray("leaderboard");
                            for (int i = 0; i < leaderArray.length(); i++) {
                                JSONObject leaderObj = leaderArray.getJSONObject(i);
                                LeaderboardModel model = new LeaderboardModel(
                                        leaderObj.getString("user_picture"),
                                        leaderObj.getString("user_name"),
                                        leaderObj.getString("user_coins")
                                );
                                leaderboardModelList.add(model);
                            }
                            leaderboardAdapter.showShimmer = false;
                            leaderboardAdapter.notifyDataSetChanged();
                        } else {
                            Methods.showSuccessResponse(TAG, response.getString("message"), getContext());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, getContext())
        );
        AppClass.getInstance(getContext()).addToRequestQueue(allLeaderRequest, TAG);
    }

    private void showAnimatedDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_custom_popup, null);

        WebView webView = dialogView.findViewById(R.id.webContent);
        TextView tvEmailInfo = dialogView.findViewById(R.id.tvEmailInfo);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.gabutgame.my.id/appgabut/rank.html");

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

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // 🔥 Tambahkan metode showTengenPopup()
    private void showTengenPopup() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_custom_popup_tengen, null);

        WebView webView = dialogView.findViewById(R.id.webContent);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvEmailInfo = dialogView.findViewById(R.id.tvEmailInfo);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.gabutgame.my.id/"); // 🔧 Ganti dengan URL yang kamu inginkan!

        tvDialogTitle.setText("Daftar Penerima Hadiah GRATIS");
        tvEmailInfo.setText(Html.fromHtml(
                "Jika mengalami masalah, silahkan hubungi:<br><b>cs@gabutgame.my.id</b>",
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

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
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
