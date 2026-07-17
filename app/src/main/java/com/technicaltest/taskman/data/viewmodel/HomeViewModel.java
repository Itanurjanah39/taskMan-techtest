package com.technicaltest.taskman.data.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.GenericApiResponse;
import com.technicaltest.taskman.data.model.ProfileResponse;
import com.technicaltest.taskman.data.model.TaskRequest;
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.data.repository.HomeRepository;
import com.technicaltest.taskman.data.network.Resource;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final HomeRepository homeRepository;
    private final MutableLiveData<Resource<ProfileResponse>> profileResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<TaskResponse>>> tasksResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<TaskResponse>> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<GenericApiResponse>> deleteResult = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        SessionManager sessionManager = new SessionManager(application);
        this.homeRepository = new HomeRepository(sessionManager);
    }

    public LiveData<Resource<ProfileResponse>> getProfileResult() {
        return profileResult;
    }

    public LiveData<Resource<List<TaskResponse>>> getTasksResult() {
        return tasksResult;
    }

    public LiveData<Resource<TaskResponse>> getUpdateResult() {
        return updateResult;
    }

    public LiveData<Resource<GenericApiResponse>> getDeleteResult() {
        return deleteResult;
    }

    public void loadProfile() {
        homeRepository.getProfile(resource -> profileResult.setValue(resource));
    }

    public void loadTasks() {
        homeRepository.getTasks(resource -> tasksResult.setValue(resource));
    }

    public void updateTask(String id, TaskRequest request) {
        homeRepository.updateTask(id, request, resource -> updateResult.setValue(resource));
    }

    public void deleteTask(String id) {
        homeRepository.deleteTask(id, resource -> deleteResult.setValue(resource));
    }

    public void resetUpdateResult() {
        updateResult.setValue(null);
    }

    public void resetDeleteResult() {
        deleteResult.setValue(null);
    }
}
