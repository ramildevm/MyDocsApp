package com.example.mydocsapp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class CreditCard
{
    public UUID Id;
    public String Number;
    public String FIO;
    public String ExpiryDate;
    public int CVV;
    @SerializedName("PhotoPage164")
    @Expose
    public String PhotoPage1;
    public String UpdateTime;

    public CreditCard(UUID id, String number, String FIO, String expiryDate, int CVV, String photoPage1, String updateTime) {
        Id = id;
        Number = number;
        this.FIO = FIO;
        ExpiryDate = expiryDate;
        this.CVV = CVV;
        PhotoPage1 = photoPage1;
        UpdateTime = updateTime;
    }

    public void setValuesNull() {
        Number = null;
        this.FIO = null;
        ExpiryDate = null;
        this.CVV = 0;
        PhotoPage1 = null;
        UpdateTime = null;
    }
}
