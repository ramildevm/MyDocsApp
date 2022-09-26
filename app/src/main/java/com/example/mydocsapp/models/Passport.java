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
    public byte[] FacePhoto;
    @Nullable
    public byte[] PhotoPage1;
    @Nullable
    public byte[] PhotoPage2;

    public Passport(int id, String seriaNomer, String divisionCode, String giveDate, String byWhom, String FIO, String birthDate, String gender, String birthPlace, String residencePlace, byte[] facePhoto, byte[] photoPage1, byte[] photoPage2) {
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
