package com.example.mydocsapp.interfaces;

import androidx.lifecycle.LiveData;

public interface StateViewModel<T> {
    LiveData<T> getState();
    void setState(T _state);
}
