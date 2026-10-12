package com.ujicoba.gabut.activities;

import android.view.View;

public class ShowcaseItem {

    public View targetView;
    public String title;
    public String description;
    public int step;
    public int totalSteps;

    public ShowcaseItem(View targetView,
                        String title,
                        String description,
                        int step,
                        int totalSteps) {

        this.targetView = targetView;
        this.title = title;
        this.description = description;
        this.step = step;
        this.totalSteps = totalSteps;
    }
}