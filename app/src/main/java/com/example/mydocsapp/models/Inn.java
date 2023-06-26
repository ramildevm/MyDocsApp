package com.example.mydocsapp.models;

import com.example.mydocsapp.interfaces.DatabaseObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Inn implements DatabaseObject {
    public UUID Id;
    public String Number;
    public String FIO;
    public String Gender;
    public String BirthDate;
    public String BirthPlace;
    public String RegistrationDate;
    @SerializedName("PhotoPage164")
    @Expose
    public String PhotoPage1;
    public String UpdateTime;

    public Inn(UUID id, String number, String FIO, String gender, String birthDate, String birthPlace, String registrationDate, String photoPage1, String updateTime) {
        Id = id;
        Number = number;
        this.FIO = FIO;
        Gender = gender;
        BirthDate = birthDate;
        BirthPlace = birthPlace;
        RegistrationDate = registrationDate;
        PhotoPage1 = photoPage1;
        UpdateTime = updateTime;
    }
    @Override
    public void setValuesNull() {
        Number = null;
        this.FIO = null;
        BirthDate = null;
        BirthPlace = null;
        RegistrationDate = null;
        PhotoPage1 = null;
        UpdateTime = null;
    }
}
