package com.technicaltest.taskman.ui.task;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.technicaltest.taskman.R;
import com.technicaltest.taskman.data.model.TaskDetailResponse;
import com.technicaltest.taskman.data.viewmodel.TaskDetailViewModel;
import com.technicaltest.taskman.databinding.ActivityTaskDetailBinding;
import com.technicaltest.taskman.utils.EmptyStateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private ActivityTaskDetailBinding binding;
    private TaskDetailViewModel viewModel;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskId = getIntent().getStringExtra("TASK_ID");

        viewModel = new ViewModelProvider(this).get(TaskDetailViewModel.class);

        setupListeners();
        setupObservers();

        if (taskId != null) {
            viewModel.loadTaskDetail(taskId);
        } else {
            showErrorState();
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupObservers() {
        viewModel.getDetailResult().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.scrollView.setVisibility(View.GONE);
                    EmptyStateUtils.hideEmptyState(binding.layoutEmpty.getRoot());
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        binding.scrollView.setVisibility(View.VISIBLE);
                        displayTaskDetail(resource.getData());
                    } else {
                        showErrorState();
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.scrollView.setVisibility(View.GONE);
                    EmptyStateUtils.showEmptyState(
                            binding.layoutEmpty.getRoot(),
                            "Gagal memuat detail tugas: " + resource.getMessage(),
                            R.drawable.img_question,
                            () -> {
                                if (taskId != null) viewModel.loadTaskDetail(taskId);
                            }
                    );
                    break;
            }
        });
    }

    private void displayTaskDetail(TaskDetailResponse task) {
        binding.tvTaskTitle.setText(task.getTitle());
        binding.tvTaskDescription.setText(task.getDescription());

        // Format dates
        binding.tvTaskDeadline.setText(formatDate(task.getDeadline()));
        binding.tvTaskCreatedAt.setText(formatDate(task.getCreatedAt()));

        // Type Badge styling
        String type = task.getType() != null ? task.getType().trim() : "Harian";
        binding.tvTypeBadge.setText(type);

        // Status Badge styling
        String status = task.getStatus() != null ? task.getStatus().trim() : "Pending";
        if (status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Selesai")) {
            binding.tvStatusBadge.setText("Done");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_done);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.done_text));
        } else {
            binding.tvStatusBadge.setText("Pending");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_pending);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.pending_text));
        }
    }

    private void showErrorState() {
        binding.progressBar.setVisibility(View.GONE);
        binding.scrollView.setVisibility(View.GONE);
        EmptyStateUtils.showEmptyState(
                binding.layoutEmpty.getRoot(),
                "Tugas tidak ditemukan",
                R.drawable.img_question,
                null
        );
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "-";
        try {
            SimpleDateFormat parser;
            if (rawDate.contains("T")) {
                parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            } else {
                parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }
            Date date = parser.parse(rawDate);
            if (date != null) {
                Locale idLocale = new Locale("in", "ID");
                SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy, HH:mm", idLocale);
                // check if it has time part, if not show only date
                if (!rawDate.contains("T")) {
                    formatter = new SimpleDateFormat("d MMMM yyyy", idLocale);
                }
                return formatter.format(date);
            }
        } catch (Exception e) {
            // Fallback
        }
        return rawDate;
    }
}
