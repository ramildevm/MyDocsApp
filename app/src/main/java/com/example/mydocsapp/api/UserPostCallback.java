package com.example.mydocsapp.api;

public interface UserPostCallback {
    void onResult(User result);
    void onError(Throwable e);
}
