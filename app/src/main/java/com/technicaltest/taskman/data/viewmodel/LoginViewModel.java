package com.technicaltest.taskman.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.LoginResponse;
import com.technicaltest.taskman.data.repository.AuthRepository;
import com.technicaltest.taskman.utils.Resource;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<LoginResponse>> loginResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        SessionManager sessionManager = new SessionManager(application);
        this.authRepository = new AuthRepository(sessionManager);
    }

    public LiveData<Resource<LoginResponse>> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            loginResult.setValue(Resource.error("Email cannot be empty", null));
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            loginResult.setValue(Resource.error("Password cannot be empty", null));
            return;
        }

        loginResult.setValue(Resource.loading(null));
        authRepository.login(email, password, resource -> loginResult.setValue(resource));
    }
}
