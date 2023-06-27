package com.example.mydocsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.mydocsapp.api.MainApiService;
import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.models.User;
import com.example.mydocsapp.api.ResponseCallback;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.CryptoService;
import com.example.mydocsapp.services.DBHelper;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignInActivity extends AppCompatActivity {

    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        db = new DBHelper(this, AppService.getUserId(this));
        findViewById(R.id.sign_up_btn).setOnClickListener(v->signupBtnClick(v));
        findViewById(R.id.cancel_btn).setOnClickListener(v->cancelBtnClick(v));
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        EditText editTextPasswordConfirm = findViewById(R.id.editTextPasswordConfirm);
        EditText editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                editTextPassword.setError(null);
            }
        });
        editTextPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                editTextPasswordConfirm.setError(null);
            }
        });
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String email = editable.toString().trim();
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextEmail.setError(getString(R.string.invalid_email));
                } else
                    editTextPassword.setError(null);
            }
        });
    }
    public void cancelBtnClick(View view) {
        overridePendingTransition(0, 0);
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(0, 0);
    }
    public void signupBtnClick(View view) {
        signInMethod();
    }
    private void signInMethod() {
        String email = ((EditText)findViewById(R.id.editTextEmail)).getText().toString();
        String password = ((EditText)findViewById(R.id.editTextPassword)).getText().toString();
        String passwordConfirm = ((EditText)findViewById(R.id.editTextPasswordConfirm)).getText().toString();

        if(email.isEmpty()){
            ((EditText)findViewById(R.id.editTextEmail)).setError(getString(R.string.error_not_all_fields_filled));
            return;
        }
        if(password.isEmpty()){
            ((EditText)findViewById(R.id.editTextPassword)).setError(getString(R.string.error_not_all_fields_filled));
            return;
        }
        else if(password.length()<8){
            ((EditText)findViewById(R.id.editTextPassword)).setError(getString(R.string.password_short));
            return;
        }
        if(passwordConfirm.isEmpty()){
            ((EditText)findViewById(R.id.editTextPasswordConfirm)).setError(getString(R.string.error_not_all_fields_filled));
            return;
        }
        if(!passwordConfirm.equals(password)){
            ((EditText)findViewById(R.id.editTextPassword)).setError(getString(R.string.error_dont_match_passwords));
            ((EditText)findViewById(R.id.editTextPasswordConfirm)).setError(getString(R.string.error_dont_match_passwords));
            return;
        }
        fromAPI(email, password);
        //fromDB(email, password);
    }
    private void fromAPI(String email, String password) {
        MainApiService mainApiService = new MainApiService(this);
        long datetimeMillis = new Date().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDateTime = dateFormat.format(new Date(datetimeMillis));
        User user = new User(0, email, email, password, null, null, formattedDateTime);
        user.Photo = ImageService.getPhoto(user.Photo);
        user.Password = CryptoService.encryptString(this,BuildConfig.ENCRYPTION_KEY_2,user.Password);
        mainApiService.CreateUser(user, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData;
                try {
                    decryptedData = CryptoService.Decrypt(encryptedData);
                    User user = new Gson().fromJson(decryptedData, User.class);
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + user.Id+"/UserPhotoFolder/";
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    user.Photo = ImageService.getPhotoFile(user.Photo, imgPath+"UserPhoto"+"_file");
                    db.insertUser(user);
                    Toast.makeText(SignInActivity.this, R.string.result_successful_user_adding, Toast.LENGTH_SHORT).show();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("email").commit();
                    editor.remove("Id").commit();
                    editor.putString("email", email);
                    editor.apply();
                    AppService.setUserId(user.Id,SignInActivity.this);
                    AppService.setHideMode(false);
                    Intent intent = new Intent(SignInActivity.this, MainContentActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out);
                }
                catch (Exception e){
                    Toast.makeText(SignInActivity.this, "Some error", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onConflict() {
                Toast.makeText(SignInActivity.this, R.string.error_email_in_use, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("Sign in error", errorMessage);
            }
        });
    }
    private void fromDB(String email, String password) {
        User user = db.getUserByEmail(email);
        if(user!=null){
            Toast.makeText(SignInActivity.this, R.string.error_email_in_use, Toast.LENGTH_SHORT).show();
            return;
        }
        long datetimeMillis = new Date().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDateTime = dateFormat.format(new Date(datetimeMillis));
        db.insertUser(new User(0,email,email,password,null,null,formattedDateTime));
        Toast msg = Toast.makeText(SignInActivity.this, R.string.result_successful_user_adding, Toast.LENGTH_SHORT);
        msg.show();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("email").commit();
        editor.remove("Id").commit();
        editor.putString("email",email);
        editor.apply();
        AppService.setUserId(db.selectLastUserId(),this);
        user = db.getUserByEmail(email);
        AppService.setUserId(user.Id,this);
        AppService.setHideMode(false);
        Intent intent = new Intent(SignInActivity.this, MainContentActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out);
    }
}