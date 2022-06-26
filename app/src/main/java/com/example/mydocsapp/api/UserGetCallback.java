package com.example.mydocsapp.api;

public interface UserGetCallback {
void onResult(User user);
void onError(Throwable e);
}
