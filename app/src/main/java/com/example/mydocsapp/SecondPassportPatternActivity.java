package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SecondPassportPatternActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_passport_pattern);
    }

    public void goBackMainPageClick(View view) {
        startActivity(new Intent(SecondPassportPatternActivity.this, MainContentActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }

    public void goBackMainPagePassportPatternClick(View view) {
        startActivity(new Intent(SecondPassportPatternActivity.this, MainPassportPatternActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }

}