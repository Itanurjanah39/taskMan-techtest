package com.technicaltest.taskman.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.GenericApiResponse;
import com.technicaltest.taskman.data.model.ProfileResponse;
import com.technicaltest.taskman.data.repository.AuthRepository;
import com.technicaltest.taskman.utils.Resource;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final SessionManager sessionManager;
    private final MutableLiveData<Resource<ProfileResponse>> profileResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<GenericApiResponse>> logoutResult = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.sessionManager = new SessionManager(application);
        this.authRepository = new AuthRepository(sessionManager);
    }

    public LiveData<Resource<ProfileResponse>> getProfileResult() {
        return profileResult;
    }

    public LiveData<Resource<GenericApiResponse>> getLogoutResult() {
        return logoutResult;
    }

    public void loadProfile() {
        profileResult.setValue(Resource.loading(null));
        authRepository.getProfile(resource -> profileResult.setValue(resource));
    }

    public void logout() {
        logoutResult.setValue(Resource.loading(null));
        authRepository.logout(resource -> {
            if (resource.isSuccess()) {
                sessionManager.clearSession();
            }
            logoutResult.setValue(resource);
        });
    }

    public void clearLocalSession() {
        sessionManager.clearSession();
    }
}
