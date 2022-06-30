package com.example.mydocsapp.models;
import com.example.mydocsapp.api.User;

import java.util.ArrayList;

public class  SystemContext {
    public static User CurrentUser = null;
    public static ArrayList<Item> CurrentItemsSet = null;
    public static ArrayList<Item> CurrentFolderItemsSet = null;
    public static Item CurrentItem = null;
}
