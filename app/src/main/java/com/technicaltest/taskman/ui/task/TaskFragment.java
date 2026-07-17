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

import java.util.ArrayList;
import java.util.List;

public class TaskFragment extends Fragment {

    private FragmentTaskBinding binding;
    private TaskAdapter taskAdapter;
    private TaskViewModel viewModel;
    private List<TaskResponse> allTasks = new ArrayList<>();
    private String currentFilter = "Semua";
    private String searchQuery = "";

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

        // Initial task load
        loadData(true);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onEditClick(TaskResponse task) {
                Toast.makeText(requireContext(), "Edit task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(TaskResponse task) {
                Toast.makeText(requireContext(), "Delete task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
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

            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (resource.getData() != null) {
                        allTasks = resource.getData();
                        filterAndDisplayTasks();
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    showError(resource.getMessage() != null ? resource.getMessage() : "Gagal mengambil task");
                    showEmptyState();
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
            binding.layoutEmpty.setVisibility(View.GONE);
            taskAdapter.setTasks(filteredTasks);
        }
    }

    private void showEmptyState() {
        binding.rvTasks.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        taskAdapter.setTasks(new ArrayList<>());
    }

    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
