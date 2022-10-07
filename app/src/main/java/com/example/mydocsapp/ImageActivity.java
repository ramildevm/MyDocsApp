package com.example.mydocsapp;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mydocsapp.databinding.ActivityImageBinding;

import java.io.File;

public class ImageActivity extends AppCompatActivity {

    private String imgFile;
    private String imgText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imgFile = getIntent().getStringExtra("imageFile");
        imgText = getIntent().getStringExtra("text");
        Log.e("Errr",imgText);
        File filePath = getFileStreamPath(imgFile);
        Drawable d = Drawable.createFromPath(filePath.toString());


        setContentView(R.layout.activity_image);


        ((ImageView)findViewById(R.id.image_holder)).setBackground(d);
        ((TextView)findViewById(R.id.image_txt)).setText(imgText);
    }
    public void goBackClick(View view){
        onBackPressed();
    }
}