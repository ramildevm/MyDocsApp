package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CardPatternActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_pattern);
    }

    public void goBackMainPageClick(View view) {
        startActivity(new Intent(CardPatternActivity.this, MainContentActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }
}