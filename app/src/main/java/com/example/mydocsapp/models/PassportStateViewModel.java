package com.example.mydocsapp.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PassportStateViewModel extends ViewModel {
    private MutableLiveData<Passport> state = new MutableLiveData<>();
    public PassportStateViewModel(){

    }
    public LiveData<Passport> getState(){
        return state;
    }
    public void setState(Passport _state){
        state.setValue(_state);
    }
}
