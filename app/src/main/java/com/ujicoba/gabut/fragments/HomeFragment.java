// ✅ Final HomeFragment.java - Integrasi penuh dengan AppOpenObserver
package com.ujicoba.gabut.fragments;

import static androidx.lifecycle.AndroidViewModel_androidKt.getApplication;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.ujicoba.gabut.BottomMarginDecoration;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.activities.GameActivity;
import com.ujicoba.gabut.activities.SpinActivity;
import com.ujicoba.gabut.activities.TaskActivity;
import com.ujicoba.gabut.adapters.GameCategoryAdapter;
import com.ujicoba.gabut.adapters.HomeSliderAdapter;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.FragmentHomeBinding;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
import com.ujicoba.gabut.models.GameCategory;
import com.ujicoba.gabut.models.GamesModel;
import com.ujicoba.gabut.models.HomeSliderModel;
import com.ujicoba.gabut.utils.AdmobManager;
import com.ujicoba.gabut.utils.AdsManager;
import com.ujicoba.gabut.utils.AppClass;
import com.ujicoba.gabut.utils.ApplovinManager;
import com.ujicoba.gabut.utils.AppOpenObserver;
import com.ujicoba.gabut.utils.Methods;
import com.ujicoba.gabut.utils.SantaCustomDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements AdsManager.AdsManagerCallback, ApplovinManager.ApplovinManagerCallback {

    FragmentHomeBinding binding;
    private final String TAG = HomeFragment.class.getSimpleName();
    private List<HomeSliderModel> homeSliderModelList;
    private HomeSliderAdapter homeSliderAdapter;
    private Handler homeSliderHandler = new Handler();
    private int defaultSliderItem = 0;
    private int homeSliderTime = 3000;
    private GameCategoryAdapter categoryAdapter;
    private List<GameCategory> categories;
    private List<GamesModel> gamesModelList;
    private SantaCustomDialog coinDialog;
    private SantaCustomDialog insufficentDialog;
    private boolean isGameStarted = false;
    private int catPos;
    private int gamePos;
    private String adType;
    private long gameStartTimeMillis;
    private long requiredPlayMillis;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        initProfile();
        loadCarousel();
        initClickListener();
        adType = Methods.getData(getContext(), Data.ADS_NETWORK_NAME);
        loadAds(adType);
        loadGames();
        return binding.getRoot();
    }

    private void initClickListener() {
        binding.spinBtn.setOnClickListener(view -> Methods.gotoNextActivity(requireActivity(), SpinActivity.class));
        binding.gameBtn.setOnClickListener(view -> Methods.gotoNextActivity(requireActivity(), GameActivity.class));
        binding.taskBtn.setOnClickListener(view -> Methods.gotoNextActivity(requireActivity(), TaskActivity.class));
        binding.viewAllBtn.setOnClickListener(view -> Methods.gotoNextActivity(requireActivity(), GameActivity.class));
    }

    private void initProfile() {
        LayoutProfileBinding profileBinding = LayoutProfileBinding.bind(binding.layoutProfile.getRoot());
        int newHeightInPixels = (int) (80 * getResources().getDisplayMetrics().density);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, newHeightInPixels);
        profileBinding.relativeLayout.setLayoutParams(layoutParams);
        Methods.initUserProfile(getContext(), profileBinding, "Daily Check-In");
        profileBinding.profileButton.setVisibility(View.GONE);
        profileBinding.nameLayout.setVisibility(View.GONE);
        profileBinding.divider.setVisibility(View.GONE);
        profileBinding.emailLayout.setVisibility(View.GONE);
        binding.checkInBtn.setOnClickListener(view -> {
            if (Methods.isNetworkConnected(requireContext())) {
                dailyCheckIn(profileBinding.userCoins);
            } else {
                Methods.showNoInternetNotification(requireContext());
            }
        });
        profileBinding.userPicture.setOnClickListener(view -> loadFragment(new ProfileFragment(), true));
    }

    public void loadFragment(Fragment fragment, boolean isReplace) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (isReplace) ft.replace(R.id.container, fragment);
        else ft.add(R.id.container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void loadCarousel() {
        homeSliderModelList = new ArrayList<>();
        getSliderItems();
        homeSliderAdapter = new HomeSliderAdapter(homeSliderModelList, binding.viewPager2);
        binding.viewPager2.setAdapter(homeSliderAdapter);
        binding.viewPager2.setOffscreenPageLimit(3);
        binding.viewPager2.setClipChildren(false);
        binding.viewPager2.setClipToPadding(false);
        binding.viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.14f);
        });
        binding.viewPager2.setPageTransformer(transformer);
        binding.viewPager2.setCurrentItem(defaultSliderItem);
        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                homeSliderHandler.removeCallbacks(homeSliderRunnable);
                homeSliderHandler.postDelayed(homeSliderRunnable, homeSliderTime);
            }
        });
    }

    private final Runnable homeSliderRunnable = () -> binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);

    private void getSliderItems() {
        SantaCoderJsonObjRequest sliderRequest = new SantaCoderJsonObjRequest(Request.Method.GET, BuildConfig.MAIN_URL + "/aapi_files/api_sliders.php", null, response -> {
            try {
                if (response.getString("status").equalsIgnoreCase("1")) {
                    JSONArray sliderArray = response.getJSONArray("sliders_data");
                    for (int i = 0; i < sliderArray.length(); i++) {
                        JSONObject sliderObj = sliderArray.getJSONObject(i);
                        homeSliderModelList.add(new HomeSliderModel(sliderObj.getString("slider_image"), sliderObj.getString("slider_link")));
                    }
                    homeSliderAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) { e.printStackTrace(); }
        }, error -> Methods.showErrorResponse(TAG, error, getContext()));
        AppClass.getInstance(getContext()).addToRequestQueue(sliderRequest, TAG);
    }

    private void loadGames() {
        categories = new ArrayList<>();
        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(getContext(), Data.USER_ID));
        } catch (JSONException e) { e.printStackTrace(); }

        SantaCoderJsonObjRequest gamesRequest = new SantaCoderJsonObjRequest(
                Request.Method.POST,
                BuildConfig.MAIN_URL + "/aapi_files/api_games.php",
                params,
                response -> {
                    try {
                        JSONArray catArray = response.getJSONArray("categories");
                        for (int i = 0; i < catArray.length(); i++) {
                            JSONObject categoryObject = catArray.getJSONObject(i);
                            JSONArray gamesArray = categoryObject.getJSONArray("games");
                            gamesModelList = new ArrayList<>();

                            for (int j = 0; j < gamesArray.length(); j++) {
                                JSONObject gamesObj = gamesArray.getJSONObject(j);
                                gamesModelList.add(new GamesModel(
                                        gamesObj.getInt("id"),
                                        gamesObj.getInt("cat_id"),
                                        gamesObj.getString("game_logo"),
                                        gamesObj.getString("game_name"),
                                        gamesObj.getString("game_coin"),
                                        gamesObj.getString("game_play_time"),
                                        gamesObj.getString("game_link"),
                                        gamesObj.optString("game_cost", "0")
                                ));
                            }

                            categories.add(new GameCategory(
                                    categoryObject.getInt("id"),
                                    categoryObject.getString("cat_name"),
                                    gamesModelList
                            ));
                        }

                        categoryAdapter = new GameCategoryAdapter(categories, true);
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.recyclerView.setAdapter(categoryAdapter);
                        binding.recyclerView.addItemDecoration(new BottomMarginDecoration(250));

                        categoryAdapter.setOnGameItemClickListener((url, cPos, gPos) -> {
                            catPos = cPos;
                            gamePos = gPos;
                            GamesModel game = categories.get(catPos).getGamesModelLIst().get(gamePos);

                            int cost = 0;
                            try { cost = Integer.parseInt(game.getGameCost()); } catch (Exception ignored) {}

                            if (cost > 0) {
                                showPlayConfirmationDialog(game);
                            } else {
                                requiredPlayMillis = Long.parseLong(game.getGamePlayTime()) * 60 * 1000;
                                gameStartTimeMillis = System.currentTimeMillis();
                                isGameStarted = true;
                                startGame(game.getGameLink());
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, getContext())
        );

        AppClass.getInstance(getContext()).addToRequestQueue(gamesRequest, TAG);
    }

    private void showPlayConfirmationDialog(GamesModel game) {
        String message = "Untuk memainkan <b>\"" + game.getGameName() + "\"</b><br><br>" +
                "💎 Biaya bermain: <b>" + game.getGameCost() + " berlian</b><br>" +
                "⏱️ Durasi minimal: <b>" + game.getGamePlayTime() + " menit</b><br>" +
                "🎁 Hadiah jika berhasil: <b>+" + game.getGameCoin() + " berlian</b><br><br>" +
                "⛔ <b>PENTING: Biaya <b>" + game.getGameCost() + " berlian akan hangus jika Anda keluar atau durasi bermain kurang dari " + game.getGamePlayTime() + " menit</b><br><br>" +
                "Apakah kamu ingin melanjutkan bermain?";

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Bermain Game")
                .setMessage(android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_LEGACY))
                .setCancelable(false)
                .setPositiveButton("LANJUTKAN", (dialog, which) -> {
                    dialog.dismiss();
                    startGameWithCost(game);
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void startGameWithCost(GamesModel game) {
        String userId = Methods.getData(requireContext(), Data.USER_ID);
        String url = BuildConfig.MAIN_URL + "/aapi_files/api_start_game.php?user_id=" + userId + "&game_id=" + game.getId();

        android.app.ProgressDialog progress = new android.app.ProgressDialog(requireContext());
        progress.setMessage("Memproses biaya bermain...");
        progress.setCancelable(false);
        progress.show();

        SantaCoderJsonObjRequest request = new SantaCoderJsonObjRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progress.dismiss();
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        Methods.showToast(requireContext(), message);

                        if (status.equals("success")) {
                            requiredPlayMillis = Long.parseLong(game.getGamePlayTime()) * 60 * 1000;
                            gameStartTimeMillis = System.currentTimeMillis();
                            isGameStarted = true;
                            startGame(game.getGameLink());
                        }
                    } catch (JSONException e) {
                        Methods.showToast(requireContext(), "Kesalahan data dari server");
                    }
                },
                error -> {
                    progress.dismiss();
                    Methods.showToast(requireContext(), "Gagal menghubungi server");
                }
        );

        AppClass.getInstance(requireContext()).addToRequestQueue(request, TAG);
    }



    private void startGame(String url) {
        // ✅ Tandai kalau ini buka game
        AppClass.getInstance(requireContext()).getAppOpenObserver().markFromGame();

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.ngisor));
        builder.setUrlBarHidingEnabled(true);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
    }

    @Override
    public void onResume() {
        super.onResume();

        AppClass.getInstance(requireContext()).setCurrentActivity(requireActivity());

        homeSliderHandler.postDelayed(homeSliderRunnable, homeSliderTime);
        if (isGameStarted) {
            isGameStarted = false;
            long elapsedTime = System.currentTimeMillis() - gameStartTimeMillis;
            if (elapsedTime >= requiredPlayMillis) {
                showCoinDialog();
            } else {
                showInsufficientDialog((requiredPlayMillis - elapsedTime) / 1000);
            }
        }
    }


    private void showCoinDialog() {
        Methods.sendRequestToServer(
                TAG,
                getContext(),
                categories.get(catPos).getGamesModelLIst().get(gamePos).getId(),
                getString(R.string.game_text),
                binding.layoutProfile.userCoins,
                null,
                null
        );

        coinDialog = new SantaCustomDialog(
                getContext(),
                "cointanpaiklan",
                1200,
                R.drawable.ic_no_internet,
                getString(R.string.msg_congratulations),
                getString(R.string.msg_you_got) + " " +
                        categories.get(catPos).getGamesModelLIst().get(gamePos).getGameCoin() + " " +
                        getString(R.string.text_coins),
                categories.get(catPos).getGamesModelLIst().get(gamePos).getGameCoin(),
                getString(R.string.btn_ok_thanks),
                getString(R.string.btn_ok_thanks),
                "Enter Referral Code",
                "null"
        );

        coinDialog.setOnDialogButtonClickListener(() -> {
            coinDialog.dismiss();

            if (adType.equalsIgnoreCase("google_ads_manager")) {
                AdsManager.loadInterstitialAds(requireActivity());
                AdsManager.showInterstitialAds(requireActivity());
                ((AppClass) requireActivity().getApplication())
                        .getAppOpenObserver()
                        .markInterstitialShown();
            } else if (adType.equalsIgnoreCase("admob")) {
                AdmobManager.loadInterstitialAd(requireActivity());
                AdmobManager.showInterstitialAd(requireActivity());
                ((AppClass) requireActivity().getApplication())
                        .getAppOpenObserver()
                        .markInterstitialShown();
            } else if (adType.equalsIgnoreCase("applovin")) {
                ApplovinManager.showApplovinInterstitial(requireActivity());
                ((AppClass) requireActivity().getApplication())
                        .getAppOpenObserver()
                        .markInterstitialShown();
            }

            /// ❌ Jangan tampilkan Open Ads setelah interstitial
            // ✅ tandai supaya open ads tidak muncul
            AppClass.getInstance(requireContext()).getAppOpenObserver().markInterstitialShown();

            loadGames();
        });

        coinDialog.show();

    }

    private void showInsufficientDialog(long secondsLeft) {
        insufficentDialog = new SantaCustomDialog(
                getContext(),
                "insufficient",
                1200,
                R.drawable.ic_clock,
                getString(R.string.text_insufficient),
                "Mainkan, minimal " + categories.get(catPos).getGamesModelLIst().get(gamePos).getGamePlayTime() +
                        " menit untuk mendapatkan " +
                        categories.get(catPos).getGamesModelLIst().get(gamePos).getGameCoin() + " berlian.",
                "Sisa waktu, kurang : " + secondsLeft + " detik",
                getString(R.string.btn_cancel), "Ulangi Lagi", "Enter Referral Code", "null");

        insufficentDialog.setOnDialogButtonClickListener(() -> {
            isGameStarted = true;
            gameStartTimeMillis = System.currentTimeMillis();
            startGame(categories.get(catPos).getGamesModelLIst().get(gamePos).getGameLink());
            insufficentDialog.dismiss();
        });
        insufficentDialog.show();
    }

    private void loadAds(String type) {
        if (type.equalsIgnoreCase("google_ads_manager")) {
            AdsManager.setAdsManagerCallback(this);
            AdsManager.initAds(getContext());
        } else if (type.equalsIgnoreCase("admob")) {
            // opsional: banner
        } else if (type.equalsIgnoreCase("applovin")) {
            ApplovinManager.setApplovinManagerCallback(this);
        }
    }

    @Override public void onAdDismissed() {}
    @Override public void onAdHidden() {}

    private void dailyCheckIn(TextView userCoins) {
        JSONObject params = new JSONObject();
        try { params.put("uid", Methods.getData(getContext(), Data.USER_ID)); } catch (JSONException e) { e.printStackTrace(); }
        SantaCoderJsonObjRequest checkinRequest = new SantaCoderJsonObjRequest(Request.Method.POST, BuildConfig.MAIN_URL + "/aapi_files/api_bonus.php", params, response -> {
            try {
                if (response.getString("status").equalsIgnoreCase("1")) {
                    String coins = response.getString("user_coins");
                    userCoins.setText(coins);
                    Methods.setData(getContext(), Data.USER_COINS, coins);
                }
                Methods.showSuccessResponse(TAG, response.getString("message"), getContext());
            } catch (JSONException e) { e.printStackTrace(); }
        }, error -> Methods.showErrorResponse(TAG, error, getContext()));
        AppClass.getInstance(getContext()).addToRequestQueue(checkinRequest, TAG);
    }

    @Override public void onPause() { super.onPause(); homeSliderHandler.removeCallbacks(homeSliderRunnable); }
    @Override public void onDestroy() { super.onDestroy(); binding = null; }
    @Override public void onStop() { super.onStop(); AppClass.getInstance(getContext()).cancelPendingRequests(TAG); }
}
