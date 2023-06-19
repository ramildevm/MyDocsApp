package com.example.mydocsapp.api;

public interface ResponseCallback {
    void onSuccess(String encryptedData);
    void onConflict();
    void onError(String errorMessage);
}
