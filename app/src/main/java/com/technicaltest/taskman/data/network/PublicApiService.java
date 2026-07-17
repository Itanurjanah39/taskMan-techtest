package com.technicaltest.taskman.data.network;

import com.technicaltest.taskman.data.model.TaskDetailResponse;
import com.technicaltest.taskman.data.model.TaskResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface PublicApiService {

    @Headers("No-Authentication: true")
    @GET("api/tasks")
    Call<List<TaskResponse>> getTasks();

    @Headers("No-Authentication: true")
    @GET("api/tasks/{id}")
    Call<TaskDetailResponse> getTaskDetail(@Path("id") String id);
}
