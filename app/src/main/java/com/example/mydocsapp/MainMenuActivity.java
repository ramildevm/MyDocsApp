package com.example.mydocsapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.view.GestureDetectorCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mydocsapp.api.User;
import com.example.mydocsapp.databinding.ActivityMainMenuBinding;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;

public class MainMenuActivity extends AppCompatActivity {
    private GestureDetectorCompat gestureDetector;
    ActivityMainMenuBinding binding;
    private boolean isGuest = false;
    private DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        gestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener());
        setWindowData();
        setOnClickListeners(isGuest);
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
        isGuest = user.Id==0;
        if(isGuest) {
            binding.changeAccountTxt.setText(R.string.exit);
            binding.accountPanel.setVisibility(View.GONE);
            binding.syncingPanel.setAlpha(0.5f);
            binding.templatesPanel.setAlpha(0.5f);
            binding.loginTxt.setText(R.string.guest_mode);
            Glide.with(this)
                    .load(R.drawable.ic_person)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.accountPhotoImage);
            binding.accountPhotoImage.setPadding(50,50,50,50);
        }
        else {
            Glide.with(this)
                    .load(R.drawable.profile_photo_image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.accountPhotoImage);
            binding.loginTxt.setText(user.Login);
        }
    }
    private void setOnClickListeners(Boolean isGuest) {
        binding.changeAccountTxt.setOnClickListener(v->changeAccountClickBack(v));
        binding.settingsImage.setOnClickListener(v->goSettingsClick(v));
        binding.settingsTxt.setOnClickListener(v->goSettingsClick(v));
        binding.syncingPanel.setOnClickListener(v->goSyncingClick(v));
        binding.syncingPanel.setOnClickListener(v->goSyncingClick(v));
        if (isGuest) {
            binding.templatesPanel.setOnClickListener(v ->makeAccountDialog());
            binding.syncingPanel.setOnClickListener(v ->makeAccountDialog());
            binding.accountPanel.setOnClickListener(v -> makeAccountDialog());
            binding.hiddenPanel.setOnClickListener(v -> goHidenFilesClick(v));
        }
        else{
            binding.templatesPanel.setOnClickListener(v -> goMainTemplateClick(v));
            binding.syncingPanel.setOnClickListener(v -> goSyncingClick(v));
            binding.accountPanel.setOnClickListener(v -> goAccountSettingsClick(v));
            binding.hiddenPanel.setOnClickListener(v -> goHidenFilesClick(v));
        }
    }
    private void goAccountSettingsClick(View v) {
        startActivity(new Intent(this,AccountSettingsActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }
    private void goSyncingClick(View v) {
        startActivity(new Intent(this,SyncActivity.class));
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
        overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
    }
    public void changeAccountClickBack(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("email").commit();
        editor.remove("Id").commit();
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
    }
    public void goSettingsClick(View view) {
        startActivity(new Intent(MainMenuActivity.this, SettingsActivity.class));
        overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
    }
    public void goHidenFilesClick(View view) {
        AppService.setHideMode(true);
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
    }
    public void goMainTemplateClick(View view) {
        startActivity(new Intent(MainMenuActivity.this, MainTemplateActivity.class));
        overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
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