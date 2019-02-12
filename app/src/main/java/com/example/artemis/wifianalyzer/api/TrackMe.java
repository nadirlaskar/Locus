package com.example.artemis.wifianalyzer.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TrackMe {
    @GET("track/{macId}")
    Call<ServerResponse<String>> loadDest(@Path("macId") String macId);
}
