package com.example.mydocsapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.example.mydocsapp.apputils.MyEncrypter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imgFile = getIntent().getStringExtra("imageFile");
        imgText = getIntent().getStringExtra("text");
        type = getIntent().getIntExtra("type",0);

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

    public void goBackClick(View view){
        onBackPressed();
    }

    public void saveAsClick(View view) {

    }

    public void changeImageClick(View view) {

    }
}