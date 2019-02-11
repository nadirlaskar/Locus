package com.example.artemis.wifianalyzer.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Spot {
    @GET("spots/")
    Call<ServerResponse<List<com.example.artemis.wifianalyzer.model.Spot>>> loadSpots();
}
