package com.example.mydocsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.NavUtils;
import androidx.core.view.GestureDetectorCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.databinding.ActivityMainMenuBinding;
import com.example.mydocsapp.models.User;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class MainMenuActivity extends AppCompatActivity {
    private GestureDetectorCompat gestureDetector;
    ActivityMainMenuBinding binding;
    private boolean isGuest = false;
    private DBHelper db;
    private ActivityResultLauncher<Intent> registerForARImageChange;
    private User CurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        gestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener());
        setWindowData();
        setOnClickListeners(isGuest);
        registerForARImageChange = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    Bitmap bitmap = ImageService.getBitmapFormUri(this, imageUri);
                    ((Runnable) () -> {
                        Glide.with(this)
                                .load(bitmap)
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.accountPhotoImage);
                    }).run();
                    changePhotoFile(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changePhotoFile(Bitmap bitmap) {
        File rootDir = getApplicationContext().getFilesDir();
        String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + CurrentUser.Id + "/UserPhotoFolder/";
        File dir = new File(imgPath);
        deleteDirectory(dir);
        dir.mkdirs();
        File imgFile = new File(dir, "UserPhoto" + "_file");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        InputStream is = new ByteArrayInputStream(stream.toByteArray());
        try {
            MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CurrentUser.Photo = imgFile.getAbsolutePath();
        db.updateUser(CurrentUser.Id, CurrentUser);

    }

    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setWindowData();
    }

    private void setWindowData() {
        int userId = AppService.getUserId(this);
        db = new DBHelper(this, userId);
        User user = db.getUserById(userId);
        isGuest = user.Id == 0;
        if (isGuest) {
            binding.changeAccountTxt.setText(R.string.exit);
            binding.accountPanel.setVisibility(View.GONE);
            binding.syncingPanel.setAlpha(0.5f);
            binding.templatesPanel.setAlpha(0.5f);
            binding.loginTxt.setText(R.string.guest_mode);
            Glide.with(this)
                    .load(R.drawable.ic_person)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.accountPhotoImage);
            binding.accountPhotoImage.setPadding(50, 50, 50, 50);
        } else {
            CurrentUser = user;
            File outputFile;
            File encFile;
            if (user.Photo != null && user.Photo.length() != 0) {
                outputFile = new File(user.Photo + "_copy");
                encFile = new File(user.Photo);
                try {
                    encFile.setReadable(true);
                    FileInputStream is = new FileInputStream(encFile);
                    FileOutputStream os = new FileOutputStream(outputFile);
                    MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(),is , os);
                    Glide.with(this)
                            .load(Uri.fromFile(outputFile))
                            .apply(RequestOptions.circleCropTransform())
                            .into(binding.accountPhotoImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Glide.with(this)
                        .load(R.drawable.ic_person)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.accountPhotoImage);
            }
            binding.loginTxt.setText(user.Login);
        }
    }

    private void setOnClickListeners(Boolean isGuest) {
        binding.changeAccountTxt.setOnClickListener(v -> changeAccountClickBack(v));
        binding.settingsImage.setOnClickListener(v -> goSettingsClick(v));
        binding.settingsTxt.setOnClickListener(v -> goSettingsClick(v));
        binding.syncingPanel.setOnClickListener(v -> goSyncingClick(v));
        binding.syncingPanel.setOnClickListener(v -> goSyncingClick(v));
        if (isGuest) {
            binding.templatesPanel.setOnClickListener(v -> makeAccountDialog());
            binding.syncingPanel.setOnClickListener(v -> makeAccountDialog());
            binding.accountPanel.setOnClickListener(v -> makeAccountDialog());
            binding.hiddenPanel.setOnClickListener(v -> goHidenFilesClick(v));
        } else {
            binding.templatesPanel.setOnClickListener(v -> goMainTemplateClick(v));
            binding.syncingPanel.setOnClickListener(v -> goSyncingClick(v));
            binding.accountPanel.setOnClickListener(v -> goAccountSettingsClick(v));
            binding.hiddenPanel.setOnClickListener(v -> goHidenFilesClick(v));
            binding.accountPhotoImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                registerForARImageChange.launch(intent);
            });
        }
    }

    private void goAccountSettingsClick(View v) {
        startActivity(new Intent(this, AccountSettingsActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }

    private void goSyncingClick(View v) {
        startActivity(new Intent(this, SyncActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }

    private void makeAccountDialog() {
        Dialog dialog = new Dialog(MainMenuActivity.this);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_account_create_layout);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(true);

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setLayout(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goMainMenuClickBack();
    }

    public void goMainMenuClickBack() {
        AppService.setHideMode(false);
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_left);
    }

    public void changeAccountClickBack(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("email").commit();
        editor.remove("Id").commit();
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_left);
    }

    public void goSettingsClick(View view) {
        startActivity(new Intent(MainMenuActivity.this, SettingsActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_left);
    }

    public void goHidenFilesClick(View view) {
        User user = db.getUserById(AppService.getUserId(this));
        String pinCode = user.AccessCode;
        if(pinCode == null) {
            Toast.makeText(this, R.string.set_pincode_first,Toast.LENGTH_SHORT).show();
        }
        else{
            makePinCodeDialog(pinCode);
        }
    }

    private void makePinCodeDialog(String pinCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.pin_code_dialog_layout,null);
        AppCompatTextView pinCodeTxt = layout.findViewById(R.id.pinTextView);
        TextInputLayout textInputLayout = layout.findViewById(R.id.textInputLayout);
        layout.findViewById(R.id.button0).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button1).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button2).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button3).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button4).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button5).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button6).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button7).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button8).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button9).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));

        builder.setView(layout);
        AlertDialog dialog = builder.create();
        layout.findViewById(R.id.buttonConfirm).setOnClickListener(v->{
            if (pinCodeTxt.getText().toString().equals(pinCode)) {
                AppService.setHideMode(true);
                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
            } else {
                textInputLayout.setError(getString(R.string.pin_code_is_not_correct));
                pinCodeTxt.setText("");
            }
        });
        layout.findViewById(R.id.buttonDelete).setOnClickListener(v->{
            textInputLayout.setError(null);
            if(pinCodeTxt.getText().toString().length() != 0){
                String text = pinCodeTxt.getText().toString();
                pinCodeTxt.setText(text.substring(0, text.length() - 1));
            }
        });
        layout.findViewById(R.id.buttonRemoveBack).setBackgroundResource(R.drawable.left_arrow_white);
        layout.findViewById(R.id.buttonRemoveBack).setOnClickListener(v->{
            dialog.dismiss();
        });
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }
    }
    private void onNumberClick(View v, TextView pinCodeTxt, TextInputLayout textInputLayout) {
        textInputLayout.setError(null);
        if(pinCodeTxt.getText().toString().length()<30) {
            String text = pinCodeTxt.getText().toString();
            pinCodeTxt.setText(text+((Button) v).getText());
        }
    }


    public void goMainTemplateClick(View view) {
        startActivity(new Intent(MainMenuActivity.this, MainTemplateActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_left);
    }

    public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            float diffX = event2.getX() - event1.getX();
            float diffY = event2.getY() - event1.getY();
            if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD &&
                    diffX < 0) {
                onBackPressed();
                return true;
            }
            return false;
        }
    }
}