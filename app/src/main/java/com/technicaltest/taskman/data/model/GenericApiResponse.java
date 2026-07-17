package com.technicaltest.taskman.data.model;

import com.google.gson.annotations.SerializedName;

public class GenericApiResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}