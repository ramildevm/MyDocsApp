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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        gestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener());

        int userId = AppService.getUserId(this);
        DBHelper db = new DBHelper(this, userId);
        User user = db.getUserById(userId);
        Boolean isGuest = user.Login.equals("гость");
        if(isGuest) {
            binding.changeAccountTxt.setText("Выйти");
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
        setOnClickListeners(isGuest);
    }

    private void setOnClickListeners(Boolean isGuest) {
        binding.changeAccountTxt.setOnClickListener(v->changeAccountClickBack(v));
        binding.settingsImage.setOnClickListener(v->goSettingsClick(v));
        binding.settingsTxt.setOnClickListener(v->goSettingsClick(v));
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
            binding.hiddenPanel.setOnClickListener(v -> goAccountSettingsClick(v));
        }
    }

    private void goAccountSettingsClick(View v) {
    }

    private void goSyncingClick(View v) {
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
        editor.clear().commit();
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
    }

    public void goSettingsClick(View view) {
        startActivity(new Intent(MainMenuActivity.this, SettingsActivity.class));
        overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out);
    }
    public void goHidenFilesClick(View view) {
        AppService.setHideMode(true);
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
    }
    public void goMainTemplateClick(View view) {
        startActivity(new Intent(MainMenuActivity.this, MainTemplateActivity.class));
        overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out);
    }
    public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100; // Пороговое значение для определения свайпа
        private static final int SWIPE_VELOCITY_THRESHOLD = 100; // Пороговое значение для определения скорости свайпа

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