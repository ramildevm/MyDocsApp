package com.example.mydocsapp.interfaces;

import android.graphics.Bitmap;
import android.view.View;

public interface IFragmentDataSaver {
    void SaveData();
    void SavePhotos(int ItemId);
    void copyTextClick(View view);
}