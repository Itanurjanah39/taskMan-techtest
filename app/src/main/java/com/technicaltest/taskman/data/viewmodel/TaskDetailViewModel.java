package com.technicaltest.taskman.data.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.TaskDetailResponse;
import com.technicaltest.taskman.data.repository.TaskRepository;
import com.technicaltest.taskman.data.network.Resource;

public class TaskDetailViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private final MutableLiveData<Resource<TaskDetailResponse>> detailResult = new MutableLiveData<>();

    public TaskDetailViewModel(@NonNull Application application) {
        super(application);
        SessionManager sessionManager = new SessionManager(application);
        this.taskRepository = new TaskRepository(sessionManager);
    }

    public LiveData<Resource<TaskDetailResponse>> getDetailResult() {
        return detailResult;
    }

    public void loadTaskDetail(String id) {
        taskRepository.getTaskDetail(id, resource -> detailResult.setValue(resource));
    }
}
