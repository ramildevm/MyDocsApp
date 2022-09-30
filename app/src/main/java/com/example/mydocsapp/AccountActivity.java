package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        TextView gtxt = findViewById(R.id.login_txt);
        gtxt.setText(((App)getApplicationContext()).CurrentUser.login);
    }

    public void goAccountClickBack(View view) {
        startActivity(new Intent(AccountActivity.this, MainContentActivity.class));
    }

    public void goMainActivityClick(View view) {
        startActivity(new Intent(AccountActivity.this, MainActivity.class));
    }

    public void goSettingsClick(View view) {
        startActivity(new Intent(AccountActivity.this, SettingsActivity.class));
    }
}