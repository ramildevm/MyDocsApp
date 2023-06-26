package com.example.mydocsapp.services;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    public static String dateToIso(String dateString) {
        if (dateString == null)
            return null;
        SimpleDateFormat inputFormatter = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = inputFormatter.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return outputFormatter.format(date);
    }
    public static boolean isUpdateTimeNewerThanUser(String _date, String _userDate){
        if(_date==null)
            return true;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(_date);
            Date userDate = sdf.parse(_userDate);
            if (date.compareTo(userDate) > 0) {
                return true;
            } else if (_date.compareTo(_userDate) < 0) {
                return false;
            } else {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String reverseDateToIso(String dateString) {
        if (dateString == null)
            return null;
        SimpleDateFormat outputFormatter = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = inputFormatter.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return outputFormatter.format(date);
    }
}
