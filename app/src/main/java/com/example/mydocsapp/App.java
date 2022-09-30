package com.example.mydocsapp;

import android.app.Application;

import com.example.mydocsapp.api.User;
import com.example.mydocsapp.models.Item;

import java.util.ArrayList;

public class App extends Application {
    public static User CurrentUser = null;
    public static ArrayList<Item> CurrentItemsSet = null;
    public static ArrayList<Item> CurrentFolderItemsSet = null;
    public static Item CurrentItem = null;
    public static boolean isTitleClicked = false;
}
