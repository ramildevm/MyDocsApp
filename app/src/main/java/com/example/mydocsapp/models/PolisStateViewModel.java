package com.example.mydocsapp.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mydocsapp.interfaces.StateViewModel;

public class PolisStateViewModel extends ViewModel implements StateViewModel {
    private MutableLiveData<Polis> state = new MutableLiveData<>();
    public PolisStateViewModel(){
    }
    public LiveData<Polis> getState(){
        return state;
    }

    @Override
    public void setState(Object _state) {
        state.setValue((Polis)_state);
    }
}