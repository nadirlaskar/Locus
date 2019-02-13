package com.example.artemis.wifianalyzer.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrackController implements Callback<Object> {

    private static final String BASE_URL = "https://svn.slk-soft.com:80/locus/v1/";
    private ApiResponse<Object> callback;



    public TrackController(ApiResponse<Object> callback){
        if(callback != null)
            this.callback = callback;
    }

    public void start(com.example.artemis.wifianalyzer.model.Fingerprint fingerprintForSpot) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Track fingerprintApi = retrofit.create(Track.class);

        Call<Object> call = fingerprintApi.postTrackFingerprint(fingerprintForSpot);
        call.enqueue(this);
        callback.loading();
    }

    @Override
    public void onResponse(Call<Object> call, Response<Object> response) {
        if(response.isSuccessful()) {
            Object res = response.body();
            callback.success(res);
        } else {
            callback.failure(response.message());

        }
    }

    @Override
    public void onFailure(Call<Object> call, Throwable t) {
        callback.failure(t.getMessage());
    }
}
