package com.ujicoba.gabut.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.ujicoba.gabut.R;

import java.util.Map;

public class PermissionUtils {
    public static void requestPermissions(ComponentActivity activity, String[] permissions, String title, String desc, Runnable onPermissionGranted) {
        ActivityResultLauncher<String[]> requestPermissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionsMap -> {
            boolean allPermissionsGranted = true;
            for (Map.Entry<String, Boolean> entry : permissionsMap.entrySet()) {
                if (!entry.getValue()) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                onPermissionGranted.run();
            } else {
                showPermissionDeniedDialog(activity, title, desc);
            }
        });
        requestPermissionLauncher.launch(permissions);
    }

    public static void showPermissionDeniedDialog(Activity activity, String title, String desc) {
        SantaCustomDialog dialog = new SantaCustomDialog(activity, "normal", 1200, R.drawable.ic_permission, title, desc, "0", "Cancel", "Go to settings", "null", "null");
        dialog.show();
        dialog.setOnDialogButtonClickListener(new SantaCustomDialog.OnDialogButtonClickListener() {
            @Override
            public void onDialogButtonClick() {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        });
    }
}
