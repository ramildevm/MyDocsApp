package com.example.mydocsapp.models;


public class User {
    public int Id;
    public String Email;
    public String Login;
    public String Password;
    public String Photo;
    public String AccessCode;
    public String UpdateTime;

    public User(int Id_,String Email_,String Login_,String Password_,  String Photo_,  String Pincode_, String UpdateTime_)
    {
        this.Id = Id_;
        this.Email = Email_;
        this.Login = Login_;
        this.Password = Password_;
        this.Photo = Photo_;
        this.AccessCode = Pincode_;
        this.UpdateTime = UpdateTime_;
    }
}
