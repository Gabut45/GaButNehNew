package com.ujicoba.gabut.activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.adapters.PromoPagerAdapter;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.databinding.ActivityMainBinding;
import com.ujicoba.gabut.fragments.HomeFragment;
import com.ujicoba.gabut.fragments.LeaderboardFragment;
import com.ujicoba.gabut.fragments.ProfileFragment;
import com.ujicoba.gabut.fragments.RedeemFragment;
import com.ujicoba.gabut.fragments.ReferFragment;
import com.ujicoba.gabut.utils.Methods;
import com.ujicoba.gabut.utils.SantaCustomDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private static final String PREFS_NAME = "PromoPrefs";
    private static final String PREF_HIDE_UNTIL = "HideUntil";
    private AlertDialog promoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Methods.setFullScreen(this);
        setUpBottomNavigation();
        openFragmentOnNotification();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndShowPromoDialog();

        // ✅ Tambahan: Cek blokir user setiap kali user kembali ke MainActivity
        Methods.checkUserBlockNow(this);
    }


    private void setUpBottomNavigation() {
        loadFragment(new HomeFragment(), false);
        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                switch (i) {
                    case 0:
                        loadFragment(new HomeFragment(), true); break;
                    case 1:
                        loadFragment(new LeaderboardFragment(), true); break;
                    case 2:
                        loadFragment(new ReferFragment(), true); break;
                    case 3:
                        checkRedeemAccess(); break;
                    case 4:
                        loadFragment(new ProfileFragment(), true); break;
                }
                return true;
            }
        });
    }

    private void checkRedeemAccess() {
        int userCoins = 0;
        try {
            userCoins = Integer.parseInt(Methods.getData(this, Data.USER_COINS));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (userCoins >= 10000) {
            loadFragment(new RedeemFragment(), true);
        } else {
            showLockedRedeemDialog();
        }
    }

    private void showLockedRedeemDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.redeem_locked_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        Button btnOk = dialogView.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void loadFragment(Fragment fragment, boolean isReplace) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (isReplace) {
            ft.replace(R.id.container, fragment);
        } else {
            ft.add(R.id.container, fragment);
        }
        ft.addToBackStack(null);
        ft.commit();
    }

    private void openFragmentOnNotification() {
        String fragmentName = getIntent().getStringExtra("fragmentName");
        if (fragmentName != null) {
            String fragName = Methods.getData(this, "fragName");
            switch (fragName) {
                case "LeaderboardFragment":
                    loadFragment(new LeaderboardFragment(), true);
                    Log.e("fragName", fragmentName);
                    break;
                case "ReferFragment":
                    loadFragment(new ReferFragment(), true);
                    Log.e("fragName", fragmentName);
                    break;
                case "RedeemFragment":
                    checkRedeemAccess();
                    Log.e("fragName", fragmentName);
                    break;
                case "ProfileFragment":
                    loadFragment(new ProfileFragment(), true);
                    Log.e("fragName", fragmentName);
                    break;
            }
        }
    }

    private void checkAndShowPromoDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long hideUntil = prefs.getLong(PREF_HIDE_UNTIL, 0);
        if (System.currentTimeMillis() >= hideUntil) {
            showPromoDialog();
        }
    }

    private void showPromoDialog() {
        if (isFinishing() || isDestroyed()) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_promo, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        promoDialog = builder.create();

        if (promoDialog.getWindow() != null) {
            promoDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        ViewPager2 viewPager = dialogView.findViewById(R.id.viewPagerPromo);
        CheckBox checkBox = dialogView.findViewById(R.id.checkBoxJanganTampilkan);
        Button btnPrev = dialogView.findViewById(R.id.btnPrev);
        Button btnNext = dialogView.findViewById(R.id.btnNext);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        loadPromosFromApi(promoItems -> {
            if (promoItems.isEmpty()) return;

            PromoPagerAdapter adapter = new PromoPagerAdapter(this, promoItems);
            viewPager.setAdapter(adapter);

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    int total = adapter.getItemCount();
                    btnPrev.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
                    btnNext.setVisibility(position == total - 1 ? View.GONE : View.VISIBLE);
                    btnClose.setVisibility(position == total - 1 ? View.VISIBLE : View.GONE);
                }
            });

            btnPrev.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true));
            btnNext.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true));
            btnClose.setOnClickListener(v -> {
                if (checkBox.isChecked()) {
                    long tomorrow = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putLong(PREF_HIDE_UNTIL, tomorrow);
                    editor.apply();
                }
                if (promoDialog != null && promoDialog.isShowing()) {
                    promoDialog.dismiss();
                }
            });

            btnPrev.setVisibility(View.GONE);
            btnNext.setVisibility(promoItems.size() > 1 ? View.VISIBLE : View.GONE);
            btnClose.setVisibility(View.GONE);

            if (!isFinishing() && !isDestroyed()) {
                promoDialog.show();
            }
        });
    }

    private void loadPromosFromApi(Consumer<List<PromoItem>> callback) {
        String url = "https://work.gabutgame.my.id/aapi_files/api_get_promos.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<PromoItem> promos = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String image = obj.getString("image_url");
                            String link = obj.getString("link");
                            promos.add(new PromoItem(image, link));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        callback.accept(promos);
                    }
                },
                error -> {
                    error.printStackTrace();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        callback.accept(Collections.emptyList());
                    }
                });

        queue.add(request);
    }

    public static class PromoItem {
        public final String imageUrl;
        public final String link;

        public PromoItem(String imageUrl, String link) {
            this.imageUrl = imageUrl;
            this.link = link;
        }
    }

    @Override
    protected void onDestroy() {
        if (promoDialog != null && promoDialog.isShowing()) {
            promoDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            SantaCustomDialog exitDialog = new SantaCustomDialog(
                    this,
                    "normal",
                    1200, R.drawable.ic_exit,
                    "Meninggalkan Aplikasi GaBut?",
                    "Yakin, Ingin menutup aplikasi GaBut ?",
                    "null",
                    "TIDAK",
                    "YA",
                    "null",
                    "null"
            );
            exitDialog.show();
            exitDialog.setOnDialogButtonClickListener(this::finishAffinity);
        } else {
            super.onBackPressed();
        }
    }
}
