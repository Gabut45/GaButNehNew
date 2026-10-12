package com.ujicoba.gabut.activities;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ujicoba.gabut.R;

import java.util.List;

public class CustomShowcaseView extends FrameLayout {

    private final List<ShowcaseItem> items;
    private int currentIndex = 0;

    private final Paint backgroundPaint;
    private final Paint clearPaint;
    private Paint borderPaint;

    private float glowShift = 0f;
    private ValueAnimator glowAnimator;

    private View popupView;

    public CustomShowcaseView(Context context, List<ShowcaseItem> items) {
        super(context);

        this.items = items;

        setWillNotDraw(false);

        // Blok semua interaksi background
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#CC120024"));

        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6f);
        borderPaint.setAntiAlias(true);

        startGlowAnimation();

        initPopup();
    }

    private void startGlowAnimation() {
        glowAnimator = ValueAnimator.ofFloat(0f, 1f);
        glowAnimator.setDuration(2500);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setInterpolator(new LinearInterpolator());

        glowAnimator.addUpdateListener(animation -> {
            glowShift = (float) animation.getAnimatedValue();
            invalidate();
        });

        glowAnimator.start();
    }

    private void stopGlowAnimation() {
        if (glowAnimator != null) {
            glowAnimator.cancel();
        }
    }

    private void initPopup() {
        popupView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_showcase_popup, this, false);

        addView(popupView);

        Button btnNext = popupView.findViewById(R.id.btnNext);
        TextView btnSkip = popupView.findViewById(R.id.btnSkip);

        btnNext.setOnClickListener(v -> nextStep());

        btnSkip.setOnClickListener(v -> {
            stopGlowAnimation();

            ViewGroup parent = (ViewGroup) getParent();
            if (parent != null) {
                parent.removeView(this);
            }
        });

        showStep();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (popupView != null) {
            int[] location = new int[2];
            popupView.getLocationOnScreen(location);

            float x = ev.getRawX();
            float y = ev.getRawY();

            boolean insidePopup =
                    x >= location[0]
                            && x <= location[0] + popupView.getWidth()
                            && y >= location[1]
                            && y <= location[1] + popupView.getHeight();

            if (insidePopup) {
                return super.dispatchTouchEvent(ev);
            }
        }

        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.saveLayer(
                0,
                0,
                getWidth(),
                getHeight(),
                null
        );

        canvas.drawRect(
                0,
                0,
                getWidth(),
                getHeight(),
                backgroundPaint
        );

        if (currentIndex < items.size()) {
            ShowcaseItem item = items.get(currentIndex);

            int[] location = new int[2];
            item.targetView.getLocationOnScreen(location);

            float cx = location[0] + item.targetView.getWidth() / 2f;
            float cy = location[1] + item.targetView.getHeight() / 2f;

            float radius = Math.max(
                    item.targetView.getWidth(),
                    item.targetView.getHeight()
            ) * 0.8f;

            // Spotlight clear
            canvas.drawCircle(cx, cy, radius, clearPaint);

            // Glow border animated
            float radiusBorder = radius + 12f;

            LinearGradient gradient = new LinearGradient(
                    cx - radiusBorder,
                    cy - radiusBorder,
                    cx + radiusBorder,
                    cy + radiusBorder,
                    new int[]{
                            Color.parseColor("#FF7F3DFF"),
                            Color.parseColor("#FFC77DFF"),
                            Color.parseColor("#FF7F3DFF")
                    },
                    new float[]{
                            Math.max(0f, glowShift - 0.3f),
                            glowShift,
                            Math.min(1f, glowShift + 0.3f)
                    },
                    Shader.TileMode.CLAMP
            );

            borderPaint.setShader(gradient);

            canvas.drawCircle(
                    cx,
                    cy,
                    radiusBorder,
                    borderPaint
            );
        }

        canvas.restoreToCount(save);

        super.dispatchDraw(canvas);
    }

    private void showStep() {
        if (currentIndex >= items.size()) return;

        ShowcaseItem item = items.get(currentIndex);

        TextView tvStep = popupView.findViewById(R.id.tvStep);
        TextView tvTitle = popupView.findViewById(R.id.tvTitle);
        TextView tvDescription = popupView.findViewById(R.id.tvDescription);
        Button btnNext = popupView.findViewById(R.id.btnNext);

        tvStep.setText("Step " + item.step + " of " + item.totalSteps);
        tvTitle.setText(item.title);
        tvDescription.setText(item.description);

        if (currentIndex == items.size() - 1) {
            btnNext.setText("Finish");
        } else {
            btnNext.setText("Next");
        }

        int[] location = new int[2];
        item.targetView.getLocationOnScreen(location);

        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        int targetTop = location[1];
        int targetBottom = location[1] + item.targetView.getHeight();

        popupView.measure(
                MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST)
        );

        int popupHeight = popupView.getMeasuredHeight();

        LayoutParams params = (LayoutParams) popupView.getLayoutParams();

        if (targetTop > screenHeight * 0.6) {

            params.gravity = android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL;
            params.topMargin = Math.max(
                    60,
                    targetTop - popupHeight - 40
            );

        } else {

            params.gravity = android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL;
            params.topMargin = targetBottom + 40;
        }

        popupView.setLayoutParams(params);

        // Popup animation
        popupView.setAlpha(0f);
        popupView.setScaleX(0.85f);
        popupView.setScaleY(0.85f);

        popupView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(450)
                .start();

        invalidate();
    }

    private void nextStep() {
        if (currentIndex < items.size() - 1) {
            currentIndex++;
            showStep();
        } else {

            stopGlowAnimation();

            ViewGroup parent = (ViewGroup) getParent();
            if (parent != null) {
                parent.removeView(this);
            }
        }
    }
}