package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.ItemAdapter;

import java.util.ArrayList;

public class AccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Intent intent = getIntent();
        TextView gtxt = findViewById(R.id.login_txt);
        gtxt.setText(intent.getStringExtra("Login"));
    }

    public void goAccountClickBack(View view) {
        startActivity(new Intent(AccountActivity.this, MainContentActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }

    public void goMainActivityClick(View view) {
        startActivity(new Intent(AccountActivity.this, MainActivity.class));
    }

    public void goSettingsClick(View view) {
        startActivity(new Intent(AccountActivity.this, SettingsActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }
}