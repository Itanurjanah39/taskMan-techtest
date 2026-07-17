package com.technicaltest.taskman.ui.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.technicaltest.taskman.R;
import com.technicaltest.taskman.data.model.ProfileResponse;
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.data.viewmodel.HomeViewModel;
import com.technicaltest.taskman.databinding.FragmentHomeBinding;
import com.technicaltest.taskman.ui.adapter.TaskAdapter;
import com.technicaltest.taskman.databinding.DialogEditTaskBinding;
import com.technicaltest.taskman.data.model.TaskRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.technicaltest.taskman.util.DialogUtils;
import com.technicaltest.taskman.util.EmptyStateUtils;

import android.app.DatePickerDialog;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TaskAdapter taskAdapter;
    private HomeViewModel viewModel;
    private List<TaskResponse> allTasks = new ArrayList<>();
    private String currentFilter = "Semua";

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerView();
        setupFilters();
        setupRefreshLayout();
        setupObservers();

        // Initial data load
        loadData(true);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onEditClick(TaskResponse task) {
                showEditTaskBottomSheet(task);
            }

            @Override
            public void onDeleteClick(TaskResponse task) {
                showDeleteConfirmationDialog(task);
            }
        });

        binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTasks.setAdapter(taskAdapter);
    }

    private void setupFilters() {
        binding.chipSemua.setOnClickListener(v -> handleFilterChange("Semua"));
        binding.chipPending.setOnClickListener(v -> handleFilterChange("Pending"));
        binding.chipDone.setOnClickListener(v -> handleFilterChange("Done"));
    }

    private void setupRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadData(false));
        binding.btnRefresh.setOnClickListener(v -> loadData(true));

        binding.swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.primary)
        );
    }

    private void setupObservers() {
        viewModel.getProfileResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.isSuccess() && resource.getData() != null) {
                ProfileResponse profile = resource.getData();
                if (profile.isSuccess() && profile.getData() != null && profile.getData().getEmployee() != null) {
                    String name = profile.getData().getEmployee().getName();
                    binding.tvGreeting.setText("Hi, " + name + " 👋");
                }
            }
        });

        viewModel.getTasksResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (resource.getData() != null) {
                        allTasks = resource.getData();
                        updateSummary();
                        filterAndDisplayTasks();
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.rvTasks.setVisibility(View.GONE);
                    EmptyStateUtils.showEmptyState(
                            binding.layoutEmpty,
                            "Gagal mengambil data.Swipe Refresh halaman untuk memuat ulang.",
                            R.drawable.img_question,
                            () -> loadData(true)
                    );
                    taskAdapter.setTasks(new ArrayList<>());
                    break;
            }
        });

        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    DialogUtils.showSuccessDialog(requireContext(), "Berhasil", "Tugas berhasil diperbarui", () -> {
                        viewModel.resetUpdateResult();
                        loadData(false);
                    });
                    break;
                case ERROR:
                    DialogUtils.showErrorDialog(requireContext(), "Gagal", "Gagal memperbarui tugas: " + resource.getMessage(), () -> {
                        viewModel.resetUpdateResult();
                    });
                    break;
            }
        });

        viewModel.getDeleteResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    DialogUtils.showSuccessDialog(requireContext(), "Berhasil", "Tugas berhasil dihapus", () -> {
                        viewModel.resetDeleteResult();
                        loadData(false);
                    });
                    break;
                case ERROR:
                    DialogUtils.showErrorDialog(requireContext(), "Gagal", "Gagal menghapus tugas: " + resource.getMessage(), () -> {
                        viewModel.resetDeleteResult();
                    });
                    break;
            }
        });
    }

    private void handleFilterChange(String newFilter) {
        if (currentFilter.equals(newFilter)) return;
        currentFilter = newFilter;
        updateChipUI();
        filterAndDisplayTasks();
    }

    private void updateChipUI() {
        if (!isAdded()) return;

        TextView chipSemua = binding.chipSemua;
        TextView chipPending = binding.chipPending;
        TextView chipDone = binding.chipDone;

        // Reset all to inactive
        chipSemua.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipSemua.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        chipSemua.setTypeface(null, Typeface.NORMAL);

        chipPending.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        chipPending.setTypeface(null, Typeface.NORMAL);

        chipDone.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipDone.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        chipDone.setTypeface(null, Typeface.NORMAL);

        // Apply active state
        if (currentFilter.equals("Semua")) {
            chipSemua.setBackgroundResource(R.drawable.bg_chip_active);
            chipSemua.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            chipSemua.setTypeface(null, Typeface.BOLD);
        } else if (currentFilter.equals("Pending")) {
            chipPending.setBackgroundResource(R.drawable.bg_chip_active);
            chipPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            chipPending.setTypeface(null, Typeface.BOLD);
        } else if (currentFilter.equals("Done")) {
            chipDone.setBackgroundResource(R.drawable.bg_chip_active);
            chipDone.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            chipDone.setTypeface(null, Typeface.BOLD);
        }
    }

    private void loadData(boolean showProgressBar) {
        if (!isAdded()) return;

        if (showProgressBar) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.rvTasks.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.GONE);
        }

        viewModel.loadProfile();
        viewModel.loadTasks();
    }

    private void updateSummary() {
        int total = allTasks.size();
        int completed = 0;
        for (TaskResponse task : allTasks) {
            String status = task.getStatus();
            if (status != null && (status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Selesai"))) {
                completed++;
            }
        }

        binding.tvTotalTaskCount.setText(String.valueOf(total));
        binding.tvCompletedTaskCount.setText(String.valueOf(completed));
    }

    private void filterAndDisplayTasks() {
        List<TaskResponse> filteredTasks = new ArrayList<>();
        for (TaskResponse task : allTasks) {
            String status = task.getStatus() != null ? task.getStatus().trim() : "Pending";
            boolean isCompleted = status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Selesai");

            if (currentFilter.equals("Semua")) {
                filteredTasks.add(task);
            } else if (currentFilter.equals("Pending") && !isCompleted) {
                filteredTasks.add(task);
            } else if (currentFilter.equals("Done") && isCompleted) {
                filteredTasks.add(task);
            }
        }

        if (filteredTasks.isEmpty()) {
            showEmptyState();
        } else {
            binding.rvTasks.setVisibility(View.VISIBLE);
            EmptyStateUtils.hideEmptyState(binding.layoutEmpty);
            taskAdapter.setTasks(filteredTasks);
        }
    }

    private void showEmptyState() {
        binding.rvTasks.setVisibility(View.GONE);
        EmptyStateUtils.showEmptyState(
                binding.layoutEmpty,
                "Tidak ada task saat ini",
                R.drawable.img_question,
                null
        );
        taskAdapter.setTasks(new ArrayList<>());
    }

    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditTaskBottomSheet(TaskResponse task) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetTheme);
        DialogEditTaskBinding dialogBinding = DialogEditTaskBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.etTaskTitle.setText(task.getTitle());
        dialogBinding.etTaskDescription.setText(task.getDescription());
        dialogBinding.etTaskDeadline.setText(task.getDeadline());

        // Setup Spinners
        String[] statusOptions = {"Pending", "Done"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerTaskStatus.setAdapter(statusAdapter);

        if (task.getStatus() != null) {
            String status = task.getStatus().trim();
            if (status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Selesai")) {
                dialogBinding.spinnerTaskStatus.setSelection(1);
            } else {
                dialogBinding.spinnerTaskStatus.setSelection(0);
            }
        }

        String[] typeOptions = {"Harian", "Mingguan", "Bulanan"};
        ArrayList<String> typeList = new ArrayList<>(java.util.Arrays.asList(typeOptions));
        if (task.getType() != null) {
            String type = task.getType().trim();
            if (!typeList.contains(type)) {
                typeList.add(type);
            }
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerTaskType.setAdapter(typeAdapter);

        if (task.getType() != null) {
            dialogBinding.spinnerTaskType.setSelection(typeList.indexOf(task.getType().trim()));
        }

        // Date Picker
        dialogBinding.layoutDeadline.setOnClickListener(v -> {
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
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat apiFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        dialogBinding.etTaskDeadline.setText(apiFormatter.format(selectedDate.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSave.setOnClickListener(v -> {
            String title = dialogBinding.etTaskTitle.getText().toString().trim();
            String description = dialogBinding.etTaskDescription.getText().toString().trim();
            String status = dialogBinding.spinnerTaskStatus.getSelectedItem().toString();
            String type = dialogBinding.spinnerTaskType.getSelectedItem().toString();
            String deadline = dialogBinding.etTaskDeadline.getText().toString().trim();

            if (title.isEmpty()) {
                dialogBinding.etTaskTitle.setError("Judul tidak boleh kosong");
                return;
            }

            TaskRequest request = new TaskRequest(title, description, status, type, deadline);
            viewModel.updateTask(task.getId(), request);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteConfirmationDialog(TaskResponse task) {
        DialogUtils.showConfirmDialog(
                requireContext(),
                "Hapus Tugas",
                "Apakah Anda yakin ingin menghapus tugas \"" + task.getTitle() + "\"?",
                R.drawable.img_question,
                () -> viewModel.deleteTask(task.getId())
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
