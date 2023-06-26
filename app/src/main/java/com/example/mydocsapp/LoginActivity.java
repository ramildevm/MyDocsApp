package com.example.mydocsapp;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.mydocsapp.api.MainApiService;
import com.example.mydocsapp.api.ResponseCallback;
import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.models.User;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.CryptoService;
import com.example.mydocsapp.services.DBHelper;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = new DBHelper(this, AppService.getUserId(this));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("email", "");
        if (!name.equalsIgnoreCase("")) {
            User user = db.getUserByEmail(name);
            int id = user.Id;
            AppService.setUserId(id, this);
            AppService.setHideMode(false);
            String pinCode = user.AccessCode;
            if(pinCode == null) {
                Intent intent = new Intent(LoginActivity.this, MainContentActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
            }
            else{
                makePinCodeDialog(pinCode);
            }
        }
        try {
            db.create_db();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EditText editTextEmail = findViewById(R.id.editTextEmail);
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextEmail.setError(getString(R.string.invalid_email));
                }else {
                    editTextEmail.setError(null);
                }
            }
        });
    }
    private void makePinCodeDialog(String pinCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.pin_code_dialog_layout,null);
        AppCompatTextView pinCodeTxt = layout.findViewById(R.id.pinTextView);
        TextInputLayout textInputLayout = layout.findViewById(R.id.textInputLayout);
        layout.findViewById(R.id.button0).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button1).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button2).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button3).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button4).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button5).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button6).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button7).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button8).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));
        layout.findViewById(R.id.button9).setOnClickListener(v->onNumberClick(v,pinCodeTxt,textInputLayout));

        builder.setView(layout);
        AlertDialog dialog = builder.create();
        layout.findViewById(R.id.buttonConfirm).setOnClickListener(v->{
            if (pinCodeTxt.getText().toString().equals(pinCode)) {
                Intent intent = new Intent(LoginActivity.this, MainContentActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
            } else {
                textInputLayout.setError(getString(R.string.pin_code_is_not_correct));
                pinCodeTxt.setText("");
            }
        });
        layout.findViewById(R.id.buttonDelete).setOnClickListener(v->{
            textInputLayout.setError(null);
            if(pinCodeTxt.getText().toString().length() != 0){
                String text = pinCodeTxt.getText().toString();
                pinCodeTxt.setText(text.substring(0, text.length() - 1));
            }
        });
        layout.findViewById(R.id.buttonRemoveBack).setBackgroundResource(R.drawable.left_arrow_white);
        layout.findViewById(R.id.buttonRemoveBack).setOnClickListener(v->{
            dialog.dismiss();
        });
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }
    }
    private void onNumberClick(View v, TextView pinCodeTxt, TextInputLayout textInputLayout) {
        textInputLayout.setError(null);
        if(pinCodeTxt.getText().toString().length()<30) {
            String text = pinCodeTxt.getText().toString();
            pinCodeTxt.setText(text+((Button) v).getText());
        }
    }
    private void loginMethod() {
        String email = ((TextView) findViewById(R.id.editTextEmail)).getText().toString();
        String password = ((TextView) findViewById(R.id.editTextPassword)).getText().toString();
        if (email.isEmpty() | password.isEmpty()) {
            Toast msg = Toast.makeText(LoginActivity.this, R.string.error_not_all_fields_filled, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        fromAPI(email, password);
    }

    @Override
    public void onBackPressed() {
        this.onDestroy();
    }

    private void fromDB(String email, String password) {
        User user = db.getUserByEmail(email);
        if (user == null) {
            Toast.makeText(LoginActivity.this, R.string.error_user_doesnt_exists, Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, R.string.error_input_password, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(user.Password)) {
            Toast.makeText(LoginActivity.this, R.string.error_passwords_are_not_same, Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("email").commit();
        editor.remove("Id").commit();
        editor.putString("email", user.Email);
        editor.apply();
        AppService.setUserId(user.Id, this);
        String pinCode = preferences.getString(user.Id+"", "");
        if(pinCode.equalsIgnoreCase("")) {
            Intent intent = new Intent(LoginActivity.this, MainContentActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        }
        else
            makePinCodeDialog(pinCode);
    }
    public void loginBtnClick(View view) {loginMethod();}
    public void reginBtnClick(View view) {
        Bundle bundle = null;
        Pair<View, String> pair1 = Pair.create(findViewById(R.id.sign_up_btn), findViewById(R.id.sign_up_btn).getTransitionName());
        Pair<View, String> pair2 = Pair.create(findViewById(R.id.email_txt), findViewById(R.id.email_txt).getTransitionName());
        Pair<View, String> pair3 = Pair.create(findViewById(R.id.editTextEmail), findViewById(R.id.editTextEmail).getTransitionName());
        Pair<View, String> pair4 = Pair.create(findViewById(R.id.password_txt), findViewById(R.id.password_txt).getTransitionName());
        Pair<View, String> pair7 = Pair.create(findViewById(R.id.without_login), findViewById(R.id.without_login).getTransitionName());
        ActivityOptions options;
        options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pair1, pair2, pair3, pair4, pair7);
        bundle = options.toBundle();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("email").commit();
        editor.remove("Id").commit();
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent, bundle);
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }
    public void goGuestModeClick(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("email").commit();
        editor.remove("Id").commit();
        editor.putString("email", "0" );
        editor.apply();
        AppService.setUserId(0, this);
        AppService.setHideMode(false);
        String pinCode = preferences.getString(0+"", "");
        if(pinCode.equalsIgnoreCase("")) {
            Intent intent = new Intent(LoginActivity.this, MainContentActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        }
        else
            makePinCodeDialog(pinCode);
    }

    private void fromAPI(String email, String password) {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetUser(email, password, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = null;
                try {
                    decryptedData = CryptoService.Decrypt(encryptedData);
                    User user = new Gson().fromJson(decryptedData, User.class);
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + user.Id+"/UserPhotoFolder/";
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    user.Photo = ImageService.getPhotoFile(user.Photo, imgPath+"UserPhoto"+"_file");
                    if(db.getUserById(user.Id)==null)
                        db.insertUser(user);
                    db.updateUserDate(user.Id, user.UpdateTime);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("email").commit();
                    editor.remove("Id").commit();
                    editor.putString("email", user.Email);
                    editor.apply();
                    AppService.setUserId(user.Id, LoginActivity.this);
                    String pinCode = preferences.getString(user.Id+"", "");
                    if(pinCode.equalsIgnoreCase("")) {
                        Intent intent = new Intent(LoginActivity.this, MainContentActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                    }
                    else
                        makePinCodeDialog(pinCode);
                }
                catch (Exception ex){

                }
            }

            @Override
            public void onConflict() {
                Toast msg = Toast.makeText(LoginActivity.this, R.string.error_user_doesnt_exists, Toast.LENGTH_SHORT);
                msg.show();

            }

            @Override
            public void onError(String errorMessage) {
                Log.e("Login error",errorMessage);
            }
        });
    }

}