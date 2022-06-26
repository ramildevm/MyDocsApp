package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydocsapp.api.User;
import com.example.mydocsapp.api.UserGetCallback;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    private void changeButtonBack(View view, int backSetId, boolean loginModeCheck, int duration){
        view.setBackground(ContextCompat.getDrawable(this,backSetId));
        TransitionDrawable transition = (TransitionDrawable) view.getBackground();
        transition.startTransition(duration);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                transition.startTransition(duration);
                transition.reverseTransition(duration);
                if(loginModeCheck)
                    loginMethod();
            }
        }, duration);
    }

    private void loginMethod() {
        String login = ((TextView)findViewById(R.id.editTextLogin)).getText().toString().replaceAll(" ", "");
        String password = ((TextView)findViewById(R.id.editTextPassword)).getText().toString().replaceAll(" ", "");
        if(login.length() == 0 | password.length()==0){
            Toast msg = Toast.makeText(MainActivity.this, R.string.error_not_all_fields_filled, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        MainApi.GetUser(login, new UserGetCallback() {
            @Override
            public void onResult(User user) {
                if(user != null) {
                    if (!password.equals(user.password.replaceAll(" ", ""))) {
                        Toast msg = Toast.makeText(MainActivity.this, R.string.error_passwords_are_not_same, Toast.LENGTH_SHORT);
                        msg.show();
                    } else
                        startActivity(new Intent(MainActivity.this, MainPageActivity.class).putExtra("Login", login));
                }
                else{
                    Toast msg = Toast.makeText(MainActivity.this, R.string.error_user_doesnt_exists, Toast.LENGTH_SHORT);
                    msg.show();
                }
            }
            @Override
            public void onError(Throwable e) {
                Toast msg = Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                msg.show();
            }
        });
    }

    public void loginBtnClick(View view) {
        changeButtonBack(view, R.drawable.main_button_click_set,true, 200);
    }
    public void reginBtnClick(View view) {
        changeButtonBack(view, R.drawable.main_button_click_set,false, 200);
    }

    public void goGuestModeClick(View view) {
        startActivity(new Intent(MainActivity.this, MainPageActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }

}