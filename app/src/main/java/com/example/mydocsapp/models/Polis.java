package com.example.mydocsapp.models;

import com.example.mydocsapp.interfaces.DatabaseObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Polis implements DatabaseObject {
    public UUID Id;
    public String Number;
    public String FIO;
    public String Gender;
    public String BirthDate;
    @SerializedName("PhotoPage164")
    @Expose
    public String PhotoPage1;
    @SerializedName("PhotoPage264")
    @Expose
    public String PhotoPage2;
    public String ValidUntil;
    public String UpdateTime;

    public Polis(UUID id, String number, String FIO, String gender, String birthDate, String photoPage1, String photoPage2, String validUntil, String updateTime) {
        Id = id;
        Number = number;
        this.FIO = FIO;
        Gender = gender;
        BirthDate = birthDate;
        PhotoPage1 = photoPage1;
        PhotoPage2 = photoPage2;
        ValidUntil = validUntil;
        UpdateTime = updateTime;
    }

    public void setValuesNull() {
        Number = null;
        FIO = null;
        BirthDate = null;
        PhotoPage1 = null;
        PhotoPage2 = null;
        ValidUntil = null;
        UpdateTime = null;
    }
}