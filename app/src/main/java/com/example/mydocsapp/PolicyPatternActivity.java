package com.example.mydocsapp;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

public class PolicyPatternActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_pattern);
    }
    public void goBackMainPageClick(View view) {
        NavUtils.navigateUpFromSameTask(this);
    }
}