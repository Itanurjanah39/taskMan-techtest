package com.technicaltest.taskman.data.repository;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.LoginRequest;
import com.technicaltest.taskman.data.model.LoginResponse;
import com.technicaltest.taskman.data.network.ApiClient;
import com.technicaltest.taskman.data.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;

    public AuthRepository(SessionManager sessionManager) {
        this.apiService = ApiClient.getApiService();
        this.sessionManager = sessionManager;
    }

    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String errorMessage);
    }

    public void login(String email, String password, LoginCallback callback) {
        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess() && loginResponse.getData() != null) {
                        sessionManager.saveSession(
                                loginResponse.getData().getToken(),
                                loginResponse.getData().getRole(),
                                email
                        );
                        callback.onSuccess(loginResponse);
                    } else {
                        callback.onError(loginResponse.getMessage() != null ? loginResponse.getMessage() : "Login failed");
                    }
                } else {
                    callback.onError("Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Connection error");
            }
        });
    }
}
