package com.technicaltest.taskman.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.LoginResponse;
import com.technicaltest.taskman.data.repository.AuthRepository;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        SessionManager sessionManager = new SessionManager(application);
        this.authRepository = new AuthRepository(sessionManager);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email cannot be empty");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password cannot be empty");
            return;
        }

        isLoading.setValue(true);
        authRepository.login(email, password, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                isLoading.setValue(false);
                loginResponse.setValue(response);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}
