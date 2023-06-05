package com.example.mydocsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;

import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.services.AppService;
import com.github.chrisbanes.photoview.PhotoView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageActivity extends AppCompatActivity {
    private String imgFile;
    private String imgText;
    private int type;
    private ActivityResultLauncher<Intent> registerForARImage;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputFile.delete();
        if(type == 0) {
            filePath.delete();
            findViewById(R.id.right_menu_change_btn).setVisibility(View.GONE);
        }
        registerForARImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                CropImage.ActivityResult cropImageResult = CropImage.getActivityResult(result.getData());
                if (result.getData() != null) {
                    final Uri uri = cropImageResult.getUri();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(stream.toByteArray()));
                        InputStream is = new ByteArrayInputStream(stream.toByteArray());
                        try {
                            MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
                            ((PhotoView)findViewById(R.id.image_holder)).setImageBitmap(decoded);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
        findViewById(R.id.right_menu_save_as_btn).setOnClickListener(v->saveAsClick());
        findViewById(R.id.right_menu_change_btn).setOnClickListener(v->changeImageClick());
    }
    private void getExtraData(Intent intent) {
        imgFile = intent.getStringExtra("imageFile");
        imgText = intent.getStringExtra("text");
        type = intent.getIntExtra("type",0);
    }

    public void goBackClick(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(0, 0);
        super.onBackPressed();
    }
    public void saveAsClick() {
        String picturesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File filePath = new File(imgFile);
        String fileName = filePath.getName();
        File outputFile = new File(picturesPath, fileName);
        try {
            MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(filePath), new FileOutputStream(outputFile));
            Toast.makeText(this,getString(R.string.image_saved),Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeImageClick() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = CropImage.activity()
                    .getIntent(this);
            registerForARImage.launch(intent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }
}