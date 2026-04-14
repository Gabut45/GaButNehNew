package com.ujicoba.gabut.activities;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.ujicoba.gabut.BuildConfig;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.adapters.TasksAdapter;
import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.api.SantaCoderJsonObjRequest;
import com.ujicoba.gabut.databinding.ActivityTaskBinding;
import com.ujicoba.gabut.databinding.LayoutProfileBinding;
import com.ujicoba.gabut.models.TasksModel;
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

public class TaskActivity extends AppCompatActivity {

    private ActivityTaskBinding binding;
    private LayoutProfileBinding profileBinding;
    private final String TAG = TaskActivity.class.getSimpleName();

    private List<TasksModel> taskList;
    private TasksAdapter adapter;
    private int currentPos;
    private boolean isTaskStarted = false;
    private long startTimeMillis;
    private long requiredMillis;
    private String adType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Methods.setFullScreen(this);

        adType = Methods.getData(this, Data.ADS_NETWORK_NAME);
        initProfile();
        preloadInterstitial(adType);

        if (Methods.isNetworkConnected(this)) {
            loadTasks();
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

    private void preloadInterstitial(String type) {
        if (type.equalsIgnoreCase("google_ads_manager")) {
            AdsManager.loadInterstitialAds(this);
        } else if (type.equalsIgnoreCase("admob")) {
            AdmobManager.loadInterstitialAd(this);
        } else if (type.equalsIgnoreCase("applovin")) {
            ApplovinManager.loadApplovinInterstitial(this);
        }
    }

    private void loadTasks() {
        taskList = new ArrayList<>();
        adapter = new TasksAdapter(taskList, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnTaskItemClickListener((url, pos) -> {
            currentPos = pos;
            TasksModel task = taskList.get(pos);

            int cost = 0;
            try { cost = Integer.parseInt(task.getTask_cost()); } catch (Exception ignored) {}

            if (cost > 0) {
                showTaskConfirmationDialog(task);
            } else {
                requiredMillis = Long.parseLong(task.getTask_time()) * 60 * 1000;
                startTimeMillis = System.currentTimeMillis();
                isTaskStarted = true;
                startTask(task.getTask_link());
            }
        });

        getTasksFromServer();
    }

    private void getTasksFromServer() {
        JSONObject params = new JSONObject();
        try {
            params.put("uid", Methods.getData(this, Data.USER_ID));
        } catch (JSONException e) { e.printStackTrace(); }

        SantaCoderJsonObjRequest request = new SantaCoderJsonObjRequest(
                Request.Method.POST,
                BuildConfig.MAIN_URL + "/aapi_files/api_tasks.php",
                params,
                response -> {
                    try {
                        if (response.getString("status").equals("1")) {
                            JSONArray array = response.getJSONArray("tasks_data");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                taskList.add(new TasksModel(
                                        obj.getInt("id"),
                                        obj.getString("task_logo"),
                                        obj.getString("task_name"),
                                        obj.getString("task_desc"),
                                        obj.getString("task_coin"),
                                        obj.optString("task_cost", "0"),
                                        obj.getString("task_time"),
                                        obj.getString("task_link")
                                ));
                            }
                        } else {
                            binding.taskNotAvailableLyt.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.GONE);
                            binding.btnOk.setOnClickListener(view -> finish());
                        }
                        adapter.showShimmer = false;
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) { e.printStackTrace(); }
                },
                error -> Methods.showErrorResponse(TAG, error, this)
        );
        AppClass.getInstance(this).addToRequestQueue(request, TAG);
    }

