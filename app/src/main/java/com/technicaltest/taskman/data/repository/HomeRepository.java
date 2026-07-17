package com.technicaltest.taskman.data.repository;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.GenericApiResponse;
import com.technicaltest.taskman.data.model.ProfileResponse;
import com.technicaltest.taskman.data.model.TaskRequest;
import com.technicaltest.taskman.data.model.TaskResponse;
import com.technicaltest.taskman.data.network.ApiClient;
import com.technicaltest.taskman.data.network.ApiService;
import com.technicaltest.taskman.data.network.PublicApiClient;
import com.technicaltest.taskman.data.network.PublicApiService;
import com.technicaltest.taskman.data.network.ApiCallback;
import com.technicaltest.taskman.data.network.NetworkHelper;

import java.util.List;

public class HomeRepository {

    private final ApiService apiService;
    private final PublicApiService publicApiService;

    public HomeRepository(SessionManager sessionManager) {
        this.apiService = ApiClient.getService(sessionManager);
        this.publicApiService = PublicApiClient.getService(sessionManager);
    }

    public void getProfile(ApiCallback<ProfileResponse> callback) {
        NetworkHelper.enqueueCall(apiService.getProfile(), callback);
    }

    public void getTasks(ApiCallback<List<TaskResponse>> callback) {
        NetworkHelper.enqueueCall(publicApiService.getTasks(), callback);
    }

    public void updateTask(String id, TaskRequest request, ApiCallback<TaskResponse> callback) {
        NetworkHelper.enqueueCall(publicApiService.updateTask(id, request), callback);
    }

    public void deleteTask(String id, ApiCallback<GenericApiResponse> callback) {
        NetworkHelper.enqueueCall(publicApiService.deleteTask(id), callback);
    }
}
