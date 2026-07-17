package com.technicaltest.taskman.data.network;

import com.technicaltest.taskman.data.auth.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetworkModule {

    public static OkHttpClient provideClient(SessionManager sessionManager) {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {

                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();

                    if (original.header("No-Authentication") != null) {
                        builder.removeHeader("No-Authentication");
                    } else {
                        String token = sessionManager.getToken();

                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                    }

                    return chain.proceed(builder.build());

                })
                .build();
    }

}
