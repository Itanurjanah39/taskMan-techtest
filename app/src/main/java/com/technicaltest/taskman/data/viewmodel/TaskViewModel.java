package com.technicaltest.taskman.data.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.data.repository.TaskRepository;
import com.technicaltest.taskman.utils.Resource;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private final MutableLiveData<Resource<List<TaskResponse>>> tasksResult = new MutableLiveData<>();

    public TaskViewModel(@NonNull Application application) {
        super(application);
        SessionManager sessionManager = new SessionManager(application);
        this.taskRepository = new TaskRepository(sessionManager);
    }

    public LiveData<Resource<List<TaskResponse>>> getTasksResult() {
        return tasksResult;
    }

    public void loadTasks() {
        taskRepository.getTasks(resource -> tasksResult.setValue(resource));
    }
}