    // 🔹 Tampilkan konfirmasi biaya sebelum membuka tugas
    private void showTaskConfirmationDialog(TasksModel task) {
        String message = "<b>Untuk membuka tugas \"" + task.getTask_name() + "\"</b><br><br>" +
                "💎 <b>Biaya:</b> " + task.getTask_cost() + " berlian<br>" +
                "🎁 <b>Hadiah:</b> +" + task.getTask_coin() + " berlian<br>" +
                "⏱️ <b>Durasi minimal:</b> " + task.getTask_time() + " menit<br><br>" +
                "⛔ <b>PENTING: Biaya " + task.getTask_cost()  + " berlian akan hangus jika Anda keluar atau durasi bermain kurang dari " + task.getTask_time() + " menit</b><br><br>" +
                "Apakah kamu ingin melanjutkan?";

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Buka Tugas")
                .setMessage(android.text.Html.fromHtml(message))
                .setCancelable(false)
                .setPositiveButton("Lanjutkan", (dialog, which) -> {
                    dialog.dismiss();
                    startTaskWithCost(task);
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // 🔹 Proses pemotongan biaya ke server sebelum membuka tugas
    private void startTaskWithCost(TasksModel task) {
        String userId = Methods.getData(this, Data.USER_ID);
        String url = BuildConfig.MAIN_URL + "/aapi_files/api_start_task.php?user_id=" + userId + "&task_id=" + task.getId();

        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Memproses biaya tugas...");
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
                            requiredMillis = Long.parseLong(task.getTask_time()) * 60 * 1000;
                            startTimeMillis = System.currentTimeMillis();
                            isTaskStarted = true;
                            startTask(task.getTask_link());
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

    private void startTask(String url) {
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
        Methods.checkUserBlockNow(this);

        if (isTaskStarted) {
            isTaskStarted = false;
            long elapsed = System.currentTimeMillis() - startTimeMillis;
            TasksModel task = taskList.get(currentPos);

            if (elapsed >= requiredMillis) {
                showRewardDialog(task);
            } else {
                showInsufficientDialog(task, (requiredMillis - elapsed) / 1000);
            }
        }
    }

    private void showRewardDialog(TasksModel task) {
        SantaCustomDialog coinDialog = new SantaCustomDialog(
                this,
                "cointanpaiklan",
                1200,
                R.drawable.ic_no_internet,
                getString(R.string.msg_congratulations),
                getString(R.string.msg_you_got) + " " + task.getTask_coin() + " " + getString(R.string.text_coins),
                task.getTask_coin(),
                getString(R.string.btn_ok_thanks),
                getString(R.string.btn_ok_thanks),
                "Enter Referral Code",
                "null"
        );

        coinDialog.setOnDialogButtonClickListener(() -> {
            coinDialog.dismiss();

            Methods.sendRequestToServer(TAG, this, task.getId(), getString(R.string.task_text),
                    profileBinding.userCoins, null, null);

            View itemView = binding.recyclerView.getLayoutManager().findViewByPosition(currentPos);
            if (itemView != null) {
                itemView.setVisibility(View.GONE);
                itemView.getLayoutParams().height = 0;
            }

            // ✅ Tampilkan interstitial
            if (adType.equalsIgnoreCase("google_ads_manager")) {
                AdsManager.showInterstitialAds(this);
                ((AppClass) getApplication()).getAppOpenObserver().markInterstitialShown();
            } else if (adType.equalsIgnoreCase("admob")) {
                AdmobManager.showInterstitialAd(this);
                ((AppClass) getApplication()).getAppOpenObserver().markInterstitialShown();
            } else if (adType.equalsIgnoreCase("applovin")) {
                ApplovinManager.showApplovinInterstitial(this);
                ((AppClass) getApplication()).getAppOpenObserver().markInterstitialShown();
            }

            AppClass.getInstance(this).getAppOpenObserver().markInterstitialShown();
            preloadInterstitial(adType);
        });

        coinDialog.show();
    }

    private void showInsufficientDialog(TasksModel task, long remainSec) {
        SantaCustomDialog dialog = new SantaCustomDialog(
                this,
                "insufficient",
                1200,
                R.drawable.ic_clock,
                getString(R.string.text_insufficient_time),
                "Tunggu minimal " + task.getTask_time() + " menit untuk mendapatkan " + task.getTask_coin() + " berlian.",
                "Sisa waktu kurang: " + remainSec + " detik",
                getString(R.string.btn_cancel),
                "Ulangi Lagi",
                "null",
                "null"
        );

        dialog.setOnDialogButtonClickListener(() -> {
            dialog.dismiss();
            isTaskStarted = true;
            startTimeMillis = System.currentTimeMillis();
            startTask(task.getTask_link());
        });

        dialog.show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Methods.gotoNextActivity(this, MainActivity.class);
        finish();
    }
}
