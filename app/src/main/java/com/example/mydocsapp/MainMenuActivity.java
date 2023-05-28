package com.example.mydocsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;

public class MainMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        TextView gtxt = findViewById(R.id.login_txt);
        int userId = AppService.getUserId(this);
        DBHelper db = new DBHelper(this, userId);
        Cursor cur = db.getUserById(userId);
        cur.moveToNext();
        gtxt.setText(cur.getString(1));
        if(cur.getString(1).equals("гость"))
            ((TextView)findViewById(R.id.change_account_txt)).setText("Выйти");
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
}