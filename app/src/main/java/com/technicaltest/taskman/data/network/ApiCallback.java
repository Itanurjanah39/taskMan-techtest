package com.technicaltest.taskman.data.network;

public interface ApiCallback<T> {
    void onResponse(Resource<T> resource);
}
