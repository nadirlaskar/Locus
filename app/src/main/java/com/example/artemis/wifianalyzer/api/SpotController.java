package com.example.artemis.wifianalyzer.api;

import java.io.IOException;
import java.util.List;

import com.example.artemis.wifianalyzer.Fingerprint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpotController implements Callback<List<String>> {

    private static final String BASE_URL = "http://www.mocky.io/v2/";
    private ApiResponse<List<String>> callback;



    public SpotController(ApiResponse<List<String>> callback){
        if(callback != null)
            this.callback = callback;
    }

    public void start() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Spot spotApi = retrofit.create(Spot.class);

        Call<List<String>> call = spotApi.loadSpots();
        call.enqueue(this);
        callback.loading();
    }

    @Override
    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
        if(response.isSuccessful()) {
            List<String> Spots = response.body();
            callback.success(Spots);
        } else {
            callback.failure(response.message());

        }
    }

    @Override
    public void onFailure(Call<List<String>> call, Throwable t) {
        callback.failure(t.getMessage());
    }
}
