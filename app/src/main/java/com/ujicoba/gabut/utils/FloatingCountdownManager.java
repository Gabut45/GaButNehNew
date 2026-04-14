package com.ujicoba.gabut.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.ujicoba.gabut.R;

public class FloatingCountdownManager {

    private static FloatingCountdownManager instance;
    private WindowManager windowManager;
    private View floatingView;
    private Handler handler;
    private long durationMillis;
    private long startTime;
    private Runnable countdownRunnable;

    private boolean isRunning = false;

    private FloatingCountdownManager() {}

    public static FloatingCountdownManager getInstance() {
        if (instance == null) {
            instance = new FloatingCountdownManager();
        }
        return instance;
    }

    public void show(final Context context, long countdownSeconds) {
        if (isRunning) return;

        isRunning = true;
        durationMillis = countdownSeconds * 1000;
        startTime = System.currentTimeMillis();

        handler = new Handler(Looper.getMainLooper());

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        final FrameLayout layout = new FrameLayout(context);
        layout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_circle_timer));
        layout.setAlpha(0.8f);

        final TextView countdownText = new TextView(context);
        countdownText.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        countdownText.setTypeface(Typeface.DEFAULT_BOLD);
        countdownText.setTextSize(16);
        countdownText.setGravity(Gravity.CENTER);

        int size = dpToPx(context, 80);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(size, size);
        countdownText.setLayoutParams(textParams);
        layout.addView(countdownText);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                size,
                size,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        layout.setOnTouchListener(new View.OnTouchListener() {
            private int lastX, lastY;
            private float touchX, touchY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                layout.setAlpha(1.0f); // full opasitas saat disentuh
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = params.x;
                        lastY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - touchX);
                        int dy = (int) (event.getRawY() - touchY);
                        params.x = lastX + dx;
                        params.y = lastY + dy;
                        windowManager.updateViewLayout(layout, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        layout.setAlpha(0.6f); // kembali transparan
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(layout, params);
        floatingView = layout;

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long remaining = durationMillis - (System.currentTimeMillis() - startTime);
                if (remaining > 0) {
                    long seconds = remaining / 1000;
                    countdownText.setText(seconds + "s");
                    handler.postDelayed(this, 1000);
                } else {
                    countdownText.setText("0s");
                    stop();
                }
            }
        };

        handler.post(countdownRunnable);
    }

    public void stop() {
        if (!isRunning) return;

        isRunning = false;

        if (handler != null && countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }

        if (windowManager != null && floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception ignored) {}
        }

        handler = null;
        floatingView = null;
        countdownRunnable = null;
        windowManager = null;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
