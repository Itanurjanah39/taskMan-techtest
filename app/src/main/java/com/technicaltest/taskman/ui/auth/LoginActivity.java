package com.technicaltest.taskman.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.technicaltest.taskman.MainActivity;
import com.technicaltest.taskman.R;
import com.technicaltest.taskman.data.viewmodel.LoginViewModel;
import com.technicaltest.taskman.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnLogin.setEnabled(!isLoading);
            if (isLoading) {
                binding.btnLogin.setText("Loading...");
            } else {
                binding.btnLogin.setText(R.string.login);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoginResponse().observe(this, response -> {
            if (response != null && response.isSuccess()) {
                Toast.makeText(LoginActivity.this, "Login successful: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            viewModel.login(email, password);
        });

        binding.icView.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            android.graphics.Typeface typeface = binding.etPassword.getTypeface();
            if (isPasswordVisible) {
                // Show password
                binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.icView.setImageResource(R.drawable.ic_eye_crossed);
            } else {
                // Hide password
                binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.icView.setImageResource(R.drawable.ic_eye);
            }
            binding.etPassword.setTypeface(typeface);
            binding.etPassword.setSelection(binding.etPassword.getText().length());
        });
    }
}