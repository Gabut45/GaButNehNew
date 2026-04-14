package com.ujicoba.gabut.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import android.os.CountDownTimer;

import santacoder.luckywheel.LuckyWheelView;
import santacoder.luckywheel.model.LuckyItem;

public class SpinActivity extends AppCompatActivity implements AdsManager.AdsManagerCallback, ApplovinManager.ApplovinManagerCallback {
    ActivitySpinBinding binding;
    private final String TAG = SpinActivity.class.getSimpleName();
    private MediaPlayer wheelSound;
    private MediaPlayer dialogSound;
    private static final int SPIN_DURATION = 5000;
    private static final long SPIN_DELAY_MS = 10 * 60 * 1000;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "spin_prefs";
    private static final String KEY_LAST_SPIN_TIME = "last_spin_time";

    List<LuckyItem> luckyItems = new ArrayList<>();
    private SantaCustomDialog coinDialog;
    private LayoutProfileBinding profileBinding;
    private String adType;
    private int spinId;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Methods.setFullScreen(SpinActivity.this);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (Methods.isNetworkConnected(this)) {
            binding.availableSpins.setText(String.format("%s %s",
                    Methods.getData(SpinActivity.this, Data.AVAILABLE_SPIN),
                    getString(R.string.text_spin_available)));

            adType = Methods.getData(SpinActivity.this, Data.ADS_NETWORK_NAME);
            loadAds(adType);
            initProfile();
            loadWheelItems();

            int availableSpin = Integer.parseInt(Methods.getData(SpinActivity.this, Data.AVAILABLE_SPIN));
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

                int availableSpin = Integer.parseInt(Methods.getData(SpinActivity.this, Data.AVAILABLE_SPIN));
                if (availableSpin <= 0) {
                    Methods.showToast(this, getString(R.string.msg_no_spin_available));
                    updatePlayButtonState(availableSpin);
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

                spinId = luckyItems.get(index).id;

                Methods.sendRequestToServer(TAG, SpinActivity.this, spinId,
                        getString(R.string.spin_text),
                        profileBinding.userCoins, binding.availableSpins, null);

                int availableSpin = Integer.parseInt(Methods.getData(SpinActivity.this, Data.AVAILABLE_SPIN));
                availableSpin--;
                Methods.setData(SpinActivity.this, Data.AVAILABLE_SPIN, String.valueOf(availableSpin));

                binding.availableSpins.setText(String.format("%s %s", availableSpin, getString(R.string.text_spin_available)));
                updatePlayButtonState(availableSpin);

                coinDialog = new SantaCustomDialog(
                        SpinActivity.this,
                        "coin",
                        1200,
                        R.drawable.ic_no_internet,
                        getString(R.string.msg_congratulations),
                        getString(R.string.msg_you_got) + " " + Methods.getNumberFromText(luckyItems.get(index).text) + " " + getString(R.string.text_coins) + " " + getString(R.string.spiniklan),
                        Methods.getNumberFromText(luckyItems.get(index).text),
                        getString(R.string.btn_ok_thanks),
                        getString(R.string.btn_ok_thanks),
                        "Enter Referral Code",
                        "null"
                );

                coinDialog.setOnDialogButtonClickListener(() -> {
                    if (adType.equalsIgnoreCase("google_ads_manager")) {
                        AdsManager.loadInterstitialAds(this); // Load iklan gam inter dulu
                        AdsManager.showInterstitialAds(SpinActivity.this);
                    } else if (adType.equalsIgnoreCase("admob")) {
                        AdmobManager.loadInterstitialAd(this); // Load iklan admob inter dulu
                        AdmobManager.showInterstitialAd(SpinActivity.this);
                    } else if (adType.equalsIgnoreCase("applovin")) {
                        ApplovinManager.showApplovinInterstitial(SpinActivity.this);
                    }
                    coinDialog.dismiss();
                });



                coinDialog.setOnRewardedButtonClickListener(this::showRewardedAdForBonus);

                coinDialog.show();
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
        // ✅ Cek apakah user harus diblokir
        Methods.checkUserBlockNow(this); // untuk pengecekan pemblokiran
    }

    private void getWheelItems() {
        SantaCoderJsonObjRequest wheelRequest = new SantaCoderJsonObjRequest(Request.Method.GET, BuildConfig.MAIN_URL + "/aapi_files/api_spins.php", null, response -> {
            try {
                if (response.getString("status").equalsIgnoreCase("1")) {
                    JSONArray wheelArray = response.getJSONArray("spins_data");
                    for (int i = 0; i < wheelArray.length(); i++) {
                        JSONObject wheelObj = wheelArray.getJSONObject(i);
                        int spinId = wheelObj.getInt("id");
                        String spinCoins = wheelObj.getString("spin_coins");
                        String spinColor = wheelObj.getString("spin_color");
                        LuckyItem luckyItem = new LuckyItem(
                                spinId,
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
        if (type.equalsIgnoreCase("google_ads_manager")) {
            AdsManager.setAdsManagerCallback(this);
            AdsManager.initAds(this);
        } else if (type.equalsIgnoreCase("admob")) {
            // AdmobManager.initAdmob(this, binding.bannerLayout.bannerLayout);
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
                    SpinActivity.this,
                    0, // pakai 0 karena ini bukan dari id spin
                    getString(R.string.iklan_text), // pakai iklan_text
                    profileBinding.userCoins,
                    null, // biar tidak mengurangi jumlah spin setelah lihat iklan
                    null // jangan pakai "rewarded_bonus"
            );
            Methods.showToast(this, "Berlian dari Tonton Iklan!");


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
    public void onBackPressed() {
        Methods.gotoNextActivity(this, MainActivity.class);
        finish();
    }

    @Override protected void onStart() { super.onStart(); }
    @Override protected void onRestart() { super.onRestart(); }
    @Override protected void onResume() {
        super.onResume();
        wheelSound = MediaPlayer.create(this, R.raw.wheelsound);
        dialogSound = MediaPlayer.create(this, R.raw.popup);
        // ✅ Cek apakah user harus diblokir
        Methods.checkUserBlockNow(this); // untuk pengecekan pemblokiran
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
        if (wheelSound != null) { wheelSound.release(); wheelSound = null; }
        if (dialogSound != null) { dialogSound.release(); dialogSound = null; }
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
        binding = null;
    }
}
