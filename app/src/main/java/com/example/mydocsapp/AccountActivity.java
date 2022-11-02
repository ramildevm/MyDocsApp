package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //TextView gtxt = findViewById(R.id.login_txt);
        //gtxt.setText(((App)getApplicationContext()).CurrentUser.login);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goAccountClickBack(getCurrentFocus());
    }

    public void goAccountClickBack(View view) {
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
    }

    public void goMainActivityClick(View view) {
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.alpha_in,R.anim.slide_out_left);
    }

    public void goSettingsClick(View view) {
        startActivity(new Intent(AccountActivity.this, SettingsActivity.class));
    }
}