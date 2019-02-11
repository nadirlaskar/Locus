package com.example.artemis.wifianalyzer.api;

import com.example.artemis.wifianalyzer.FingerprintListModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Fingerprint {
    @POST("fingerprint")
    Call<Object> postFingerprint(@Body com.example.artemis.wifianalyzer.model.Fingerprint fingerprintListModel);
}
