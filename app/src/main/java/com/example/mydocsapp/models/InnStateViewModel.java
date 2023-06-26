package com.example.mydocsapp.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mydocsapp.interfaces.StateViewModel;

public class InnStateViewModel extends ViewModel implements StateViewModel {
    private MutableLiveData<Inn> state = new MutableLiveData<>();
    public InnStateViewModel(){
    }
    public LiveData<Inn> getState(){
        return state;
    }

    @Override
    public void setState(Object _state) {
        state.setValue((Inn)_state);
    }
}