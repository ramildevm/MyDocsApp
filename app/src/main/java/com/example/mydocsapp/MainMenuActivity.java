package com.example.mydocsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.view.GestureDetectorCompat;

import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;

public class MainMenuActivity extends AppCompatActivity {
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        gestureDetector = new GestureDetectorCompat(this, new SwipeGestureListener());

        TextView greetingTxt = findViewById(R.id.login_txt);
        int userId = AppService.getUserId(this);
        DBHelper db = new DBHelper(this, userId);
        Cursor cur = db.getUserById(userId);
        cur.moveToNext();
        greetingTxt.setText(cur.getString(1));
        if(cur.getString(1).equals("гость"))
            ((TextView)findViewById(R.id.change_account_txt)).setText("Выйти");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goMainMenuClickBack(getCurrentFocus());
    }

    public void goMainMenuClickBack(View view) {
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

            // Определение направления свайпа
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