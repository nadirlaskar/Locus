package com.example.artemis.wifianalyzer.api;

import com.example.artemis.wifianalyzer.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrackMeController implements Callback<ServerResponse<String>> {

    private static final String BASE_URL = "https://svn.slk-soft.com/v1/";
    private ApiResponse<String> callback;



    public TrackMeController(ApiResponse<String> callback){
        if(callback != null)
            this.callback = callback;
    }

    public void start(String macId) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(Util.getUnSafeClient())
                .build();

        TrackMe fingerprintApi = retrofit.create(TrackMe.class);

        Call<ServerResponse<String>> call = fingerprintApi.loadDest(macId);
        call.enqueue(this);
        callback.loading();
    }

    @Override
    public void onResponse(Call<ServerResponse<String>> call, Response<ServerResponse<String>> response) {
        if(response.isSuccessful()) {
            String res = response.body().getData();
            callback.success(res);
        } else {
            callback.failure(response.message());

        }
    }

    @Override
    public void onFailure(Call<ServerResponse<String>> call, Throwable t) {
        callback.failure(t.getMessage());
    }
}
