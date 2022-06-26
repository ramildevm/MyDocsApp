package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class MainPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Intent intent = getIntent();
        TextView gtxt = findViewById(R.id.greating_txt);
        gtxt.setText(gtxt.getText().toString() +" "+ intent.getStringExtra("Login") +"!");
    }

    public void switchLangClick(View view) {
        Locale locale = null;
        if (((TextView)findViewById(R.id.email_txt)).getText().equals("Логин:"))
            locale = new Locale("en");
        else
            locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, null);
        //
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }
    public void goBackClick(View view) {
        NavUtils.navigateUpFromSameTask(this);
    }
}