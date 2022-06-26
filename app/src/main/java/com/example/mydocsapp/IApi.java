package com.example.mydocsapp;

import com.example.mydocsapp.api.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IApi {
@GET("api/users")
    Call<User> GetUser(@Query("login") String login);
}
