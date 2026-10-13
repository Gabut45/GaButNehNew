package com.ujicoba.gabut.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.ActivitySpinBinding;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
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
import java.util.Random;

import santacoder.luckywheel.model.LuckyItem;

public class SpinActivity extends AppCompatActivity implements AdsManager.AdsManagerCallback, ApplovinManager.ApplovinManagerCallback {
    ActivitySpinBinding binding;
    private final String TAG = SpinActivity.class.getSimpleName();
    private MediaPlayer wheelSound;
    private MediaPlayer dialogSound;
    private static final long SPIN_DELAY_MS = 10 * 60 * 1000; // 10 menit
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "spin_prefs";
    private static final String KEY_LAST_SPIN_TIME = "last_spin_time";

    private final List<LuckyItem> luckyItems = new ArrayList<>();
    private SantaCustomDialog coinDialog;
    private LayoutProfileBinding profileBinding;
    private String adType;
    private int spinId;

    private CountDownTimer countDownTimer;
    private ProgressDialog adLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Methods.setFullScreen(SpinActivity.this);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Inisialisasi dialog loading jika koneksi lambat
        adLoadingDialog = new ProgressDialog(this);
        adLoadingDialog.setMessage("Menyiapkan iklan hadiah...");
        adLoadingDialog.setCancelable(false);

