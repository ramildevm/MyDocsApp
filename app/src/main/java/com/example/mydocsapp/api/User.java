package com.example.mydocsapp.api;
public class User {
    public int id;
    public String login;
    public String password;

    public User(String _login,String _password) {
        login = _login;
        password = _password;
    }
}
