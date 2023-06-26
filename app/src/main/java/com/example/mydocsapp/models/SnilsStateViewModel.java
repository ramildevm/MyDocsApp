package com.example.mydocsapp.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mydocsapp.interfaces.StateViewModel;

public class SnilsStateViewModel extends ViewModel implements StateViewModel {
    private MutableLiveData<Snils> state = new MutableLiveData<>();
    public SnilsStateViewModel(){
    }
    public LiveData<Snils> getState(){
        return state;
    }

    @Override
    public void setState(Object _state) {
        state.setValue((Snils)_state);
    }
}
