package com.ujicoba.gabut.activities;

import androidx.appcompat.app.AlertDialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.ujicoba.gabut.BottomMarginDecoration;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.adapters.GameListAdapter;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.ActivityGameListBinding;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
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

public class GameListActivity extends AppCompatActivity implements AdsManager.AdsManagerCallback, ApplovinManager.ApplovinManagerCallback {
    ActivityGameListBinding binding;
    private final String TAG = GameListActivity.class.getSimpleName();
    private LayoutProfileBinding profileBinding;
    private List<GamesModel> gamesModelList;
    private GameListAdapter gameListAdapter;
    private SantaCustomDialog coinDialog;
    private SantaCustomDialog insufficentDialog;
    private long totalDurationSeconds;
    private long startTimeMillis;
    private boolean isGameStarted = false;
    public static int gamePos;
    private int catId;
    private String adType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Methods.setFullScreen(this);

        if (getIntent().hasExtra("cat_id")) {
            catId = getIntent().getIntExtra("cat_id", 0);
        }

        if (Methods.isNetworkConnected(this)) {
            adType = Methods.getData(this, Data.ADS_NETWORK_NAME);
            loadAds(adType);
            initProfile();
            fetchGamesFromServer();
        } else {
            Methods.showNoInternetNotification(this);
        }
    }

    private void initProfile() {
        profileBinding = LayoutProfileBinding.bind(binding.layoutProfile.getRoot());
        Methods.initUserProfile(this, profileBinding, "Kembali!");
        profileBinding.profileButton.setOnClickListener(view -> {
            Methods.gotoNextActivity(this, MainActivity.class);
            finish();
        });
    }

    private void fetchGamesFromServer() {
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
                            if (categoryId == catId) {
                                JSONArray gamesArray = categoryObject.getJSONArray("games");
                                gamesModelList = new ArrayList<>();

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

                                // ✅ Inisialisasi adapter di sini
                                gameListAdapter = new GameListAdapter(gamesModelList);
                                binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
                                binding.recyclerView.setAdapter(gameListAdapter);
                                binding.recyclerView.addItemDecoration(new BottomMarginDecoration(250));

                                // ✅ Setelah adapter siap, baru set listener
                                gameListAdapter.setOnGameItemClickListener((url, gPos) -> {
                                    gamePos = gPos;
                                    GamesModel game = gamesModelList.get(gamePos);

                                    int cost = 0;
                                    try { cost = Integer.parseInt(game.getGameCost()); } catch (Exception ignored) {}

                                    if (cost > 0) {
                                        showPlayConfirmationDialog(game);
                                    } else {
                                        totalDurationSeconds = Long.parseLong(game.getGamePlayTime()) * 60;
                                        startTimeMillis = System.currentTimeMillis();
                                        isGameStarted = true;
                                        startGame(game.getGameLink());
                                    }
                                });
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Methods.showErrorResponse(TAG, error, this)
        );

        AppClass.getInstance(this).addToRequestQueue(gamesRequest, TAG);
    }


    // 🔹 Tampilkan konfirmasi biaya sebelum bermain
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


    // 🔹 Proses pemotongan biaya bermain ke server
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

                        if (status.equals("success")) {
                            totalDurationSeconds = Long.parseLong(game.getGamePlayTime()) * 60;
                            startTimeMillis = System.currentTimeMillis();
                            isGameStarted = true;
                            startGame(game.getGameLink());
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
        // Tandai bahwa user masuk dari game (CustomTabsIntent)
        ((AppClass) getApplication()).getAppOpenObserver().markFromGame();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.ngisor));
        builder.setUrlBarHidingEnabled(true);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppClass.getInstance(this).setCurrentActivity(this);
        // ✅ Cek apakah user harus diblokir
        Methods.checkUserBlockNow(this); // untuk pengecekan pemblokiran
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d(TAG, "onRestart dipanggil, isGameStarted=" + isGameStarted);

        if (isGameStarted) {
            isGameStarted = false;
            long elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000;
            Log.d(TAG, "elapsed=" + elapsed + " detik, total=" + totalDurationSeconds);

            if (elapsed >= totalDurationSeconds) {
                sendRewardToServer();
                showCoinDialog();
            } else {
                showInsufficientDialog(totalDurationSeconds - elapsed);
            }
        }
    }


    private void sendRewardToServer() {
        Methods.sendRequestToServer(TAG, this,
                gamesModelList.get(gamePos).getId(),
                getString(R.string.game_text),
                profileBinding.userCoins, null, null);
        fetchGamesFromServer();
    }

    private void showCoinDialog() {
        coinDialog = new SantaCustomDialog(this, "coin", 1200, R.drawable.ic_no_internet,
                getString(R.string.msg_congratulations),
                getString(R.string.msg_you_got) + " " +
                        gamesModelList.get(gamePos).getGameCoin() + " " + getString(R.string.text_coins) + " " + getString(R.string.iklanlis),
                gamesModelList.get(gamePos).getGameCoin(),
                getString(R.string.btn_ok_thanks), getString(R.string.btn_ok_thanks),
                "Enter Referral Code", "null");

        // Iklan hanya sebagai bonus setelah reward
        coinDialog.setOnDialogButtonClickListener(() -> {
            if (adType.equalsIgnoreCase("google_ads_manager")) {
                AdsManager.loadInterstitialAds(this); // Load iklan gam inter dulu
                AdsManager.showInterstitialAds(GameListActivity.this);
                ((AppClass) getApplication()).getAppOpenObserver().markInterstitialShown();
            } else if (adType.equalsIgnoreCase("admob")) {
                AdmobManager.loadInterstitialAd(this); // Load iklan admob inter dulu
                AdmobManager.showInterstitialAd(GameListActivity.this);
                ((AppClass) getApplication()).getAppOpenObserver().markInterstitialShown();
            } else if (adType.equalsIgnoreCase("applovin")) {
                ApplovinManager.showApplovinInterstitial(GameListActivity.this);
                ((AppClass) getApplication()).getAppOpenObserver().markInterstitialShown();
            }

            // ✅ Setelah inter selesai → izinkan open ads lagi
           // ((AppClass) getApplication()).getAppOpenObserver().setDisableForWebView(false);

            coinDialog.dismiss();
        });

        coinDialog.setOnRewardedButtonClickListener(this::showRewardedAdForBonus);

        coinDialog.show();
        //Methods.checkUserBlockNow(this);
    }


    private void showInsufficientDialog(long remainingSeconds) {
        insufficentDialog = new SantaCustomDialog(this, "insufficient", 1200, R.drawable.ic_clock,
                getString(R.string.text_insufficient),
                "Mainkan, minimal " +
                        gamesModelList.get(gamePos).getGamePlayTime() + " menit untuk mendapatkan " +
                        gamesModelList.get(gamePos).getGameCoin() + " berlian.",
                "Sisa waktu, kurang : " + remainingSeconds + " detik",
                getString(R.string.btn_cancel), "Ulangi Lagi",
                "Enter Referral Code", "null");

        insufficentDialog.setOnDialogButtonClickListener(() -> {
            startTimeMillis = System.currentTimeMillis();
            isGameStarted = true;
            startGame(gamesModelList.get(gamePos).getGameLink());
            insufficentDialog.dismiss();
        });
        insufficentDialog.show();
    }

    private void loadAds(String type) {
        if (type.equalsIgnoreCase("google_ads_manager")) {
            AdsManager.setAdsManagerCallback(this);
            AdsManager.initAds(this);
        //    AdsManager.loadBannerAds(this, binding.bannerLayout.bannerLayout);
        //    AdsManager.adManagerBannerCallback(this, binding.bannerLayout.bannerLayout);
            AdsManager.loadInterstitialAds(this);
        } else if (type.equalsIgnoreCase("admob")) {
         //   AdmobManager.initAdmob(this, binding.bannerLayout.bannerLayout);
            // Interstitial juga hanya load saat tombol OK, jangan load di sini
        } else if (type.equalsIgnoreCase("applovin")) {
            ApplovinManager.setApplovinManagerCallback(this);
            ApplovinManager.initApplovin(this, binding.bannerLayout.bannerLayout);
        }
    }

    private void showRewardedAdForBonus() {

        Runnable rewardAction = () -> {
            Methods.sendRequestToServer(
                    TAG,
                    this,
                    gamesModelList.get(gamePos).getId(),
                    getString(R.string.iklanlis_text), // pakai iklanlis_text
                    profileBinding.userCoins,
                    null, // biar tidak mengurangi jumlah spin setelah lihat iklan
                    null // jangan pakai "rewarded_bonus"
            );
            Methods.showToast(this, "Berlian dari Tonton Iklan!");
            Methods.checkUserBlockNow(this);

            // Langsung kembali ke tampilan spin, tutup dialog
            if (coinDialog != null && coinDialog.isShowing()) {
                coinDialog.dismiss();
            }
        };

        if (adType.equalsIgnoreCase("google_ads_manager")) {
            if (AdsManager.isRewardedAdReady()) {
                AdsManager.showRewardedAd(this, rewardItem -> rewardAction.run());
            } else {
                Methods.showToast(this, "Iklan belum siap. Coba lagi setelah beberapa saat.");
                AdsManager.loadRewardedAd(this); // // Baru di-load kalau user klik
            }
        } else if (adType.equalsIgnoreCase("admob")) {
            if (AdmobManager.isRewardedAdReady()) {
                AdmobManager.showRewardedAd(this, rewardItem -> rewardAction.run());
            } else {
                Methods.showToast(this, "Iklan belum siap. Coba lagi setelah beberapa saat.");
                AdmobManager.loadRewardedAd(this); // Baru di-load kalau user klik
            }
        } else if (adType.equalsIgnoreCase("applovin")) {
            if (ApplovinManager.isRewardedAdReady()) {
                ApplovinManager.showRewardedAd(this, rewardAction);
            } else {
                Methods.showToast(this, "Iklan belum siap. Coba lagi setelah beberapa saat.");
                ApplovinManager.loadApplovinRewarded(this); // Baru di-load kalau user klik
            }
        }
    }

    @Override
    public void onAdDismissed() {}

    @Override
    public void onAdHidden() {}

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Methods.gotoNextActivity(this, GameActivity.class);
        finish();
    }
}
