package com.example.mydocsapp.api;



public class User {
    public int Id;
    public String Login;
    public String Password;
    public String PremiumStatus;
    public String Syncing;
    public String Photo;

    public User(int Id_,String Login_,String Password_,String PremiumStatus_,String Syncing_,  String Photo_)
    {
        this.Id = Id_;
        this.Login = Login_;
        this.Password = Password_;
        this.PremiumStatus = PremiumStatus_;
        this.Syncing = Syncing_;
        this.Photo = Photo_;
    }

}
