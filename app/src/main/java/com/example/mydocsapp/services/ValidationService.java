package com.example.mydocsapp.services;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ValidationService {

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
    public static boolean isValidDateFormat(String dateString, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
