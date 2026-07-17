package com.technicaltest.taskman.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void loadImage(String url, ImageView imageView, int placeholderResId) {
        if (placeholderResId != 0) {
            imageView.setImageResource(placeholderResId);
        }
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        executor.execute(() -> {
            try {
                InputStream in = new URL(url).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(in);
                handler.post(() -> imageView.setImageBitmap(bmp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
