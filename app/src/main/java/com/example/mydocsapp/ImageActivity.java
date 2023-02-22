package com.example.mydocsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.NavUtils;

import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.services.AppService;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

public class ImageActivity extends AppCompatActivity {

    private String imgFile;
    private String imgText;
    private int type;
    private Item CurrentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        getExtraData(getIntent());

        File outputFile = new File(imgFile+"_copy");
        File filePath = new File(imgFile);

        try {
            MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(filePath), new FileOutputStream(outputFile));
            ((PhotoView)findViewById(R.id.image_holder)).setImageURI(Uri.fromFile(outputFile));
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputFile.delete();
        if(type == 0) {
            filePath.delete();
        }
        ((TextView)findViewById(R.id.image_txt)).setText(imgText);
        (findViewById(R.id.menubar_options)).setOnClickListener(v -> {
            if (v.getTag().equals("off")) {
                MotionLayout ml = findViewById(R.id.motion_layout);
                ml.setTransition(R.id.transOptionMenu);
                ml.transitionToEnd();
                v.setTag("on");
            } else if (v.getTag().equals("on")) {
                MotionLayout ml = findViewById(R.id.motion_layout);
                ml.setTransition(R.id.transOptionMenu);
                ml.transitionToStart();
                v.setTag("off");
            }
        });
    }
    private void getExtraData(Intent intent) {
        imgFile = intent.getStringExtra("imageFile");
        imgText = intent.getStringExtra("text");
        type = intent.getIntExtra("type",0);
        CurrentItem= intent.getParcelableExtra("item");
    }

    public void goBackClick(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if(CurrentItem.Type.equals("Изображение"))
            NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }
    public void saveAsClick(View view) {

    }

    public void changeImageClick(View view) {

    }
}