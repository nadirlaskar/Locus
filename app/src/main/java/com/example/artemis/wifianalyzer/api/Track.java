package com.example.artemis.wifianalyzer.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Track {
    @POST("track")
    Call<Object> postTrackFingerprint(@Body com.example.artemis.wifianalyzer.model.Fingerprint fingerprintListModel);
}
