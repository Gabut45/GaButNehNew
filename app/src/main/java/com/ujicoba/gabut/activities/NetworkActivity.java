package com.ujicoba.gabut.activities;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.ActivityNetworkBinding;
import com.ujicoba.gabut.utils.Methods;

public class NetworkActivity extends AppCompatActivity  {
    ActivityNetworkBinding binding;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNetworkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        binding.btnRetry.setOnClickListener(view -> {
            if (Methods.isNetworkConnected(this)){
                finish();
            } else {
                Toast.makeText(this, R.string.msg_please_try_again_later, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}