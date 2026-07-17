package com.technicaltest.taskman.ui.task;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.data.viewmodel.TaskViewModel;
import com.technicaltest.taskman.databinding.FragmentTaskBinding;
import com.technicaltest.taskman.ui.adapter.TaskAdapter;
import com.technicaltest.taskman.databinding.DialogEditTaskBinding;
import com.technicaltest.taskman.data.model.TaskRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.technicaltest.taskman.util.DialogUtils;
import com.technicaltest.taskman.util.EmptyStateUtils;

import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class TaskFragment extends Fragment {

    private FragmentTaskBinding binding;
    private TaskAdapter taskAdapter;
    private TaskViewModel viewModel;
    private List<TaskResponse> allTasks = new ArrayList<>();
    private String currentFilter = "Semua";
    private String searchQuery = "";

    private boolean isTasksLoaded = false;
    private boolean isMinTimeElapsed = false;
    private com.technicaltest.taskman.data.network.Resource<List<TaskResponse>> pendingTasksResource = null;

    private void checkAndDisplayData() {
        if (!isAdded()) return;
        if (isTasksLoaded && isMinTimeElapsed) {
            if (pendingTasksResource != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                switch (pendingTasksResource.getStatus()) {
                    case SUCCESS:
                        if (pendingTasksResource.getData() != null) {
                            allTasks = pendingTasksResource.getData();
                            // Sort by createdAt descending (newest first)
                            java.util.Collections.sort(allTasks, (o1, o2) -> {
                                String c1 = o1.getCreatedAt();
                                String c2 = o2.getCreatedAt();
                                if (c1 == null && c2 == null) return 0;
                                if (c1 == null) return 1;
                                if (c2 == null) return -1;
                                return c2.compareTo(c1);
                            });
                            filterAndDisplayTasks();
                        }
                        break;
                    case ERROR:
                        binding.rvTasks.setVisibility(View.GONE);
                        EmptyStateUtils.showEmptyState(
                                binding.layoutEmpty,
                                "Gagal mengambil data. Ketuk untuk memuat ulang.",
                                R.drawable.img_question,
                                () -> loadData(true)
                        );
                        taskAdapter.setTasks(new ArrayList<>());
                        break;
                }
            }
        }
    }

    public TaskFragment() {
        // Required empty public constructor
    }

    public static TaskFragment newInstance() {
        return new TaskFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        setupRecyclerView();
        setupFilters();
        setupSearch();
        setupRefreshLayout();
        setupObservers();

        binding.addNewTask.setOnClickListener(v -> showTaskBottomSheet(null));

        // Initial task load
        loadData(true);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onEditClick(TaskResponse task) {
                showTaskBottomSheet(task);
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

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                filterAndDisplayTasks();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }

    private void setupRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadData(false));
        
        // Color scheme matching application primary color
        binding.swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.primary)
        );
    }

    private void setupObservers() {
        viewModel.getTasksResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.getStatus() != com.technicaltest.taskman.data.network.Resource.Status.LOADING) {
                pendingTasksResource = resource;
                isTasksLoaded = true;
                checkAndDisplayData();
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

        viewModel.getCreateResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    DialogUtils.showSuccessDialog(requireContext(), "Berhasil", "Tugas baru berhasil disimpan", () -> {
                        viewModel.resetCreateResult();
                        loadData(false);
                    });
                    break;
                case ERROR:
                    DialogUtils.showErrorDialog(requireContext(), "Gagal", "Gagal menyimpan tugas: " + resource.getMessage(), () -> {
                        viewModel.resetCreateResult();
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
            EmptyStateUtils.hideEmptyState(binding.layoutEmpty);

            isTasksLoaded = false;
            isMinTimeElapsed = false;
            pendingTasksResource = null;

            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                isMinTimeElapsed = true;
                checkAndDisplayData();
            }, 3000);
        } else {
            isTasksLoaded = false;
            isMinTimeElapsed = true;
            pendingTasksResource = null;
        }

        viewModel.loadTasks();
    }

    private void filterAndDisplayTasks() {
        List<TaskResponse> filteredTasks = new ArrayList<>();
        for (TaskResponse task : allTasks) {
            // Status match check
            String status = task.getStatus() != null ? task.getStatus().trim() : "Pending";
            boolean isCompleted = status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Selesai");

            boolean statusMatches = false;
            if (currentFilter.equals("Semua")) {
                statusMatches = true;
            } else if (currentFilter.equals("Pending") && !isCompleted) {
                statusMatches = true;
            } else if (currentFilter.equals("Done") && isCompleted) {
                statusMatches = true;
            }

            // Search text match check
            boolean searchMatches = false;
            if (searchQuery.isEmpty()) {
                searchMatches = true;
            } else {
                String query = searchQuery.toLowerCase();
                String title = task.getTitle() != null ? task.getTitle().toLowerCase() : "";
                String desc = task.getDescription() != null ? task.getDescription().toLowerCase() : "";
                if (title.contains(query) || desc.contains(query)) {
                    searchMatches = true;
                }
            }

            if (statusMatches && searchMatches) {
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
                "Tugas tidak ditemukan",
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

    private String selectedType = "Harian";
    private String selectedStatus = "Pending";

    private void updateTypeSelection(DialogEditTaskBinding dialogBinding, String type) {
        selectedType = type;
        int activeColor = ContextCompat.getColor(requireContext(), R.color.primary);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.white);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary);
        int inactiveSubColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        int inactiveBorderColor = ContextCompat.getColor(requireContext(), R.color.border);


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

        // Set active
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

    private void updateStatusSelection(DialogEditTaskBinding dialogBinding, String status) {
        selectedStatus = status;
        int activeColor = ContextCompat.getColor(requireContext(), R.color.primary);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.white);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary);
        int inactiveBorderColor = ContextCompat.getColor(requireContext(), R.color.border);

        // Reset
        dialogBinding.btnStatusPending.setCardBackgroundColor(inactiveColor);
        dialogBinding.btnStatusPending.setStrokeColor(inactiveBorderColor);
        dialogBinding.ivStatusPending.setColorFilter(activeColor);
        dialogBinding.tvStatusPending.setTextColor(inactiveTextColor);

        dialogBinding.btnStatusDone.setCardBackgroundColor(inactiveColor);
        dialogBinding.btnStatusDone.setStrokeColor(inactiveBorderColor);
        dialogBinding.ivStatusDone.setColorFilter(activeColor);
        dialogBinding.tvStatusDone.setTextColor(inactiveTextColor);

        // Set active
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

    private void showTaskBottomSheet(TaskResponse task) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetTheme);
        DialogEditTaskBinding dialogBinding = DialogEditTaskBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Default state
        selectedType = "Harian";
        selectedStatus = "Pending";

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
                selectedType = task.getType().trim();
            }
            if (task.getStatus() != null) {
                selectedStatus = task.getStatus().trim();
            }
        }

        // Initialize selections
        updateTypeSelection(dialogBinding, selectedType);
        updateStatusSelection(dialogBinding, selectedStatus);

        // Click listeners for type cards
        dialogBinding.btnTypeHarian.setOnClickListener(v -> updateTypeSelection(dialogBinding, "Harian"));
        dialogBinding.btnTypeMingguan.setOnClickListener(v -> updateTypeSelection(dialogBinding, "Mingguan"));
        dialogBinding.btnTypeBulanan.setOnClickListener(v -> updateTypeSelection(dialogBinding, "Bulanan"));

        // Click listeners for status cards
        dialogBinding.btnStatusPending.setOnClickListener(v -> updateStatusSelection(dialogBinding, "Pending"));
        dialogBinding.btnStatusDone.setOnClickListener(v -> updateStatusSelection(dialogBinding, "Done"));

        // Description char counter
        dialogBinding.tvCharCount.setText(dialogBinding.etTaskDescription.getText().length() + "/300");
        dialogBinding.etTaskDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialogBinding.tvCharCount.setText(s.length() + "/300");
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Deadline input clear button visibility
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

        // Save action
        dialogBinding.btnSaveTask.setOnClickListener(v -> {
            String title = dialogBinding.etTaskTitle.getText().toString().trim();
            String description = dialogBinding.etTaskDescription.getText().toString().trim();
            String deadline = dialogBinding.etTaskDeadline.getText().toString().trim();

            if (title.isEmpty()) {
                dialogBinding.etTaskTitle.setError("Judul tidak boleh kosong");
                return;
            }

            TaskRequest request = new TaskRequest(title, description, selectedStatus, selectedType, deadline);
            if (task == null) {
                viewModel.createTask(request);
            } else {
                viewModel.updateTask(task.getId(), request);
            }
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
