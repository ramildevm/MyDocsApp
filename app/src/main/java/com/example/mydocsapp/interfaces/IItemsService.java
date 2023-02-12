package com.example.mydocsapp.interfaces;

import com.example.mydocsapp.models.Item;

import java.util.ArrayList;

public interface IItemsService {
    ArrayList<Item> getCurrentItemsSet();
    void setCurrentItemsSet(ArrayList<Item> items);
}
