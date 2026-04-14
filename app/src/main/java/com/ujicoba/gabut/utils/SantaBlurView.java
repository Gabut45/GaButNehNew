package com.ujicoba.gabut.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

public class SantaBlurView extends ConstraintLayout {

    /**
     * This version replaces the blur effect with a sci-fi style dark overlay.
     * Written by Santa Coder, modified for dark sci-fi overlay on 11/07/2025
     */
    private int alphaLevel = 10; // 0–255, makin besar makin gelap

    public SantaBlurView(Context context) {
        super(context);
        setSciFiDarkOverlay();
    }

    public SantaBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSciFiDarkOverlay();
    }

    public SantaBlurView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSciFiDarkOverlay();
    }

    /**
     * Apply a dark sci-fi-style overlay with customizable transparency.
     */
    public void setDarkOverlay(int alpha) {
        this.alphaLevel = alpha;
        setBackgroundColor(Color.argb(alphaLevel, 0, 0, 0));
    }

    /**
     * Default sci-fi-style dark transparent overlay (bluish tint optional).
     */
    public void setSciFiDarkOverlay() {
        // Bisa diganti jadi biru keunguan kalau ingin efek sci-fi lebih kuat:
        // setBackgroundColor(Color.argb(alphaLevel, 20, 20, 40)); // biru keunguan
        setBackgroundColor(Color.argb(alphaLevel, 0, 0, 0)); // hitam gelap transparan
    }
}
