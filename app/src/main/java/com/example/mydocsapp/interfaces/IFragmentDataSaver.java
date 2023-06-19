package com.example.mydocsapp.interfaces;

import android.graphics.Bitmap;
import android.view.View;

import java.util.List;

public interface IFragmentDataSaver {
    void SaveData();
    List<Bitmap> getPhotos();
    void copyTextClick(View view);
    boolean IsValidData();
}
