package com.example.mydocsapp.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mydocsapp.interfaces.StateViewModel;

public class PassportStateViewModel extends ViewModel  implements StateViewModel {
    private MutableLiveData<Passport> state = new MutableLiveData<>();
    public PassportStateViewModel(){
    }
    public LiveData<Passport> getState(){
        return state;
    }

    @Override
    public void setState(Object _state) {
        state.setValue((Passport)_state);
    }
}
