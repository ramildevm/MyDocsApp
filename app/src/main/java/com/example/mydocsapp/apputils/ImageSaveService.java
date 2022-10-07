package com.example.mydocsapp.apputils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;

public class ImageSaveService {
    public static byte[] bitmapToByteArray(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
