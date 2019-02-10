package com.example.artemis.wifianalyzer.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FingerprintController implements Callback<ArrayList<com.example.artemis.wifianalyzer.Fingerprint>> {

    private static final String BASE_URL = "http://mockbin.org/";
    private ApiResponse<ArrayList<com.example.artemis.wifianalyzer.Fingerprint>> callback;



    public FingerprintController(ApiResponse<ArrayList<com.example.artemis.wifianalyzer.Fingerprint>> callback){
        if(callback != null)
            this.callback = callback;
    }

    public void start(String spot, ArrayList<com.example.artemis.wifianalyzer.Fingerprint> fingerprint) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Fingerprint fingerprintApi = retrofit.create(Fingerprint.class);

        Call<ArrayList<com.example.artemis.wifianalyzer.Fingerprint>> call = fingerprintApi.postFingerprint(spot,fingerprint);
        call.enqueue(this);
        callback.loading();
    }

    @Override
    public void onResponse(Call<ArrayList<com.example.artemis.wifianalyzer.Fingerprint>> call, Response<ArrayList<com.example.artemis.wifianalyzer.Fingerprint>> response) {
        if(response.isSuccessful()) {
            ArrayList<com.example.artemis.wifianalyzer.Fingerprint> Spots = response.body();
            callback.success(Spots);
        } else {
            callback.failure(response.message());

        }
    }

    @Override
    public void onFailure(Call<ArrayList<com.example.artemis.wifianalyzer.Fingerprint>> call, Throwable t) {
        callback.failure(t.getMessage());
    }
}
