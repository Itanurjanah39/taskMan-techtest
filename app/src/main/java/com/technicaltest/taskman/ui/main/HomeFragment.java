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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.technicaltest.taskman.R;
import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.ProfileResponse;
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.data.network.ApiClient;
import com.technicaltest.taskman.data.network.ApiService;
import com.technicaltest.taskman.data.network.PublicApiClient;
import com.technicaltest.taskman.data.network.PublicApiService;
import com.technicaltest.taskman.databinding.FragmentHomeBinding;
import com.technicaltest.taskman.ui.adapter.TaskAdapter;
import com.technicaltest.taskman.utils.ApiCallback;
import com.technicaltest.taskman.utils.NetworkHelper;
import com.technicaltest.taskman.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TaskAdapter taskAdapter;
    private ApiService apiService;
    private PublicApiService publicApiService;
    private SessionManager sessionManager;
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

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getService(sessionManager);
        publicApiService = PublicApiClient.getService(sessionManager);

        setupRecyclerView();
        setupFilters();
        setupRefreshLayout();

        // Initial data load
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

    private void setupRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadData(false));
        binding.btnRefresh.setOnClickListener(v -> loadData(true));
        
        // Color scheme matching application primary color
        binding.swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.primary)
        );
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

        loadProfile();
        loadTasks();
    }

    private void loadProfile() {
        NetworkHelper.enqueueCall(apiService.getProfile(), new ApiCallback<ProfileResponse>() {
            @Override
            public void onResponse(Resource<ProfileResponse> resource) {
                if (!isAdded()) return;

                if (resource.isSuccess() && resource.getData() != null) {
                    ProfileResponse profile = resource.getData();
                    if (profile.isSuccess() && profile.getData() != null && profile.getData().getEmployee() != null) {
                        String name = profile.getData().getEmployee().getName();
                        binding.tvGreeting.setText("Hi, " + name + " 👋");
                    }
                }
            }
        });
    }

    private void loadTasks() {
        NetworkHelper.enqueueCall(publicApiService.getTasks(), new ApiCallback<List<TaskResponse>>() {
            @Override
            public void onResponse(Resource<List<TaskResponse>> resource) {
                if (!isAdded()) return;

                if (resource.isLoading()) {
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (resource.isSuccess() && resource.getData() != null) {
                    allTasks = resource.getData();
                    updateSummary();
                    filterAndDisplayTasks();
                } else if (resource.isError()) {
                    showError(resource.getMessage() != null ? resource.getMessage() : "Gagal mengambil task");
                    showEmptyState();
                }
            }
        });
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
