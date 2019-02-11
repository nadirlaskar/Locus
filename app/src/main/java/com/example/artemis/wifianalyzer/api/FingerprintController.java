package com.example.artemis.wifianalyzer.api;

import com.example.artemis.wifianalyzer.FingerprintListModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FingerprintController implements Callback<Object> {

    private static final String BASE_URL = "http://192.168.137.1:6789/v1/";
    private ApiResponse<Object> callback;



    public FingerprintController(ApiResponse<Object> callback){
        if(callback != null)
            this.callback = callback;
    }

    public void start( com.example.artemis.wifianalyzer.model.Spot spot, com.example.artemis.wifianalyzer.model.Fingerprint fingerprintForSpot) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Fingerprint fingerprintApi = retrofit.create(Fingerprint.class);

        Call<Object> call = fingerprintApi.postFingerprint(fingerprintForSpot);
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
