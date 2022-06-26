package com.example.mydocsapp;
import com.example.mydocsapp.api.User;
import com.example.mydocsapp.api.UserGetCallback;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class MainApi {
    static final String baseUrl = "http://192.168.0.103:5000/";

    public static void GetUser(String login, UserGetCallback userGetCallback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IApi api = retrofit.create(IApi.class);

        api.GetUser(login).enqueue(new retrofit2.Callback<User>() {
            @Override
            public void onResponse(Call<User> Gall, Response<User> response) {

                userGetCallback.onResult(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

                userGetCallback.onError(t);

            }
        });
    }
}
