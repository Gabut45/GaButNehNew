package com.ujicoba.gabut.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.adapters.TransAdapter;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.ActivityTransactionsBinding;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
import com.ujicoba.gabut.models.TransModel;
import com.ujicoba.gabut.utils.AdsManager;
import com.ujicoba.gabut.utils.AdmobManager;
import com.ujicoba.gabut.utils.AppClass;
import com.ujicoba.gabut.utils.ApplovinManager;
import com.ujicoba.gabut.utils.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {
    ActivityTransactionsBinding binding;
    private final String TAG = TransactionsActivity.class.getSimpleName();
    private List<TransModel> transModelList;
    private TransAdapter transAdapter;
    private String adType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Methods.setFullScreen(TransactionsActivity.this);

        if (Methods.isNetworkConnected(this)) {
            loadTransactionItems();
            initProfile();
            adType = Methods.getData(this, Data.ADS_NETWORK_NAME);
            loadAds(adType);
        } else
            Methods.showNoInternetNotification(this);
    }

    private void initProfile() {
        LayoutProfileBinding profileBinding = LayoutProfileBinding.bind(binding.layoutProfile.getRoot());
        Methods.initUserProfile(TransactionsActivity.this, profileBinding, "Kembali");
        profileBinding.profileButton.setOnClickListener(view -> {
            Methods.gotoNextActivity(TransactionsActivity.this, MainActivity.class);
        });
    }

    private void loadTransactionItems() {
        transModelList = new ArrayList<>();
        if (Methods.isNetworkConnected(TransactionsActivity.this)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getTransactionItems();
                }
            }, Methods.shimmerTime);
        } else
            Methods.showNoInternetNotification(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(TransactionsActivity.this));
        transAdapter = new TransAdapter(transModelList);
        binding.recyclerView.setAdapter(transAdapter);
    }

    private void getTransactionItems() {
        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(this, Data.USER_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SantaCoderJsonObjRequest transRequest = new SantaCoderJsonObjRequest(Request.Method.POST, BuildConfig.MAIN_URL + "/aapi_files/api_trans.php", params, response -> {
            try {
                if (response.getString("status").equalsIgnoreCase("1")){
                    JSONArray transArray = response.getJSONArray("trans_data");
                    for (int i = 0; i < transArray.length(); i++){
                        JSONObject transObj = transArray.getJSONObject(i);
                        TransModel model = new TransModel(
                                transObj.getString("transaction_title"),
                                transObj.getString("transaction_coins"),
                                transObj.getString("transaction_date")
                        );
                        transModelList.add(model);
                    }
                    transAdapter.showShimmer = false;
                    transAdapter.notifyDataSetChanged();
                } else {
                    Methods.showSuccessResponse(TAG, response.getString("message"), TransactionsActivity.this);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Methods.showErrorResponse(TAG, error, TransactionsActivity.this);
            }
        });
        AppClass.getInstance(TransactionsActivity.this).addToRequestQueue(transRequest, TAG);
    }

    private void loadAds(String type) {
        if (adType.equalsIgnoreCase("google_ads_manager")){
            AdsManager.initAds(this);
            AdsManager.loadBannerAds(this, binding.bannerLayout.bannerLayout);
            AdsManager.adManagerBannerCallback(this, binding.bannerLayout.bannerLayout);
            AdsManager.loadInterstitialAds(this);
        } else if (type.equalsIgnoreCase("admob")) {
            AdmobManager.initAdmob(this, binding.bannerLayout.bannerLayout);
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