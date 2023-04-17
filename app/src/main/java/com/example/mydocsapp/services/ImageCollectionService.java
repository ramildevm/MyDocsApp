package com.example.mydocsapp.services;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

public class ImageCollectionService {
    private ArrayList<Bitmap> bitmapList = new ArrayList<>();
    private int currentImage = -1;

    public void add(Bitmap bitmap){
        bitmapList.add(bitmap);
        Log.e("dededed","service");
    }
    public Bitmap get(int id){
        return bitmapList.get(id);
    }
    public ArrayList<Bitmap> get(){
        return bitmapList;
    }
    public void set(int id, Bitmap bitmap){
        bitmapList.set(id,bitmap);
    }
    public int getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(int currentImage) {
        this.currentImage = currentImage;
    }

    public int getSize() {
        return bitmapList.size();
    }

    public void remove(int index) {
        this.bitmapList.remove(index);
    }
}
