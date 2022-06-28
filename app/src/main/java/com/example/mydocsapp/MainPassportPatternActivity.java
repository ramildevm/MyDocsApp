package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainPassportPatternActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_passport_pattern);
    }

    public void goBackMainPageClick(View view) {
        startActivity(new Intent(MainPassportPatternActivity.this, MainContentActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }

    public void goSecondPagePassportPatternClick(View view) {
        startActivity(new Intent(MainPassportPatternActivity.this, SecondPassportPatternActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }
}