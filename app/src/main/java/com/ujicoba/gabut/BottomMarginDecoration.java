package com.ujicoba.gabut;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class BottomMarginDecoration extends RecyclerView.ItemDecoration {
    private final int bottomMargin;

    public BottomMarginDecoration(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // Check if it's the last item
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = bottomMargin;
        }
    }
}
