package com.technicaltest.taskman.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.technicaltest.taskman.R;

public class EmptyStateUtils {

    public interface OnRefreshListener {
        void onRefresh();
    }

    public static void showEmptyState(View emptyLayout, String message, int imageRes, OnRefreshListener refreshListener) {
        if (emptyLayout == null) return;
        emptyLayout.setVisibility(View.VISIBLE);

        ImageView imageView = null;
        TextView textView = null;

        if (emptyLayout instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) emptyLayout;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof ImageView) {
                    imageView = (ImageView) child;
                } else if (child instanceof TextView) {
                    textView = (TextView) child;
                }
            }
        }

        if (textView != null) {
            textView.setText(message);
        }

        if (imageView != null && imageRes != 0) {
            imageView.setImageResource(imageRes);
        }

        if (refreshListener != null) {
            emptyLayout.setClickable(true);
            emptyLayout.setFocusable(true);
            emptyLayout.setOnClickListener(v -> refreshListener.onRefresh());
        } else {
            emptyLayout.setOnClickListener(null);
            emptyLayout.setClickable(false);
            emptyLayout.setFocusable(false);
        }
    }

    public static void showEmptyState(View emptyLayout, String message, int imageRes) {
        showEmptyState(emptyLayout, message, imageRes, null);
    }

    /**
     * Hide empty state.
     */
    public static void hideEmptyState(View emptyLayout) {
        if (emptyLayout != null) {
            emptyLayout.setVisibility(View.GONE);
            emptyLayout.setOnClickListener(null);
            emptyLayout.setClickable(false);
            emptyLayout.setFocusable(false);
        }
    }
}
