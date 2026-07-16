package com.technicaltest.taskman.utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkHelper {

    public interface ResponseValidator<T> {
        boolean isValid(T body);
        String getErrorMessage(T body);
    }

    public static <T> void enqueueCall(Call<T> call, ApiCallback<T> callback) {
        enqueueCall(call, callback, null);
    }

    public static <T> void enqueueCall(Call<T> call, ApiCallback<T> callback, ResponseValidator<T> validator) {
        callback.onResponse(Resource.loading(null));
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    T body = response.body();
                    if (validator != null) {
                        if (validator.isValid(body)) {
                            callback.onResponse(Resource.success(body));
                        } else {
                            callback.onResponse(Resource.error(validator.getErrorMessage(body), null));
                        }
                    } else {
                        callback.onResponse(Resource.success(body));
                    }
                } else {
                    callback.onResponse(Resource.error("Error: " + response.code() + " " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                String error = t.getMessage() != null ? t.getMessage() : "Connection error";
                callback.onResponse(Resource.error(error, null));
            }
        });
    }
}
