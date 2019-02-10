package com.example.artemis.wifianalyzer.api;

import com.example.artemis.wifianalyzer.Fingerprint;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Spot {
    @GET("5c600f7a310000e707f1afb3")
    Call<List<String>> loadSpots();
}
