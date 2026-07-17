package com.technicaltest.taskman.data.repository;

import com.technicaltest.taskman.data.auth.SessionManager;
import com.technicaltest.taskman.data.model.GenericApiResponse;
import com.technicaltest.taskman.data.model.LoginRequest;
import com.technicaltest.taskman.data.model.LoginResponse;
import com.technicaltest.taskman.data.model.ProfileResponse;
import com.technicaltest.taskman.data.network.ApiClient;
import com.technicaltest.taskman.data.network.ApiService;
import com.technicaltest.taskman.utils.ApiCallback;
import com.technicaltest.taskman.utils.NetworkHelper;

import retrofit2.Call;

public class AuthRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;

    public AuthRepository(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.apiService = ApiClient.getService(sessionManager);
    }


    public void login(String email, String password, ApiCallback<LoginResponse> callback) {

        LoginRequest request = new LoginRequest(email, password);

        NetworkHelper.enqueueCall(
                apiService.login(request),
                callback,
                new NetworkHelper.ResponseValidator<LoginResponse>() {

                    @Override
                    public boolean isValid(LoginResponse body) {

                        boolean valid =
                                body != null &&
                                        body.isSuccess() &&
                                        body.getData() != null;

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
                        return body != null && body.getMessage() != null
                                ? body.getMessage()
                                : "Login failed";
                    }
                }
        );
    }

    public void getProfile(ApiCallback<ProfileResponse> callback) {
        NetworkHelper.enqueueCall(apiService.getProfile(), callback);
    }

    public void logout(ApiCallback<GenericApiResponse> callback) {
        NetworkHelper.enqueueCall(apiService.logout(), callback);
    }
}