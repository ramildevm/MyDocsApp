package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PolicyPatternActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_pattern);
    }

    public void goBackMainPageClick(View view) {
        startActivity(new Intent(PolicyPatternActivity.this, MainContentActivity.class));
    }
}