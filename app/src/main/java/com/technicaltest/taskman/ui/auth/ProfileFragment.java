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
        viewModel.loadProfile();
    }

    private void setupListeners() {
        binding.btnProfile.setOnClickListener(v -> showProfileDetailBottomSheet());

        binding.layoutLogout.setOnClickListener(v -> showLogoutConfirmationDialog());


        binding.layoutSyarat.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Syarat dan Ketentuan belum tersedia", Toast.LENGTH_SHORT).show()
        );

        binding.layoutKebijakan.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Kebijakan Privasi belum tersedia", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupObservers() {
        viewModel.getProfileResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    if (!binding.swipeRefresh.isRefreshing()) {
                        binding.swipeRefresh.setRefreshing(true);
                    }
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    if (resource.getData() != null && resource.getData().isSuccess() && resource.getData().getData() != null) {
                        currentProfile = resource.getData().getData();
                        displayProfileData(currentProfile.getEmployee());
                    } else {
                        String msg = resource.getData() != null ? resource.getData().getMessage() : "Gagal memuat profil";
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), resource.getMessage() != null ? resource.getMessage() : "Koneksi bermasalah", Toast.LENGTH_SHORT).show();
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

    private void displayProfileData(ProfileResponse.Employee employee) {
        binding.tvName.setText(employee.getName());
        binding.tvRole.setText(employee.getRole());
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