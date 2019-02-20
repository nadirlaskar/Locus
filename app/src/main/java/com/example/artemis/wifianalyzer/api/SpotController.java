package com.example.artemis.wifianalyzer.api;

import java.util.List;

import com.example.artemis.wifianalyzer.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpotController implements Callback<ServerResponse<List<com.example.artemis.wifianalyzer.model.Spot>>> {

    private static final String BASE_URL = "https://svn.slk-soft.com/v1/";
    private ApiResponse<List<com.example.artemis.wifianalyzer.model.Spot>> callback;



    public SpotController(ApiResponse<List<com.example.artemis.wifianalyzer.model.Spot>> callback){
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
                .client(Util.getUnSafeClient())
                .build();

        Spot spotApi = retrofit.create(Spot.class);

        Call<ServerResponse<List<com.example.artemis.wifianalyzer.model.Spot>>> call = spotApi.loadSpots();
        call.enqueue(this);
        callback.loading();
    }

    @Override
    public void onResponse(Call<ServerResponse<List<com.example.artemis.wifianalyzer.model.Spot>>> call, Response<ServerResponse<List<com.example.artemis.wifianalyzer.model.Spot>>> response) {
        if(response.isSuccessful()) {
            List<com.example.artemis.wifianalyzer.model.Spot> Spots = response.body().getData();
            callback.success(Spots);
        } else {
            callback.failure(response.message());

        }
    }

    @Override
    public void onFailure(Call<ServerResponse<List<com.example.artemis.wifianalyzer.model.Spot>>> call, Throwable t) {
        callback.failure(t.getMessage());
    }
}
