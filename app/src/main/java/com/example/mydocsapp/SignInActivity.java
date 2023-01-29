package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydocsapp.api.MainApi;
import com.example.mydocsapp.api.User;
import com.example.mydocsapp.api.UserPostCallback;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
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
        String login = ((TextView)findViewById(R.id.editTextLogin)).getText().toString().replaceAll(" ", "");
        String password = ((TextView)findViewById(R.id.editTextPassword)).getText().toString().replaceAll(" ", "");
        String passwordConfirm = ((TextView)findViewById(R.id.editTextPasswordConfirm)).getText().toString().replaceAll(" ", "");
        if(login.length() == 0 | password.length()==0){
            Toast msg = Toast.makeText(SignInActivity.this, R.string.error_not_all_fields_filled, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        if(passwordConfirm.equals(password)){
            Toast msg = Toast.makeText(SignInActivity.this, R.string.error_dont_match_passwords, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        MainApi.CreateUser(new User(0,login,password,"None","None",null), new UserPostCallback() {
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