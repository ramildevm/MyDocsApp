package com.example.mydocsapp.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppService {
    private static String my_key = "r6Nuf3tD9MF0oCcA";
    private static String my_spec_key = "yWP30I1zO92r83Kt";
    public static String getMy_key() {
        return my_key;
    }
    public static String getMy_spec_key() {
        return my_spec_key;
    }
    private static boolean hideMode;

    public static int getUserId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("Id",0);
    }

    public static void setUserId(int userId, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("Id").commit();
        editor.putInt("Id",userId);
        editor.apply();
    }

    public static boolean isHideMode() {
        return hideMode;
    }

    public static void setHideMode(boolean hideMode) {
        AppService.hideMode = hideMode;
    }
}
