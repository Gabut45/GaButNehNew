package com.ujicoba.gabut.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.ActivityPermissionBinding;
import com.ujicoba.gabut.utils.Methods;

import java.util.Map;

public class PermissionActivity extends AppCompatActivity {
    private ActivityPermissionBinding binding;

    private final String[] permissions = {
            Manifest.permission.POST_NOTIFICATIONS
    };

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Methods.setFullScreen(this);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::onPermissionResult);

        binding.okBtn.setOnClickListener(v -> {
            if (arePermissionsGranted()) {
                gotoNextActivity();
            } else {
                requestPermissionLauncher.launch(permissions);
            }
        });

        binding.skipBtn.setOnClickListener(v -> showSkipConfirmationDialog());
    }

    private boolean arePermissionsGranted() {
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    permission.equals(Manifest.permission.POST_NOTIFICATIONS)) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void onPermissionResult(Map<String, Boolean> permissionsMap) {
        boolean allGranted = true;
        for (Boolean granted : permissionsMap.values()) {
            if (!granted) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) {
            gotoNextActivity();
        } else {
            showPermissionDeniedDialog();
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Ketidakcocokan Pilihan")
                .setMessage("Izin notifikasi belum diaktifkan. Anda tetap dapat melanjutkan, namun Anda mungkin tidak akan menerima pemberitahuan mengenai status hadiah atau penukaran berlian.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showSkipConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_skip_confirmation, null);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        btnPositive.setOnClickListener(v -> {
            alertDialog.dismiss();
            gotoNextActivity();
        });

        btnNegative.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void gotoNextActivity() {
        Methods.gotoNextActivity(this, LoginActivity.class);
        finish();
    }
}
