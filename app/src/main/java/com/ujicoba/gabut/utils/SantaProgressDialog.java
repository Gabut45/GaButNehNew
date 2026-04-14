package com.ujicoba.gabut.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

import com.ujicoba.gabut.R;

public class SantaProgressDialog extends Dialog {

    public SantaProgressDialog(Context context) {
        super(context);
        setContentView(R.layout.layout_progress_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_progress_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void setMessage(String message) {
        TextView progressText = findViewById(R.id.progress_text);
        progressText.setText(message);
    }

//    public void setProgress(int progress) {
//        ProgressBar progressBar = findViewById(R.id.progress_bar);
//        progressBar.setProgress(progress);
//    }
}
