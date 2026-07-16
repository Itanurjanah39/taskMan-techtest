package com.technicaltest.taskman.data.repository;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.LoginRequest;
import com.technicaltest.taskman.data.model.LoginResponse;
import com.technicaltest.taskman.data.network.ApiClient;
import com.technicaltest.taskman.data.network.ApiService;
import com.technicaltest.taskman.utils.ApiCallback;
import com.technicaltest.taskman.utils.NetworkHelper;

import retrofit2.Call;

public class AuthRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;

    public AuthRepository(SessionManager sessionManager) {
        this.apiService = ApiClient.getApiService();
        this.sessionManager = sessionManager;
    }

    public void login(String email, String password, ApiCallback<LoginResponse> callback) {
        LoginRequest request = new LoginRequest(email, password);
        Call<LoginResponse> call = apiService.login(request);

        NetworkHelper.enqueueCall(call, callback, new NetworkHelper.ResponseValidator<LoginResponse>() {
            @Override
            public boolean isValid(LoginResponse body) {
                boolean valid = body.isSuccess() && body.getData() != null;
                if (valid) {
                    sessionManager.saveSession(
                            body.getData().getToken(),
                            body.getData().getRole(),
                            email
                    );
                }
                return valid;
            }

            @Override
            public String getErrorMessage(LoginResponse body) {
                return body.getMessage() != null ? body.getMessage() : "Login failed";
            }
        });
    }
}
