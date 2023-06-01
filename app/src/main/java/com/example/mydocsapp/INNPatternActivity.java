package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class INNPatternActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inn_pattern);
        overridePendingTransition(0, 0);
    }

    public void goBackMainPageClick(View view) {
        NavUtils.navigateUpFromSameTask(this);
    }
}