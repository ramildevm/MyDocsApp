package com.example.mydocsapp.models;
public class CreditCard
{
    public int Id;
    public String Number;
    public String FIO;
    public String ExpiryDate;
    public int CVV;
    public String PhotoPage1;

    public CreditCard(int Id_,String Number_,String FIO_,String ExpiryDate_,int CVV_,String PhotoPage1_)
    {
        this.Id = Id_;
        this.Number = Number_;
        this.FIO = FIO_;
        this.ExpiryDate = ExpiryDate_;
        this.CVV = CVV_;
        this.PhotoPage1 = PhotoPage1_;
    }
}
