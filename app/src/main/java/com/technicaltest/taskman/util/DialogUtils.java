package com.technicaltest.taskman.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import com.technicaltest.taskman.R;
import com.technicaltest.taskman.databinding.DialogConfirmBinding;
import com.technicaltest.taskman.databinding.DialogMessageBinding;

public class DialogUtils {

    public interface OnConfirmListener {
        void onConfirm();
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public static void showConfirmDialog(Context context, String title, String subTitle, int imageRes, OnConfirmListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogConfirmBinding binding = DialogConfirmBinding.inflate(LayoutInflater.from(context));
        builder.setView(binding.getRoot());

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        binding.tvTitleText.setText(title);
        binding.subTitle.setText(subTitle);
        if (imageRes != 0) {
            binding.ivType.setImageResource(imageRes);
        } else {
            binding.ivType.setImageResource(R.drawable.img_question);
        }

        binding.tvCancel.setOnClickListener(v -> dialog.dismiss());
        binding.btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onConfirm();
            }
        });

        dialog.show();
    }


    public static void showMessageDialog(Context context, String title, String subTitle, int imageRes, OnDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogMessageBinding binding = DialogMessageBinding.inflate(LayoutInflater.from(context));
        builder.setView(binding.getRoot());

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        binding.tvTitleText.setText(title);
        binding.subTitle.setText(subTitle);
        if (imageRes != 0) {
            binding.ivResult.setImageResource(imageRes);
        }

        binding.btnNext.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onDismiss();
            }
        });

        dialog.show();
    }

    public static void showSuccessDialog(Context context, String title, String subTitle, OnDismissListener listener) {
        showMessageDialog(context, title, subTitle, R.drawable.ic_setuju, listener);
    }

    public static void showErrorDialog(Context context, String title, String subTitle, OnDismissListener listener) {
        showMessageDialog(context, title, subTitle, R.drawable.ic_tolak, listener);
    }
}
