package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydocsapp.api.MainApi;
import com.example.mydocsapp.api.User;
import com.example.mydocsapp.api.UserPostCallback;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;

public class SignInActivity extends AppCompatActivity {

    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        db = new DBHelper(this, AppService.getUserId());
    }

    public void cancelBtnClick(View view) {
        overridePendingTransition(0, 0);
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(0, 0);
    }
    public void signupBtnClick(View view) {
        changeButtonBack(view, R.drawable.main_button_click_set, 200);
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
                signInMethod();

            }
        }, duration);
    }
    private void signInMethod() {
        String login = ((TextView)findViewById(R.id.editTextLogin)).getText().toString();
        String password = ((TextView)findViewById(R.id.editTextPassword)).getText().toString();
        String passwordConfirm = ((TextView)findViewById(R.id.editTextPasswordConfirm)).getText().toString();
        if(login.isEmpty() | password.isEmpty()){
            Toast msg = Toast.makeText(SignInActivity.this, R.string.error_not_all_fields_filled, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        if(!passwordConfirm.equals(password)){
            Toast msg = Toast.makeText(SignInActivity.this, R.string.error_dont_match_passwords, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }

        //fromAPI(login, password);
        fromDB(login, password);
    }

    private void fromDB(String login, String password) {
        Cursor cur = db.getUserByLogin(login);
        cur.moveToNext();
        if(cur!=null){
            Toast msg = Toast.makeText(SignInActivity.this, R.string.error_ligin_in_use, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        db.insertUser(new User(0,login,password,"None","None",null));
        AppService.setUserId(db.selectLastUserId());
        Toast msg = Toast.makeText(SignInActivity.this, R.string.result_successful_user_adding, Toast.LENGTH_SHORT);
        msg.show();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
        editor.putString("Login",cur.getString(1));
        editor.apply();

        Intent intent = new Intent(SignInActivity.this, MainContentActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out);
    }

    private void fromAPI(String login, String password) {
        MainApi.CreateUser(new User(0, login, password,"None","None",null), new UserPostCallback() {
            @Override
            public void onResult(User result) {
                if(result == null) {
                    Toast msg = Toast.makeText(SignInActivity.this, R.string.error_adding_error, Toast.LENGTH_SHORT);
                    msg.show();
                }
                else{
                    Toast msg = Toast.makeText(SignInActivity.this, R.string.result_successful_user_adding, Toast.LENGTH_SHORT);
                    msg.show();
                }
            }
            @Override
            public void onError(Throwable e) {
                Log.e("ErrorSQL",e.toString());
                Toast msg = Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                msg.show();
            }
        });
    }
}