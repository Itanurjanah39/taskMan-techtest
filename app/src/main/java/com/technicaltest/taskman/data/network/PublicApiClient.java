package com.technicaltest.taskman.data.network;

import com.technicaltest.taskman.data.auth.SessionManager;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PublicApiClient {

    private static final String BASE_URL =
            "https://6a58a9b368601fc330e913e0.mockapi.io/";

    private static Retrofit retrofit;

    public static PublicApiService getService(SessionManager sessionManager){

        if(retrofit == null){

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(NetworkModule.provideClient(sessionManager))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }

        return retrofit.create(PublicApiService.class);
    }

}
