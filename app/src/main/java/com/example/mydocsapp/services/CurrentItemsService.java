package com.example.mydocsapp.services;

import android.database.Cursor;
import android.util.Log;

import com.example.mydocsapp.models.Item;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CurrentItemsService  {
    private ArrayList<Item> CurrentItemsSet;
    private Item CurrentItem;
    public boolean isFoldersAvailable;
    public boolean isDocsAvailable;
    public boolean isCardsAvailable;
    public boolean isImagesAvailable;
    DBHelper db;

    public CurrentItemsService(DBHelper _db){
        isCardsAvailable = true;
        isImagesAvailable = true;
        isDocsAvailable = true;
        isFoldersAvailable = true;
        db = _db;
        CurrentItemsSet = new ArrayList<Item>();
    }

    public void setSelectedPropertyZero() {
        for (Item x : CurrentItemsSet) {
            int id = CurrentItemsSet.indexOf(x);
            x.isSelected = 0;
            CurrentItemsSet.set(id, x);
            db.updateItem(x.Id, x);
        }
    }
    public void setInitialData() {
        CurrentItemsSet.clear();
        Cursor cur = db.getItemsByFolder0();
        Item item;
        while (cur.moveToNext()) {
            item = new Item(cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4),
                    cur.getInt(5),
                    cur.getInt(6),
                    cur.getString(7),
                    cur.getInt(8),
                    cur.getInt(8));
            CurrentItemsSet.add(item);
        }
        CurrentItem = null;
    }
    public ArrayList<Item> getCurrentItemsSet() {
        return CurrentItemsSet;
    }
    public ArrayList<Item> getSortedCurrentItemsSet() {
        setInitialData();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            if (!isFoldersAvailable)
                CurrentItemsSet = new ArrayList<>(CurrentItemsSet.stream().filter((x) -> !(x.Type.equals("Папка"))).collect(Collectors.toList()));
            if (!isDocsAvailable)
                CurrentItemsSet = new ArrayList<>(CurrentItemsSet.stream().filter((x) -> !(x.Type.equals("Паспорт"))).collect(Collectors.toList()));
            if (!isImagesAvailable)
                CurrentItemsSet = new ArrayList<>(CurrentItemsSet.stream().filter((x) -> !(x.Type.equals("Изображение"))).collect(Collectors.toList()));
            if (!isCardsAvailable)
                CurrentItemsSet = new ArrayList<>(CurrentItemsSet.stream().filter((x) -> !(x.Type.equals("Карта"))).collect(Collectors.toList()));
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
        CurrentItemsSet.set(position,item);
    }
}
