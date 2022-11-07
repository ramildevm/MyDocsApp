package com.example.mydocsapp.models;

import androidx.annotation.Nullable;

public class Passport {
    public int Id;
    public String SeriaNomer;
    public String DivisionCode;
    public String GiveDate;
    public String ByWhom;
    public String FIO;
    public String BirthDate;
    public String Gender;
    public String BirthPlace;
    public String ResidencePlace;
    @Nullable
    public String FacePhoto;
    @Nullable
    public String PhotoPage1;
    @Nullable
    public String PhotoPage2;

    public Passport(int id, String seriaNomer, String divisionCode, String giveDate, String byWhom, String FIO, String birthDate, String gender, String birthPlace, String residencePlace, String facePhoto, String photoPage1, String photoPage2) {
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
    }
}
