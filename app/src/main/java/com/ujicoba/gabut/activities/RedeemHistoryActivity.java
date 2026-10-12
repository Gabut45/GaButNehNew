package com.ujicoba.gabut.activities;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.adapters.RedeemHistoryAdapter;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.ActivityRedeemHistoryBinding;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
import com.ujicoba.gabut.models.RedeemHistoryModel;
import com.ujicoba.gabut.utils.AdsManager;
import com.ujicoba.gabut.utils.AppClass;
import com.ujicoba.gabut.utils.ApplovinManager;
import com.ujicoba.gabut.utils.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RedeemHistoryActivity extends AppCompatActivity {
    ActivityRedeemHistoryBinding binding;
    private final String TAG = RedeemHistoryActivity.class.getSimpleName();
    private List<RedeemHistoryModel> redeemHistoryModelList;
    private RedeemHistoryAdapter redeemHistoryAdapter;
    private String adType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRedeemHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Methods.setFullScreen(RedeemHistoryActivity.this);

        if (Methods.isNetworkConnected(this)){
            loadRedeemHistoryItems();
            initProfile();
            adType = Methods.getData(this, Data.ADS_NETWORK_NAME);
            loadAds(adType);
        } else
            Methods.showNoInternetNotification(this);
    }

    private void initProfile() {
        LayoutProfileBinding profileBinding = LayoutProfileBinding.bind(binding.layoutProfile.getRoot());
        Methods.initUserProfile(this, profileBinding, "Kembali");
        profileBinding.profileButton.setOnClickListener(view -> {
            Methods.gotoNextActivity(RedeemHistoryActivity.this, MainActivity.class);
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void loadRedeemHistoryItems() {
        redeemHistoryModelList = new ArrayList<>();
        if (Methods.isNetworkConnected(this))
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getRedeemHitoryItems();
                }
            }, Methods.shimmerTime);
        else
            Methods.showNoInternetNotification(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        redeemHistoryAdapter = new RedeemHistoryAdapter(redeemHistoryModelList);
        binding.recyclerView.setAdapter(redeemHistoryAdapter);
    }

    private void getRedeemHitoryItems() {
        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(this, Data.USER_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SantaCoderJsonObjRequest historyItemRequest = new SantaCoderJsonObjRequest(Request.Method.POST, BuildConfig.MAIN_URL + "/aapi_files/api_redeem_history.php", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equalsIgnoreCase("1")){
                        JSONArray itemArr = response.getJSONArray("redeem_history_data");
                        for (int i = 0; i < itemArr.length(); i++){
                            JSONObject itemObj = itemArr.getJSONObject(i);
                            RedeemHistoryModel model = new RedeemHistoryModel(
                                    itemObj.getString("redeem_coins"),
                                    itemObj.getString("payment_method"),
                                    itemObj.getString("payment_status"),
                                    itemObj.getString("redeem_date")
                            );
                            redeemHistoryModelList.add(model);
                        }
                        redeemHistoryAdapter.showShimmer = false;
                        redeemHistoryAdapter.notifyDataSetChanged();
                    } else {
                        Methods.showSuccessResponse(TAG, response.getString("message"), RedeemHistoryActivity.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Methods.showErrorResponse(TAG, error, RedeemHistoryActivity.this);
            }
        });
        AppClass.getInstance(this).addToRequestQueue(historyItemRequest, TAG);
    }

    private void loadAds(String type) {
        if (adType.equalsIgnoreCase("google_ads_manager")){
            AdsManager.initAds(this);
            AdsManager.loadBannerAds(this, binding.bannerLayout.bannerLayout);
            AdsManager.adManagerBannerCallback(this, binding.bannerLayout.bannerLayout);
            AdsManager.loadInterstitialAds(this);
        } else if (adType.equalsIgnoreCase("applovin")){
            ApplovinManager.initApplovin(this, binding.bannerLayout.bannerLayout);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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