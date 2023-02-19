package com.example.mydocsapp.services;public class AppService {
    private static int UserId;
    private static String my_key = "r6Nuf3tD9MF0oCcA";
    private static String my_spec_key = "yWP30I1zO92r83Kt";
    public static String getMy_key() {
        return my_key;
    }
    public static String getMy_spec_key() {
        return my_spec_key;
    }

    public static int getUserId() {
        return UserId;
    }

    public static void setUserId(int userId) {
        UserId = userId;
    }
}
