package com.technicaltest.taskman.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.technicaltest.taskman.R;
import com.technicaltest.taskman.data.model.ProfileResponse;
import com.technicaltest.taskman.data.viewmodel.ProfileViewModel;
import com.technicaltest.taskman.databinding.DialogProfileDetailBinding;
import com.technicaltest.taskman.databinding.FragmentProfileBinding;
import com.technicaltest.taskman.utils.DialogUtils;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private ProfileResponse.Data currentProfile;
    private boolean isProfileLoaded = false;
    private boolean isMinTimeElapsed = false;
    private com.technicaltest.taskman.data.network.Resource<ProfileResponse> pendingProfileResource = null;

    public ProfileFragment() {}

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupRefreshLayout();
        setupListeners();
        setupObservers();


        loadData();
    }

    private void setupRefreshLayout() {
        binding.swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.primary)
        );
        binding.swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        if (!isAdded()) return;

        isProfileLoaded = false;
        isMinTimeElapsed = false;
        pendingProfileResource = null;

        if (!binding.swipeRefresh.isRefreshing()) {
            binding.shimmerProfile.startShimmer();
            binding.shimmerProfile.setVisibility(View.VISIBLE);
            binding.layoutProfileDetails.setVisibility(View.GONE);
        }

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            isMinTimeElapsed = true;
            checkAndDisplayData();
        }, 2000);

        viewModel.loadProfile();
    }

    private void setupListeners() {
        binding.layoutAkun.setOnClickListener(v -> showProfileDetailBottomSheet());

        binding.layoutLogout.setOnClickListener(v -> showLogoutConfirmationDialog());


        binding.layoutSyarat.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TermsConditionsActivity.class);
            startActivity(intent);
        });

        binding.layoutKebijakan.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PrivacyPolicyActivity.class);
            startActivity(intent);
        });
    }

    private void setupObservers() {
        viewModel.getProfileResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    // Handled inside loadData()
                    break;
                case SUCCESS:
                case ERROR:
                    pendingProfileResource = resource;
                    isProfileLoaded = true;
                    checkAndDisplayData();
                    break;
            }
        });

        viewModel.getLogoutResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show();
                    navigateToLoginActivity();
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Logout selesai (offline)", Toast.LENGTH_SHORT).show();
                    viewModel.clearLocalSession();
                    navigateToLoginActivity();
                    break;
            }
        });
    }

    private void checkAndDisplayData() {
        if (!isAdded()) return;
        if (isProfileLoaded && isMinTimeElapsed) {
            binding.swipeRefresh.setRefreshing(false);
            if (pendingProfileResource != null) {
                switch (pendingProfileResource.getStatus()) {
                    case SUCCESS:
                        if (pendingProfileResource.getData() != null && pendingProfileResource.getData().isSuccess() && pendingProfileResource.getData().getData() != null) {
                            binding.shimmerProfile.stopShimmer();
                            binding.shimmerProfile.setVisibility(View.GONE);
                            currentProfile = pendingProfileResource.getData().getData();
                            binding.layoutProfileDetails.setVisibility(View.VISIBLE);
                            displayProfileData(currentProfile.getEmployee());
                        } else {
                            // On failure (invalid data), do not hide details, keep shimmer showing
                            binding.shimmerProfile.startShimmer();
                            binding.shimmerProfile.setVisibility(View.VISIBLE);
                            binding.layoutProfileDetails.setVisibility(View.GONE);
                            String msg = pendingProfileResource.getData() != null ? pendingProfileResource.getData().getMessage() : "Gagal memuat profil";
                            com.technicaltest.taskman.utils.DialogUtils.showErrorDialog(
                                    requireContext(),
                                    "Gagal Memuat Profil",
                                    msg,
                                    () -> {}
                            );
                        }
                        break;
                    case ERROR:
                        // On error, do not hide details, keep shimmer showing
                        binding.shimmerProfile.startShimmer();
                        binding.shimmerProfile.setVisibility(View.VISIBLE);
                        binding.layoutProfileDetails.setVisibility(View.GONE);
                        String errorMsg = pendingProfileResource.getMessage() != null ? pendingProfileResource.getMessage() : "Koneksi bermasalah";
                        com.technicaltest.taskman.utils.DialogUtils.showErrorDialog(
                                requireContext(),
                                "Gagal Memuat Profil",
                                errorMsg,
                                () -> {}
                        );
                        break;
                }
            }
        }
    }

    private void displayProfileData(ProfileResponse.Employee employee) {
        binding.tvName.setText(employee.getName());
        binding.tvRole.setText(employee.getRole());
        binding.tvCompany.setText(employee.getCompany());
    }

    private void showProfileDetailBottomSheet() {

        if (currentProfile == null) {
            Toast.makeText(requireContext(), "Data profil belum dimuat", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog =
                new BottomSheetDialog(requireContext(), R.style.BottomSheetTheme);

        DialogProfileDetailBinding binding =
                DialogProfileDetailBinding.inflate(getLayoutInflater());

        dialog.setContentView(binding.getRoot());

        ProfileResponse.Employee employee = currentProfile.getEmployee();

        binding.tvDetailNik.setText(value(employee.getNik()));
        binding.tvDetailEmail.setText(value(currentProfile.getEmail()));
        binding.tvDetailPhone.setText(value(employee.getPhone()));
        binding.tvDetailCompany.setText(value(employee.getCompany()));
        binding.tvDetailDivision.setText(value(employee.getDivision()));
        binding.tvDetailWorkEntry.setText(value(employee.getWorkEntryDate()));

        binding.btnDismiss.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String value(String text) {
        return text == null || text.isEmpty() ? "-" : text;
    }

    private void showLogoutConfirmationDialog() {
        DialogUtils.showConfirmDialog(
                requireContext(),
                "Keluar Aplikasi",
                "Apakah Anda yakin ingin keluar dari aplikasi?",
                R.drawable.img_question,
                () -> viewModel.logout()
        );
    }

    private void navigateToLoginActivity() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}