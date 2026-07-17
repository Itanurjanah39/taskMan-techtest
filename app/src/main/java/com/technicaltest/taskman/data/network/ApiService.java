package com.technicaltest.taskman.data.network;

import com.technicaltest.taskman.data.model.LoginRequest;
import com.technicaltest.taskman.data.model.LoginResponse;
import com.technicaltest.taskman.data.model.ProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers({
        "Content-Type: application/json",
        "Accept: application/json",
        "No-Authentication: true"
    })
    @POST("api/auth/sign-in")
    Call<LoginResponse> login(@Body LoginRequest request);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("api/auth/account")
    Call<ProfileResponse> getProfile();

}
