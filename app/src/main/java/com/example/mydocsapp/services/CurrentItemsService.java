package com.example.mydocsapp.services;

import android.database.Cursor;

import com.example.mydocsapp.models.Item;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class CurrentItemsService {
    private ArrayList<Item> CurrentItemsSet;
    private Item CurrentItem;
    public boolean isFoldersAvailable;
    public boolean isDocsAvailable;
    public boolean isCardsAvailable;
    public boolean isImagesAvailable;
    DBHelper db;
    private boolean isHidenItems;

    public CurrentItemsService(DBHelper _db, boolean _isHidenItems) {
        isHidenItems = _isHidenItems;
        isCardsAvailable = true;
        isImagesAvailable = true;
        isDocsAvailable = true;
        isFoldersAvailable = true;
        db = _db;
        CurrentItemsSet = new ArrayList<>();
    }

    public void setSelectedPropertyZero() {
        for (Item x : CurrentItemsSet) {
            int id = CurrentItemsSet.indexOf(x);
            x.isSelected = 0;
            CurrentItemsSet.set(id, x);
        }
    }

    public void setInitialData() {
        CurrentItemsSet.clear();
        CurrentItemsSet.addAll(isHidenItems ? db.getHiddenItemsByFolder0() : db.getItemsByFolder0());
        CurrentItem = null;
    }

    public ArrayList<Item> getFolderItemsFromDb(UUID id) {
        return (ArrayList<Item>) db.getItemsByFolder(id, AppService.isHideMode() ? 1 : 0);
    }

    public ArrayList<Item> getCurrentItemsSet() {
        return CurrentItemsSet;
    }

    public ArrayList<Item> getSortedCurrentItemsSet() {
        setInitialData();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            if (!isFoldersAvailable)
                CurrentItemsSet = new ArrayList<>(CurrentItemsSet.stream().filter((x) -> !(x.Type.equals("Folder"))).collect(Collectors.toList()));
            if (!isDocsAvailable)
                CurrentItemsSet = new ArrayList<>(CurrentItemsSet.stream().filter((x) -> !(x.Type.equals("Passport") |x.Type.equals("Polis") | x.Type.equals("SNILS")| x.Type.equals("INN")| x.Type.equals("Template"))).collect(Collectors.toList()));
            if (!isImagesAvailable)
                CurrentItemsSet = new ArrayList<>(CurrentItemsSet.stream().filter((x) -> !(x.Type.equals("Collection"))).collect(Collectors.toList()));
            if (!isCardsAvailable)
                CurrentItemsSet = new ArrayList<>(CurrentItemsSet.stream().filter((x) -> !(x.Type.equals("CreditCard"))).collect(Collectors.toList()));
        }
        return CurrentItemsSet;
    }

    public void setCurrentItemsSet(ArrayList<Item> currentItemsSet) {
        CurrentItemsSet = currentItemsSet;
    }

    public Item getCurrentItem() {
        return CurrentItem;
    }

    public void setCurrentItem(Item currentItem) {
        CurrentItem = currentItem;
    }

    public void setItem(int position, Item item) {
        CurrentItemsSet.set(position, item);
    }
}
