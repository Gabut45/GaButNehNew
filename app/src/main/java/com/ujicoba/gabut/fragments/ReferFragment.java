package com.ujicoba.gabut.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ujicoba.gabut.api.Data;
import com.ujicoba.gabut.databinding.FragmentReferBinding;
import com.ujicoba.gabut.utils.Methods;

public class ReferFragment extends Fragment {
    public ReferFragment() {
        // Required empty public constructor
    }
    FragmentReferBinding binding;
    private final String TAG = ReferFragment.class.getSimpleName();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReferBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        binding.referCodeText.setText(Methods.getData(getContext(), Data.USER_REFERRAL_CODE));
        binding.referDesc.setText(Methods.getData(getContext(), Data.REFER_DESC));
        binding.copyBtn.setOnClickListener(view -> {
            String code = binding.referCodeText.getText().toString();
            ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("text", code.toUpperCase());
            clipboardManager.setPrimaryClip(clipData);
        });
        binding.inviteBtn.setOnClickListener(view -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    Methods.getData(getContext(), Data.SHARE_MSG)+
                            " \n\nDownload Link: https://play.google.com/store/apps/details?id="+
                            requireContext().getPackageName() +
                            " \nReferral Code: "+Methods.getData(getContext(), Data.USER_REFERRAL_CODE).toUpperCase());
            startActivity(Intent.createChooser(shareIntent, "Share"));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}