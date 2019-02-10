package com.example.artemis.wifianalyzer.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Fingerprint {
    @POST("echo/{spot}")
    Call<ArrayList<com.example.artemis.wifianalyzer.Fingerprint>> postFingerprint(@Path("spot") String spot , @Body ArrayList<com.example.artemis.wifianalyzer.Fingerprint> fingerprint);
}
