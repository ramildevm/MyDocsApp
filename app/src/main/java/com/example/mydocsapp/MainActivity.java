package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydocsapp.api.MainApi;
import com.example.mydocsapp.api.User;
import com.example.mydocsapp.api.UserGetCallback;
import com.example.mydocsapp.models.DBHelper;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DBHelper(this);
        try {
            db.create_db();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        try {
            db.deleteUser(1);
        }
        catch (Exception e){}
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
                    if (!password.equals(user.password.replaceAll(" ", "")) && !password.isEmpty()) {
                        Toast msg = Toast.makeText(MainActivity.this, R.string.error_passwords_are_not_same, Toast.LENGTH_SHORT);
                        msg.show();
                    } else {
                        db.insertUser(user);
                        Intent intent =new Intent(MainActivity.this, MainContentActivity.class);
                        startActivity(intent);
                    }
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
        options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, pair1,pair2,pair3,pair4,pair7);
        bundle = options.toBundle();
        Intent intent = new Intent(this,SignInActivity.class);
        startActivity(intent, bundle);
    }

    public void goGuestModeClick(View view) {
        Intent intent = new Intent(MainActivity.this, MainContentActivity.class);
        User user = new User(getString(R.string.extra_guest),"");
        db.insertUser(user);
        startActivity(intent);
        overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out);
    }

    private void intentPutUserExtras(Intent intent, User user) {
        intent.putExtra("user_login",user.login);
        intent.putExtra("user_password",user.password);
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