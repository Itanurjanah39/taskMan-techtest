package com.technicaltest.taskman.data.network;

import com.technicaltest.taskman.data.model.GenericApiResponse;
import com.technicaltest.taskman.data.model.TaskDetailResponse;
import com.technicaltest.taskman.data.model.TaskRequest;
import com.technicaltest.taskman.data.model.TaskResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PublicApiService {

    @Headers("No-Authentication: true")
    @GET("api/tasks")
    Call<List<TaskResponse>> getTasks();

    @Headers("No-Authentication: true")
    @GET("api/tasks/{id}")
    Call<TaskDetailResponse> getTaskDetail(@Path("id") String id);

    @Headers("No-Authentication: true")
    @POST("api/tasks")
    Call<TaskResponse> createTask(@Body TaskRequest request);

    @Headers("No-Authentication: true")
    @PUT("api/tasks/{id}")
    Call<TaskResponse> updateTask(@Path("id") String id, @Body TaskRequest request);

    @Headers("No-Authentication: true")
    @DELETE("api/tasks/{id}")
    Call<GenericApiResponse> deleteTask(@Path("id") String id);
}
