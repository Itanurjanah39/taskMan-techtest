package com.technicaltest.taskman.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.technicaltest.taskman.MainActivity;
import com.technicaltest.taskman.R;
import com.technicaltest.taskman.data.model.ProfileResponse;
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.data.viewmodel.HomeViewModel;
import com.technicaltest.taskman.databinding.FragmentHomeBinding;
import com.technicaltest.taskman.ui.adapter.TaskAdapter;
import com.technicaltest.taskman.utils.DialogUtils;
import com.technicaltest.taskman.utils.EmptyStateUtils;
import com.technicaltest.taskman.MainActivity;
import com.technicaltest.taskman.ui.task.TaskDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TaskAdapter taskAdapter;
    private HomeViewModel viewModel;
    private List<TaskResponse> allTasks = new ArrayList<>();
    private String currentFilter = "Semua";

    private boolean isProfileLoaded = false;
    private boolean isTasksLoaded = false;
    private boolean isMinTimeElapsed = false;
    private com.technicaltest.taskman.data.network.Resource<ProfileResponse> pendingProfileResource = null;
    private com.technicaltest.taskman.data.network.Resource<List<TaskResponse>> pendingTasksResource = null;

    private void checkAndDisplayData() {
        if (!isAdded()) return;
        if (isProfileLoaded && isTasksLoaded && isMinTimeElapsed) {
            // Process Profile
            if (pendingProfileResource != null && pendingProfileResource.isSuccess() && pendingProfileResource.getData() != null) {
                ProfileResponse profile = pendingProfileResource.getData();
                if (profile.isSuccess() && profile.getData() != null && profile.getData().getEmployee() != null) {
                    String name = profile.getData().getEmployee().getName();
                    binding.tvGreeting.setText("Hi, " + name + " 👋");
                }
            }

            // Process Tasks
            if (pendingTasksResource != null) {
                binding.progressBar.stopShimmer();
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
                                java.time.Instant i1 = com.technicaltest.taskman.utils.DateUtils.parseInstant(c1);
                                java.time.Instant i2 = com.technicaltest.taskman.utils.DateUtils.parseInstant(c2);
                                return i2.compareTo(i1);
                            });
                            updateSummary();
                            filterAndDisplayTasks();
                        }
                        break;
                    case ERROR:
                        binding.rvTasks.setVisibility(View.GONE);
                        binding.showAll.setVisibility(View.GONE);
                        EmptyStateUtils.showEmptyState(
                                binding.layoutEmpty.getRoot(),
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
        setupRefreshLayout();
        setupObservers();


        binding.showAll.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTaskFragment();
            }
        });

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

            @Override
            public void onItemClick(TaskResponse task) {
                Intent intent = new Intent(requireContext(), TaskDetailActivity.class);
                intent.putExtra("TASK_ID", task.getId());
                startActivity(intent);
            }
        });

        binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTasks.setAdapter(taskAdapter);
    }



    private void setupRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadData(false));

        binding.swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.primary)
        );
    }

    private void setupObservers() {
        viewModel.getProfileResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            pendingProfileResource = resource;
            isProfileLoaded = true;
            checkAndDisplayData();
        });

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
                    binding.progressBar.startShimmer();
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.stopShimmer();
                    binding.progressBar.setVisibility(View.GONE);
                    DialogUtils.showSuccessDialog(requireContext(), "Berhasil", "Tugas berhasil dihapus", () -> {
                        viewModel.resetDeleteResult();
                        loadData(false);
                    });
                    break;
                case ERROR:
                    binding.progressBar.stopShimmer();
                    binding.progressBar.setVisibility(View.GONE);
                    DialogUtils.showErrorDialog(requireContext(), "Gagal", "Gagal menghapus tugas: " + resource.getMessage(), () -> {
                        viewModel.resetDeleteResult();
                    });
                    break;
            }
        });
    }

    private void loadData(boolean showProgressBar) {
        if (!isAdded()) return;

        if (showProgressBar) {
            binding.progressBar.startShimmer();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.rvTasks.setVisibility(View.GONE);
            binding.showAll.setVisibility(View.GONE);
            EmptyStateUtils.hideEmptyState(binding.layoutEmpty.getRoot());

            isProfileLoaded = false;
            isTasksLoaded = false;
            isMinTimeElapsed = false;
            pendingProfileResource = null;
            pendingTasksResource = null;

            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                isMinTimeElapsed = true;
                checkAndDisplayData();
            }, 3000);
        } else {
            isProfileLoaded = false;
            isTasksLoaded = false;
            isMinTimeElapsed = true;
            pendingProfileResource = null;
            pendingTasksResource = null;
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
            EmptyStateUtils.hideEmptyState(binding.layoutEmpty.getRoot());
            List<TaskResponse> displayedTasks;
            if (filteredTasks.size() > 10) {
                displayedTasks = filteredTasks.subList(0, 10);
            } else {
                displayedTasks = filteredTasks;
            }
            taskAdapter.setTasks(displayedTasks);
            binding.showAll.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        binding.rvTasks.setVisibility(View.GONE);
        binding.showAll.setVisibility(View.GONE);
        EmptyStateUtils.showEmptyState(
                binding.layoutEmpty.getRoot(),
                "Tidak ada task saat ini",
                R.drawable.img_question,
                null
        );
        taskAdapter.setTasks(new ArrayList<>());
    }

    private void showTaskBottomSheet(TaskResponse task) {
        DialogUtils.showTaskBottomSheet(requireContext(), getLayoutInflater(), task, request -> {
            if (task != null) {
                viewModel.updateTask(task.getId(), request);
            }
        });
    }

    private void showDeleteConfirmationDialog(TaskResponse task) {
        DialogUtils.showConfirmDialog(
                requireContext(),
                "Hapus Tugas",
                "Apakah Anda yakin ingin menghapus tugas \"" + task.getTitle() + "\"?",
                R.drawable.img_question,
                () -> {
                    binding.rvTasks.setVisibility(View.GONE);
                    EmptyStateUtils.hideEmptyState(binding.layoutEmpty.getRoot());
                    binding.progressBar.startShimmer();
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.showAll.setVisibility(View.GONE);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        viewModel.deleteTask(task.getId());
                    }, 2000);
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
