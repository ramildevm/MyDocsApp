package com.example.mydocsapp;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.mydocsapp.api.MainApi;
import com.example.mydocsapp.api.User;
import com.example.mydocsapp.api.UserGetCallback;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;

import java.io.IOException;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DBHelper(this, AppService.getUserId(this));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("Login", "");
        if(!name.equalsIgnoreCase(""))
        {
            Cursor cur = db.getUserByLogin(name);
            cur.moveToNext();
            int id = cur.getInt(0);
            AppService.setUserId(id, this);
            AppService.setHideMode(false);
            if(id!=0){
                Intent intent =new Intent(LoginActivity.this, MainContentActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(LoginActivity.this, MainContentActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out);
            }
        }
        try {
            db.create_db();
        }
        catch (IOException e){
            Log.e("SQLError",e.getMessage());
            e.printStackTrace();
        }
    }
    private void changeButtonBack(View view, int backSetId, int duration){
        view.setBackground(ContextCompat.getDrawable(this,backSetId));
        TransitionDrawable transition = (TransitionDrawable) view.getBackground();
        transition.startTransition(duration);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                transition.startTransition(duration);
                transition.reverseTransition(duration);
                loginMethod();

            }
        }, duration);
    }

    private void loginMethod() {
        String login = ((TextView)findViewById(R.id.editTextLogin)).getText().toString();
        String password = ((TextView)findViewById(R.id.editTextPassword)).getText().toString();
        if(login.isEmpty() | password.isEmpty()){
            Toast msg = Toast.makeText(LoginActivity.this, R.string.error_not_all_fields_filled, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        fromDB(login, password);

        //fromAPI(login, password);
    }

    private void fromDB(String login, String password) {
        Cursor cur = db.getUserByLogin(login);
        if(cur == null){
            Toast msg = Toast.makeText(LoginActivity.this, R.string.error_user_doesnt_exists, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        cur.moveToFirst();
        if(password.isEmpty()){
            Toast msg = Toast.makeText(LoginActivity.this, R.string.error_input_password, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        if (!password.equals(cur.getString(2))) {
            Toast msg = Toast.makeText(LoginActivity.this, R.string.error_passwords_are_not_same, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
        editor.putString("Login",cur.getString(1));
        editor.apply();

        AppService.setUserId(cur.getInt(0),this);
        Intent intent =new Intent(LoginActivity.this, MainContentActivity.class);
        startActivity(intent);
    }

    private void fromAPI(String login, String password) {
        MainApi.GetUser(login, new UserGetCallback() {
            @Override
            public void onResult(User user) {
                if(user != null) {
                    if (!password.equals(user.Password) && !password.isEmpty()) {
                        Toast msg = Toast.makeText(LoginActivity.this, R.string.error_passwords_are_not_same, Toast.LENGTH_SHORT);
                        msg.show();
                    } else {
                        db.insertUser(user);
                        Intent intent =new Intent(LoginActivity.this, MainContentActivity.class);
                        startActivity(intent);
                    }
                }
                else{
                    Toast msg = Toast.makeText(LoginActivity.this, R.string.error_user_doesnt_exists, Toast.LENGTH_SHORT);
                    msg.show();
                }
            }
            @Override
            public void onError(Throwable e) {
                Toast msg = Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                msg.show();
            }
        });
    }

    public void loginBtnClick(View view) {
        changeButtonBack(view, R.drawable.main_button_click_set, 200);
    }
    public void reginBtnClick(View view) {
        Bundle bundle = null;
        Pair<View, String> pair1 = Pair.create(findViewById(R.id.sign_up_btn), findViewById(R.id.sign_up_btn).getTransitionName());
        Pair<View, String> pair2 = Pair.create(findViewById(R.id.email_txt), findViewById(R.id.email_txt).getTransitionName());
        Pair<View, String> pair3 = Pair.create(findViewById(R.id.editTextLogin), findViewById(R.id.editTextLogin).getTransitionName());
        Pair<View, String> pair4 = Pair.create(findViewById(R.id.password_txt), findViewById(R.id.password_txt).getTransitionName());
        Pair<View, String> pair7 = Pair.create(findViewById(R.id.without_login), findViewById(R.id.without_login).getTransitionName());

        ActivityOptions options;
        options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pair1,pair2,pair3,pair4,pair7);
        bundle = options.toBundle();
        Intent intent = new Intent(this,SignInActivity.class);
        startActivity(intent, bundle);
    }

    public void goGuestModeClick(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
        editor.putString("Login","гость");
        editor.apply();
        AppService.setUserId(1,this);
        AppService.setHideMode(false);
        Intent intent = new Intent(LoginActivity.this, MainContentActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out);
    }

    public void switchLangClick(View view) {
        Locale locale = null;
        if (((TextView)findViewById(R.id.login_btn)).getText().equals("Выйти"))
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
}