        if (Methods.isNetworkConnected(this)) {
            binding.availableSpins.setText(String.format("%s %s",
                    Methods.getData(SpinActivity.this, Data.AVAILABLE_SPIN),
                    getString(R.string.text_spin_available)));

            adType = Methods.getData(SpinActivity.this, Data.ADS_NETWORK_NAME);
            loadAds(adType);

            // Preload iklan sejak awal
            preloadRewardedAd();
            preloadInterstitialAd();

            initProfile();
            loadWheelItems();

            int availableSpin = 0;
            try {
                availableSpin = Integer.parseInt(Methods.getData(SpinActivity.this, Data.AVAILABLE_SPIN));
            } catch (NumberFormatException ignored) {}
            updatePlayButtonState(availableSpin);

        } else {
            Methods.showNoInternetNotification(this);
        }
    }

    private void updatePlayButtonState(int availableSpin) {
        if (availableSpin <= 0) {
            binding.playBtn.setClickable(false);
            binding.playBtn.setAlpha(0.5f);
        } else {
            binding.playBtn.setClickable(true);
            binding.playBtn.setAlpha(1f);
        }
    }

    private void initProfile() {
        profileBinding = LayoutProfileBinding.bind(binding.layoutProfile.getRoot());
        Methods.initUserProfile(SpinActivity.this, profileBinding, "Kembali");
        profileBinding.profileButton.setOnClickListener(view -> {
            Methods.gotoNextActivity(SpinActivity.this, MainActivity.class);
            finish();
        });
    }

    private void loadWheelItems() {
        if (Methods.isNetworkConnected(SpinActivity.this)) {
            getWheelItems();

            binding.playBtn.setOnClickListener(view -> {
                if (!Methods.isNetworkConnected(this)) {
                    Methods.showNoInternetNotification(this);
                    return;
                }

                long lastSpinTime = sharedPreferences.getLong(KEY_LAST_SPIN_TIME, 0);
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastSpinTime < SPIN_DELAY_MS) {
                    long remainingMillis = SPIN_DELAY_MS - (currentTime - lastSpinTime);
                    showCooldownTimer(remainingMillis);
                    return;
                }

                int availableSpin = 0;
                try {
                    availableSpin = Integer.parseInt(Methods.getData(SpinActivity.this, Data.AVAILABLE_SPIN));
                } catch (NumberFormatException ignored) {}

                if (availableSpin <= 0) {
                    Methods.showToast(this, getString(R.string.msg_no_spin_available));
                    updatePlayButtonState(availableSpin);
                    return;
                }

                if (luckyItems.isEmpty()) {
                    Methods.showToast(this, "Memuat roda hadiah...");
                    return;
                }

                binding.playBtn.setClickable(false);
                binding.cooldownTimerText.setVisibility(View.GONE);

                Random random = new Random();
                int indexNumber = random.nextInt(luckyItems.size());
                binding.spinWheel.startLuckyWheelWithTargetIndex(indexNumber);
                if (wheelSound != null) wheelSound.start();

                sharedPreferences.edit().putLong(KEY_LAST_SPIN_TIME, currentTime).apply();
            });

            binding.spinWheel.setLuckyRoundItemSelectedListener(index -> {
                binding.playBtn.setClickable(true);
                if (wheelSound != null && wheelSound.isPlaying()) {
                    wheelSound.pause();
                    wheelSound.seekTo(0);
                }
                if (dialogSound != null) {
                    dialogSound.start();
                }

                if (index >= 0 && index < luckyItems.size()) {
                    spinId = luckyItems.get(index).id;

                    Methods.sendRequestToServer(TAG, SpinActivity.this, spinId,
                            getString(R.string.spin_text),
                            profileBinding.userCoins, binding.availableSpins, null);

                    int availableSpin = 0;
                    try {
                        availableSpin = Integer.parseInt(Methods.getData(SpinActivity.this, Data.AVAILABLE_SPIN));
                    } catch (NumberFormatException ignored) {}
                    availableSpin = Math.max(0, availableSpin - 1);
                    Methods.setData(SpinActivity.this, Data.AVAILABLE_SPIN, String.valueOf(availableSpin));

                    binding.availableSpins.setText(String.format("%s %s", availableSpin, getString(R.string.text_spin_available)));
                    updatePlayButtonState(availableSpin);

                    String coinsWon = Methods.getNumberFromText(luckyItems.get(index).text);

                    coinDialog = new SantaCustomDialog(
                            SpinActivity.this,
                            "coin",
                            1200,
                            R.drawable.ic_no_internet,
                            getString(R.string.msg_congratulations),
                            getString(R.string.msg_you_got) + " " + coinsWon + " " + getString(R.string.text_coins) + " " + getString(R.string.spiniklan),
                            coinsWon,
                            getString(R.string.btn_ok_thanks),
                            getString(R.string.btn_ok_thanks),
                            "Enter Referral Code",
                            "null"
                    );

                    // Saat tombol OKE ditekan (tampilkan Interstitial jika sudah ready)
                    coinDialog.setOnDialogButtonClickListener(() -> {
                        showInterstitialSafe();
                        coinDialog.dismiss();
                    });

                    // Saat tombol "TONTON IKLAN (+500)" ditekan
                    coinDialog.setOnRewardedButtonClickListener(this::showRewardedAdForBonus);

                    coinDialog.show();
                }
            });

        } else {
            Methods.showNoInternetNotification(this);
        }
    }

    private void showCooldownTimer(long millisUntilFinished) {
        binding.cooldownTimerText.setVisibility(View.VISIBLE);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(millisUntilFinished, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                binding.cooldownTimerText.setText(String.format("Tunggu %02d:%02d sebelum Putar Roda lagi!", minutes, seconds));
            }

            @Override
            public void onFinish() {
                binding.cooldownTimerText.setVisibility(View.GONE);
            }
        }.start();

        Methods.checkUserBlockNow(this);
    }

    private void getWheelItems() {
        SantaCoderJsonObjRequest wheelRequest = new SantaCoderJsonObjRequest(Request.Method.GET, BuildConfig.MAIN_URL + "/aapi_files/api_spins.php", null, response -> {
            try {
                if (response.getString("status").equalsIgnoreCase("1")) {
                    JSONArray wheelArray = response.getJSONArray("spins_data");
                    luckyItems.clear();
                    for (int i = 0; i < wheelArray.length(); i++) {
                        JSONObject wheelObj = wheelArray.getJSONObject(i);
                        int id = wheelObj.getInt("id");
                        String spinCoins = wheelObj.getString("spin_coins");
                        String spinColor = wheelObj.getString("spin_color");
                        LuckyItem luckyItem = new LuckyItem(
                                id,
                                spinCoins + " " + getString(R.string.text_coins),
                                R.drawable.coin,
                                Color.parseColor(spinColor));
                        luckyItems.add(luckyItem);
                    }
                    binding.spinWheel.setData(luckyItems);
                    binding.spinWheel.setRound(7);
                    Methods.checkVpnService(SpinActivity.this);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Methods.showErrorResponse(TAG, error, SpinActivity.this));
        AppClass.getInstance(this).addToRequestQueue(wheelRequest, TAG);
    }

    private void loadAds(String type) {
        if (type == null) return;
        if (type.equalsIgnoreCase("google_ads_manager")) {
            AdsManager.setAdsManagerCallback(this);
            AdsManager.initAds(this);
        } else if (type.equalsIgnoreCase("applovin")) {
            ApplovinManager.setApplovinManagerCallback(this);
            ApplovinManager.initApplovin(this, binding.bannerLayout.bannerLayout);
        }
    }

    // Preload iklan rewarded di latar belakang
    private void preloadRewardedAd() {
        if (adType == null) return;
        if (adType.equalsIgnoreCase("google_ads_manager")) {
            AdsManager.loadRewardedAd(this);
        } else if (adType.equalsIgnoreCase("admob")) {
            AdmobManager.loadRewardedAd(this);
        } else if (adType.equalsIgnoreCase("applovin")) {
            ApplovinManager.loadApplovinRewarded(this);
        }
    }

    // Preload iklan interstitial di latar belakang
    private void preloadInterstitialAd() {
        if (adType == null) return;
        if (adType.equalsIgnoreCase("google_ads_manager")) {
            AdsManager.loadInterstitialAds(this);
        } else if (adType.equalsIgnoreCase("admob")) {
            AdmobManager.loadInterstitialAd(this);
        } else if (adType.equalsIgnoreCase("applovin")) {
            // AppLovin interstitial biasanya di-handle oleh manager
        }
    }

    // Tampilkan interstitial yang sudah siap (tanpa load berbarengan)
    private void showInterstitialSafe() {
        if (adType == null) return;
        if (adType.equalsIgnoreCase("google_ads_manager")) {
            AdsManager.showInterstitialAds(this);
        } else if (adType.equalsIgnoreCase("admob")) {
            AdmobManager.showInterstitialAd(this);
        } else if (adType.equalsIgnoreCase("applovin")) {
            ApplovinManager.showApplovinInterstitial(this);
        }
        // Preload untuk putaran berikutnya
        preloadInterstitialAd();
    }

    // Menampilkan iklan hadiah (+500 berlian) dengan mulus
    private void showRewardedAdForBonus() {
        Runnable rewardAction = () -> {
            Methods.sendRequestToServer(
                    TAG,
                    SpinActivity.this,
                    0,
                    getString(R.string.iklan_text),
                    profileBinding.userCoins,
                    null,
                    null
            );
            Methods.showToast(this, "Berlian dari Tonton Iklan berhasil ditambahkan!");

            if (coinDialog != null && coinDialog.isShowing()) {
                coinDialog.dismiss();
            }

            // Langsung siapkan iklan berikutnya untuk putaran selanjutnya
            preloadRewardedAd();
        };

        boolean isReady = false;
        if (adType != null) {
            if (adType.equalsIgnoreCase("google_ads_manager")) {
                isReady = AdsManager.isRewardedAdReady();
            } else if (adType.equalsIgnoreCase("admob")) {
                isReady = AdmobManager.isRewardedAdReady();
            } else if (adType.equalsIgnoreCase("applovin")) {
                isReady = ApplovinManager.isRewardedAdReady();
            }
        }

        if (isReady) {
            // Jika sudah siap: langsung putar seketika!
            playRewardedAd(rewardAction);
        } else {
            // Jika belum selesai download (misal koneksi lambat):
            // Tampilkan loading sebentar lalu otomatis putar, tanpa paksa klik 2 kali
            if (!isFinishing() && !isDestroyed()) {
                adLoadingDialog.show();
            }
            preloadRewardedAd();

            // Beri toleransi waktu 2.5 detik untuk menyelesaikan download
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (adLoadingDialog != null && adLoadingDialog.isShowing()) {
                    adLoadingDialog.dismiss();
                }
                playRewardedAd(rewardAction);
            }, 2500);
        }
    }

    private void playRewardedAd(Runnable onReward) {
        if (adType == null) return;
        if (adType.equalsIgnoreCase("google_ads_manager")) {
            if (AdsManager.isRewardedAdReady()) {
                AdsManager.showRewardedAd(this, rewardItem -> onReward.run());
            } else {
                Methods.showToast(this, "Iklan sedang dimuat, silakan coba beberapa detik lagi.");
            }
        } else if (adType.equalsIgnoreCase("admob")) {
            if (AdmobManager.isRewardedAdReady()) {
                AdmobManager.showRewardedAd(this, rewardItem -> onReward.run());
            } else {
                Methods.showToast(this, "Iklan sedang dimuat, silakan coba beberapa detik lagi.");
            }
        } else if (adType.equalsIgnoreCase("applovin")) {
            if (ApplovinManager.isRewardedAdReady()) {
                ApplovinManager.showRewardedAd(this, onReward);
            } else {
                Methods.showToast(this, "Iklan sedang dimuat, silakan coba beberapa detik lagi.");
            }
        }
    }

    @Override
    public void onAdDismissed() {
        preloadRewardedAd();
        preloadInterstitialAd();
    }

    @Override
    public void onAdHidden() {
        preloadRewardedAd();
        preloadInterstitialAd();
    }

    @Override
    public void onBackPressed() {
        Methods.gotoNextActivity(this, MainActivity.class);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        wheelSound = MediaPlayer.create(this, R.raw.wheelsound);
        dialogSound = MediaPlayer.create(this, R.raw.popup);
        Methods.checkUserBlockNow(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wheelSound != null) { wheelSound.release(); wheelSound = null; }
        if (dialogSound != null) { dialogSound.release(); dialogSound = null; }
        if (countDownTimer != null) { countDownTimer.cancel(); }
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppClass.getInstance(this).cancelPendingRequests(TAG);
        if (wheelSound != null) { wheelSound.release(); wheelSound = null; }
        if (dialogSound != null) { dialogSound.release(); dialogSound = null; }
        if (countDownTimer != null) { countDownTimer.cancel(); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adLoadingDialog != null && adLoadingDialog.isShowing()) {
            adLoadingDialog.dismiss();
        }
        if (wheelSound != null) { wheelSound.release(); wheelSound = null; }
        if (dialogSound != null) { dialogSound.release(); dialogSound = null; }
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
        binding = null;
    }
}