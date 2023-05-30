package com.example.mydocsapp.models;
public class Polis {
    public int Id;
    public String Number;
    public String FIO;
    public char Gender;
    public String BirthDate;
    public String PhotoPage1;
    public String PhotoPage2;

    public Polis(int id, String number, String fio, char gender, String birthDate, String photoPage1, String photoPage2) {
        Id = id;
        Number = number;
        FIO = fio;
        Gender = gender;
        BirthDate = birthDate;
        PhotoPage1 = photoPage1;
        PhotoPage2 = photoPage2;
    }
}