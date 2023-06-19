package com.example.mydocsapp.models;


import java.time.LocalDateTime;

public class User {
    public int Id;
    public String Email;
    public String Login;
    public String Password;
    public String Photo;
    public String PinCode;
    public String UpdateTime;

    public User(int Id_,String Email_,String Login_,String Password_,  String Photo_,  String Pincode_, String UpdateTime_)
    {
        this.Id = Id_;
        this.Email = Email_;
        this.Login = Login_;
        this.Password = Password_;
        this.Photo = Photo_;
        this.PinCode = Pincode_;
        this.UpdateTime = UpdateTime_;
    }
}
