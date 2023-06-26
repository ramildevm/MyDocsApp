package com.example.mydocsapp.models;

import androidx.annotation.Nullable;

import com.example.mydocsapp.interfaces.DatabaseObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Passport implements DatabaseObject {
    public UUID Id;
    @SerializedName("SerialNumber")
    @Expose
    public String SeriaNomer;
    public String DivisionCode;
    public String GiveDate;
    public String ByWhom;
    public String FIO;
    public String BirthDate;
    public String Gender;
    public String BirthPlace;
    public String ResidencePlace;
    @SerializedName("FacePhoto64")
    @Expose
    @Nullable
    public String FacePhoto;
    @SerializedName("PhotoPage164")
    @Expose
    @Nullable
    public String PhotoPage1;
    @SerializedName("PhotoPage264")
    @Expose
    @Nullable
    public String PhotoPage2;
    public String UpdateTime;

    public Passport(UUID id, String seriaNomer, String divisionCode, String giveDate, String byWhom, String FIO, String birthDate, String gender, String birthPlace, String residencePlace, @Nullable String facePhoto, @Nullable String photoPage1, @Nullable String photoPage2, String updateTime) {
        Id = id;
        SeriaNomer = seriaNomer;
        DivisionCode = divisionCode;
        GiveDate = giveDate;
        ByWhom = byWhom;
        this.FIO = FIO;
        BirthDate = birthDate;
        Gender = gender;
        BirthPlace = birthPlace;
        ResidencePlace = residencePlace;
        FacePhoto = facePhoto;
        PhotoPage1 = photoPage1;
        PhotoPage2 = photoPage2;
        UpdateTime = updateTime;
    }

    public void setValuesNull() {
        SeriaNomer = null;
        DivisionCode = null;
        GiveDate = null;
        ByWhom = null;
        this.FIO = null;
        BirthDate = null;
        Gender = null;
        BirthPlace = null;
        ResidencePlace = null;
        FacePhoto = null;
        PhotoPage1 = null;
        PhotoPage2 = null;
        UpdateTime = null;
    }
}
