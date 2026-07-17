package com.technicaltest.taskman.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.technicaltest.taskman.R;
import com.technicaltest.taskman.data.model.TaskRequest;
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.databinding.DialogConfirmBinding;
import com.technicaltest.taskman.databinding.DialogEditTaskBinding;
import com.technicaltest.taskman.databinding.DialogMessageBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DialogUtils {

    public interface OnConfirmListener {
        void onConfirm();
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public interface OnTaskSaveListener {
        void onSave(TaskRequest request);
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

    private static void updateTypeSelection(Context context, DialogEditTaskBinding dialogBinding, String type, String[] selectedType) {
        selectedType[0] = type;
        int activeColor = ContextCompat.getColor(context, R.color.primary);
        int inactiveColor = ContextCompat.getColor(context, R.color.white);
        int activeTextColor = ContextCompat.getColor(context, R.color.white);
        int inactiveTextColor = ContextCompat.getColor(context, R.color.text_primary);
        int inactiveBorderColor = ContextCompat.getColor(context, R.color.border);

        dialogBinding.btnTypeHarian.setCardBackgroundColor(inactiveColor);
        dialogBinding.btnTypeHarian.setStrokeColor(inactiveBorderColor);
        dialogBinding.ivTypeHarian.setColorFilter(activeColor);
        dialogBinding.tvTypeHarianTitle.setTextColor(inactiveTextColor);

        dialogBinding.btnTypeMingguan.setCardBackgroundColor(inactiveColor);
        dialogBinding.btnTypeMingguan.setStrokeColor(inactiveBorderColor);
        dialogBinding.ivTypeMingguan.setColorFilter(activeColor);
        dialogBinding.tvTypeMingguanTitle.setTextColor(inactiveTextColor);

        dialogBinding.btnTypeBulanan.setCardBackgroundColor(inactiveColor);
        dialogBinding.btnTypeBulanan.setStrokeColor(inactiveBorderColor);
        dialogBinding.ivTypeBulanan.setColorFilter(activeColor);
        dialogBinding.tvTypeBulananTitle.setTextColor(inactiveTextColor);

        if (type.equalsIgnoreCase("Harian")) {
            dialogBinding.btnTypeHarian.setCardBackgroundColor(activeColor);
            dialogBinding.btnTypeHarian.setStrokeColor(activeColor);
            dialogBinding.ivTypeHarian.setColorFilter(activeTextColor);
            dialogBinding.tvTypeHarianTitle.setTextColor(activeTextColor);
        } else if (type.equalsIgnoreCase("Mingguan")) {
            dialogBinding.btnTypeMingguan.setCardBackgroundColor(activeColor);
            dialogBinding.btnTypeMingguan.setStrokeColor(activeColor);
            dialogBinding.ivTypeMingguan.setColorFilter(activeTextColor);
            dialogBinding.tvTypeMingguanTitle.setTextColor(activeTextColor);
        } else if (type.equalsIgnoreCase("Bulanan")) {
            dialogBinding.btnTypeBulanan.setCardBackgroundColor(activeColor);
            dialogBinding.btnTypeBulanan.setStrokeColor(activeColor);
            dialogBinding.ivTypeBulanan.setColorFilter(activeTextColor);
            dialogBinding.tvTypeBulananTitle.setTextColor(activeTextColor);
        }
    }

    private static void updateStatusSelection(Context context, DialogEditTaskBinding dialogBinding, String status, String[] selectedStatus) {
        selectedStatus[0] = status;
        int activeColor = ContextCompat.getColor(context, R.color.primary);
        int inactiveColor = ContextCompat.getColor(context, R.color.white);
        int activeTextColor = ContextCompat.getColor(context, R.color.white);
        int inactiveTextColor = ContextCompat.getColor(context, R.color.text_primary);
        int inactiveBorderColor = ContextCompat.getColor(context, R.color.border);

        dialogBinding.btnStatusPending.setCardBackgroundColor(inactiveColor);
        dialogBinding.btnStatusPending.setStrokeColor(inactiveBorderColor);
        dialogBinding.ivStatusPending.setColorFilter(activeColor);
        dialogBinding.tvStatusPending.setTextColor(inactiveTextColor);

        dialogBinding.btnStatusDone.setCardBackgroundColor(inactiveColor);
        dialogBinding.btnStatusDone.setStrokeColor(inactiveBorderColor);
        dialogBinding.ivStatusDone.setColorFilter(activeColor);
        dialogBinding.tvStatusDone.setTextColor(inactiveTextColor);

        if (status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Selesai")) {
            dialogBinding.btnStatusDone.setCardBackgroundColor(activeColor);
            dialogBinding.btnStatusDone.setStrokeColor(activeColor);
            dialogBinding.ivStatusDone.setColorFilter(activeTextColor);
            dialogBinding.tvStatusDone.setTextColor(activeTextColor);
        } else {
            dialogBinding.btnStatusPending.setCardBackgroundColor(activeColor);
            dialogBinding.btnStatusPending.setStrokeColor(activeColor);
            dialogBinding.ivStatusPending.setColorFilter(activeTextColor);
            dialogBinding.tvStatusPending.setTextColor(activeTextColor);
        }
    }

    public static void showTaskBottomSheet(Context context, LayoutInflater inflater, TaskResponse task, OnTaskSaveListener saveListener) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        DialogEditTaskBinding dialogBinding = DialogEditTaskBinding.inflate(inflater);
        dialog.setContentView(dialogBinding.getRoot());

        final String[] selectedType = { "Harian" };
        final String[] selectedStatus = { "Pending" };

        if (task == null) {
            dialogBinding.tvDialogTitle.setText("Buat task baru");
            dialogBinding.tvDialogSubtitle.setText("Isi detail task dengan lengkap agar lebih terorganisir");
            dialogBinding.btnSaveTask.setText("SIMPAN TASK");

            dialogBinding.etTaskTitle.setText("");
            dialogBinding.etTaskDescription.setText("");
            dialogBinding.etTaskDeadline.setText("");
        } else {
            dialogBinding.tvDialogTitle.setText("Edit task");
            dialogBinding.tvDialogSubtitle.setText("Perbarui detail task sesuai kebutuhan");
            dialogBinding.btnSaveTask.setText("UPDATE TASK");

            dialogBinding.etTaskTitle.setText(task.getTitle());
            dialogBinding.etTaskDescription.setText(task.getDescription());
            dialogBinding.etTaskDeadline.setText(task.getDeadline());

            if (task.getType() != null) {
                selectedType[0] = task.getType().trim();
            }
            if (task.getStatus() != null) {
                selectedStatus[0] = task.getStatus().trim();
            }
        }

        updateTypeSelection(context, dialogBinding, selectedType[0], selectedType);
        updateStatusSelection(context, dialogBinding, selectedStatus[0], selectedStatus);

        dialogBinding.btnTypeHarian.setOnClickListener(v -> updateTypeSelection(context, dialogBinding, "Harian", selectedType));
        dialogBinding.btnTypeMingguan.setOnClickListener(v -> updateTypeSelection(context, dialogBinding, "Mingguan", selectedType));
        dialogBinding.btnTypeBulanan.setOnClickListener(v -> updateTypeSelection(context, dialogBinding, "Bulanan", selectedType));

        dialogBinding.btnStatusPending.setOnClickListener(v -> updateStatusSelection(context, dialogBinding, "Pending", selectedStatus));
        dialogBinding.btnStatusDone.setOnClickListener(v -> updateStatusSelection(context, dialogBinding, "Done", selectedStatus));

        dialogBinding.etTaskTitle.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialogBinding.etTaskTitle.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        dialogBinding.tvCharCount.setText(dialogBinding.etTaskDescription.getText().length() + "/300");
        dialogBinding.etTaskDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialogBinding.tvCharCount.setText(s.length() + "/300");
                dialogBinding.etTaskDescription.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        dialogBinding.etTaskDeadline.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    dialogBinding.btnClearDeadline.setVisibility(View.VISIBLE);
                } else {
                    dialogBinding.btnClearDeadline.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        dialogBinding.btnClearDeadline.setOnClickListener(v -> dialogBinding.etTaskDeadline.setText(""));

        View.OnClickListener datePickerClickListener = v -> {
            Calendar calendar = Calendar.getInstance();
            String currentDeadline = dialogBinding.etTaskDeadline.getText().toString().trim();
            if (!currentDeadline.isEmpty()) {
                try {
                    SimpleDateFormat parser;
                    if (currentDeadline.contains("T")) {
                        parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    } else if (currentDeadline.contains("-")) {
                        parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    } else {
                        parser = new SimpleDateFormat("d MMMM yyyy", new Locale("in", "ID"));
                    }
                    Date parsedDate = parser.parse(currentDeadline);
                    if (parsedDate != null) {
                        calendar.setTime(parsedDate);
                    }
                } catch (Exception e) {
                    // Fallback
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    R.style.CustomDatePickerTheme,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat apiFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        dialogBinding.etTaskDeadline.setText(apiFormatter.format(selectedDate.getTime()));
                        dialogBinding.etTaskDeadline.setError(null);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        };

        dialogBinding.layoutDeadline.setOnClickListener(datePickerClickListener);
        dialogBinding.etTaskDeadline.setOnClickListener(datePickerClickListener);

        dialogBinding.btnSaveTask.setOnClickListener(v -> {
            String title = dialogBinding.etTaskTitle.getText().toString().trim();
            String description = dialogBinding.etTaskDescription.getText().toString().trim();
            String deadline = dialogBinding.etTaskDeadline.getText().toString().trim();

            if (title.isEmpty()) {
                dialogBinding.etTaskTitle.requestFocus();
                dialogBinding.etTaskTitle.setError("Judul task tidak boleh kosong");
                return;
            }

            if (description.isEmpty()) {
                dialogBinding.etTaskDescription.requestFocus();
                dialogBinding.etTaskDescription.setError("Deskripsi task tidak boleh kosong");
                return;
            }

            if (deadline.isEmpty()) {
                dialogBinding.etTaskDeadline.setError("Deadline task tidak boleh kosong");
                dialogBinding.layoutDeadline.requestFocus();
                return;
            }

            TaskRequest request = new TaskRequest(title, description, selectedStatus[0], selectedType[0], deadline);
            if (saveListener != null) {
                saveListener.onSave(request);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
