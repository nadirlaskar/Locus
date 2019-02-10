package com.example.artemis.wifianalyzer.api;

public interface ApiResponse<T> {
    void loading();
    void success(T response);
    void failure(String error);
}
