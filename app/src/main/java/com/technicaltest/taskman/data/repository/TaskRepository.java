package com.technicaltest.taskman.data.repository;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.data.network.PublicApiClient;
import com.technicaltest.taskman.data.network.PublicApiService;
import com.technicaltest.taskman.utils.ApiCallback;
import com.technicaltest.taskman.utils.NetworkHelper;

import java.util.List;

public class TaskRepository {

    private final PublicApiService publicApiService;

    public TaskRepository(SessionManager sessionManager) {
        this.publicApiService = PublicApiClient.getService(sessionManager);
    }

    public void getTasks(ApiCallback<List<TaskResponse>> callback) {
        NetworkHelper.enqueueCall(publicApiService.getTasks(), callback);
    }
}
