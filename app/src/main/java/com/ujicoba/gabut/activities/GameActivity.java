package com.ujicoba.gabut.activities;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.Request;
import com.ujicoba.gabut.BottomMarginDecoration;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.adapters.GameCategoryAdapter;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.ActivityGameBinding;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
import com.ujicoba.gabut.models.GameCategory;
import com.ujicoba.gabut.models.GamesModel;
import com.ujicoba.gabut.utils.AdmobManager;
import com.ujicoba.gabut.utils.AdsManager;
import com.ujicoba.gabut.utils.AppClass;
import com.ujicoba.gabut.utils.ApplovinManager;
import com.ujicoba.gabut.utils.Methods;
import com.ujicoba.gabut.utils.SantaCustomDialog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements AdsManager.AdsManagerCallback, ApplovinManager.ApplovinManagerCallback {

    ActivityGameBinding binding;
    private final String TAG = GameActivity.class.getSimpleName();
    private LayoutProfileBinding profileBinding;
    private List<GameCategory> categories;
    private GameCategoryAdapter categoryAdapter;
    private SantaCustomDialog coinDialog;
    private SantaCustomDialog insufficentDialog;
    public static int catPos;
    public static int gamePos;
    private String adType;
    private boolean isGameStarted = false;
    private long gameStartTime;
    private long requiredPlayMillis;
    private GamesModel currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Methods.setFullScreen(this);

        if (Methods.isNetworkConnected(this)) {
            adType = Methods.getData(this, Data.ADS_NETWORK_NAME);
            loadAds(adType);
            initProfile();
            loadGames();
        } else {
            Methods.showNoInternetNotification(this);
        }
    }

    private void initProfile() {
        profileBinding = LayoutProfileBinding.bind(binding.layoutProfile.getRoot());
        Methods.initUserProfile(this, profileBinding, "Kembali!");
        profileBinding.profileButton.setOnClickListener(v -> {
            Methods.gotoNextActivity(this, MainActivity.class);
            finish();
        });
    }

    private void loadGames() {
        categories = new ArrayList<>();
        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(this, Data.USER_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SantaCoderJsonObjRequest gamesRequest = new SantaCoderJsonObjRequest(
                Request.Method.POST,
                BuildConfig.MAIN_URL + "/aapi_files/api_games.php",
                params,
                response -> {
                    try {
                        JSONArray catArray = response.getJSONArray("categories");
                        for (int i = 0; i < catArray.length(); i++) {
                            JSONObject categoryObject = catArray.getJSONObject(i);
                            int categoryId = categoryObject.getInt("id");
                            String categoryName = categoryObject.getString("cat_name");
                            JSONArray gamesArray = categoryObject.getJSONArray("games");

                            List<GamesModel> gamesModelList = new ArrayList<>();
                            for (int j = 0; j < gamesArray.length(); j++) {
                                JSONObject gamesObj = gamesArray.getJSONObject(j);
                                GamesModel game = new GamesModel(
                                        gamesObj.getInt("id"),
                                        gamesObj.getInt("cat_id"),
                                        gamesObj.getString("game_logo"),
                                        gamesObj.getString("game_name"),
                                        gamesObj.getString("game_coin"),
                                        gamesObj.getString("game_play_time"),
                                        gamesObj.getString("game_link"),
                                        gamesObj.optString("game_cost", "0")
                                );
                                gamesModelList.add(game);
                            }

                            GameCategory category = new GameCategory(categoryId, categoryName, gamesModelList);
                            categories.add(category);
                        }

                        categoryAdapter = new GameCategoryAdapter(categories, false);
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        binding.recyclerView.setAdapter(categoryAdapter);
                        binding.recyclerView.addItemDecoration(new BottomMarginDecoration(250));

                        categoryAdapter.setOnGameItemClickListener((url, cPos, gPos) -> {
                            catPos = cPos;
                            gamePos = gPos;
                            currentGame = categories.get(catPos).getGamesModelLIst().get(gamePos);

                            int cost = 0;
                            try {
                                cost = Integer.parseInt(currentGame.getGameCost());
                            } catch (Exception ignored) {}

                            if (cost > 0) {
                                showPlayConfirmationDialog(currentGame);
                            } else {
                                startGame(currentGame.getGameLink());
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, this)
        );

        AppClass.getInstance(this).addToRequestQueue(gamesRequest, TAG);
    }

    // ✅ Pop-up konfirmasi biaya main
    private void showPlayConfirmationDialog(GamesModel game) {
        String message = "Untuk memainkan <b>\"" + game.getGameName() + "\"</b><br><br>" +
                "💎 Biaya bermain: <b>" + game.getGameCost() + " berlian</b><br>" +
                "⏱️ Durasi minimal: <b>" + game.getGamePlayTime() + " menit</b><br>" +
                "🎁 Hadiah jika berhasil: <b>+" + game.getGameCoin() + " berlian</b><br><br>" +
                "⛔ <b>PENTING: Biaya <b>" + game.getGameCost() + " berlian akan hangus jika Anda keluar atau durasi bermain kurang dari " + game.getGamePlayTime() + " menit</b><br><br>" +
                "Apakah kamu ingin melanjutkan bermain?";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Konfirmasi Bermain Game")
                .setMessage(android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_LEGACY))
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    startGameWithCost(game);
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ✅ Proses potong koin di server
    private void startGameWithCost(GamesModel game) {
        String userId = Methods.getData(this, Data.USER_ID);
        String url = BuildConfig.MAIN_URL + "/aapi_files/api_start_game.php?user_id=" + userId + "&game_id=" + game.getId();

        ProgressDialog progress = new ProgressDialog(this);
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
                        Methods.showToast(this, message);

                        if (status.equalsIgnoreCase("success")) {
                            String link = response.getString("game_link");
                            startGame(link);
                        }
                    } catch (JSONException e) {
                        Methods.showToast(this, "Kesalahan data dari server");
                    }
                },
                error -> {
                    progress.dismiss();
                    Methods.showToast(this, "Gagal menghubungi server");
                }
        );

        AppClass.getInstance(this).addToRequestQueue(request, TAG);
    }

    private void startGame(String url) {
        isGameStarted = true;
        gameStartTime = System.currentTimeMillis();
        requiredPlayMillis = Long.parseLong(currentGame.getGamePlayTime()) * 60 * 1000;
        AppClass.getInstance(this).getAppOpenObserver().markFromGame();

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.ngisor));
        builder.setUrlBarHidingEnabled(true);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (!isGameStarted || currentGame == null) return;
        isGameStarted = false;

        long elapsed = System.currentTimeMillis() - gameStartTime;

        if (elapsed >= requiredPlayMillis) {
            sendRewardToServer();
            showCoinDialog();
        } else {
            showInsufficientDialog((int) ((requiredPlayMillis - elapsed) / 1000));
        }
    }

    private void sendRewardToServer() {
        Methods.sendRequestToServer(
                TAG, this, currentGame.getId(),
                getString(R.string.game_text),
                profileBinding.userCoins, null, null
        );
    }

    private void showCoinDialog() {
        coinDialog = new SantaCustomDialog(
                this,
                "cointanpaiklan",
                1200,
                R.drawable.ic_no_internet,
                getString(R.string.msg_congratulations),
                getString(R.string.msg_you_got) + " " +
                        currentGame.getGameCoin() + " " +
                        getString(R.string.text_coins),
                currentGame.getGameCoin(),
                getString(R.string.btn_ok_thanks),
                getString(R.string.btn_ok_thanks),
                "Enter Referral Code",
                "null"
        );

        coinDialog.setOnDialogButtonClickListener(() -> {
            coinDialog.dismiss();

            if (adType.equalsIgnoreCase("google_ads_manager")) {
                AdsManager.loadInterstitialAds(this);
                AdsManager.showInterstitialAds(GameActivity.this);
                ((AppClass) getApplication()).getAppOpenObserver().markInterstitialShown();
            } else if (adType.equalsIgnoreCase("admob")) {
                AdmobManager.loadInterstitialAd(this);
                AdmobManager.showInterstitialAd(GameActivity.this);
                ((AppClass) getApplication()).getAppOpenObserver().markInterstitialShown();
            } else if (adType.equalsIgnoreCase("applovin")) {
                ApplovinManager.showApplovinInterstitial(GameActivity.this);
                ((AppClass) getApplication()).getAppOpenObserver().markInterstitialShown();
            }

            loadGames();
        });

        coinDialog.show();
    }

    private void showInsufficientDialog(int remainingSeconds) {
        insufficentDialog = new SantaCustomDialog(
                this,
                "insufficient",
                1200,
                R.drawable.ic_clock,
                getString(R.string.text_insufficient),
                "Mainkan minimal " + currentGame.getGamePlayTime() +
                        " menit untuk mendapatkan " + currentGame.getGameCoin() + " berlian.",
                "Sisa waktu: " + remainingSeconds + " detik",
                getString(R.string.btn_cancel),
                "Ulangi Lagi",
                "Enter Referral Code",
                "null"
        );

        insufficentDialog.setOnDialogButtonClickListener(() -> {
            insufficentDialog.dismiss();
            isGameStarted = true;
            startGame(currentGame.getGameLink());
        });

        insufficentDialog.show();
    }

    private void loadAds(String type) {
        if (type.equalsIgnoreCase("google_ads_manager")) {
            AdsManager.setAdsManagerCallback(this);
            AdsManager.initAds(this);
        } else if (type.equalsIgnoreCase("admob")) {
            // Tidak load interstitial di awal
        } else if (type.equalsIgnoreCase("applovin")) {
            ApplovinManager.setApplovinManagerCallback(this);
            ApplovinManager.initApplovin(this, binding.bannerLayout.bannerLayout);
        }
    }

    @Override public void onAdDismissed() {}
    @Override public void onAdHidden() {}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Methods.gotoNextActivity(this, MainActivity.class);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppClass.getInstance(this).cancelPendingRequests(TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdsManager.destroyBannerAd();
        binding = null;
    }
}
