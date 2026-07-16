package com.technicaltest.taskman.utils;

public interface ApiCallback<T> {
    void onResponse(Resource<T> resource);
}
