package com.example.mydocsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mydocsapp.api.User;
import com.example.mydocsapp.databinding.ActivityAccountSettingsBinding;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDateTime;

public class AccountSettingsActivity extends AppCompatActivity {
    ActivityAccountSettingsBinding binding;
    private DBHelper db;
    private User User;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = new DBHelper(this, AppService.getUserId(this));
        binding.saveBtn.setOnClickListener(v->saveMethod());
        binding.menubarBack.setOnClickListener(v->onBackPressed());
        loadUserData();
    }

    private void loadUserData() {
        User = db.getUserById(AppService.getUserId(this));
        ((TextView)findViewById(R.id.editTextEmail)).setText(User.Email);
        ((TextView)findViewById(R.id.editTextPassword)).setText(User.Password);
        ((TextView)findViewById(R.id.editTextPasswordConfirm)).setText(User.Password);
        ((TextView)findViewById(R.id.editTextLogin)).setText(User.Login);
    }

    private void saveMethod() {
        String email = ((TextView)findViewById(R.id.editTextEmail)).getText().toString();
        String login = ((TextView)findViewById(R.id.editTextLogin)).getText().toString();
        String password = ((TextView)findViewById(R.id.editTextPassword)).getText().toString();
        String passwordConfirm = ((TextView)findViewById(R.id.editTextPasswordConfirm)).getText().toString();
        if(email.isEmpty() | password.isEmpty() | login.isEmpty()){
            Toast msg = Toast.makeText(this, R.string.error_not_all_fields_filled, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        if(!passwordConfirm.equals(password)){
            Toast msg = Toast.makeText(this, R.string.error_dont_match_passwords, Toast.LENGTH_SHORT);
            msg.show();
            return;
        }
        //fromAPI(login, password);
        if(!email.equals(User.Email)) {
            User user = db.getUserByEmail(email);
            if (user != null) {
                Toast.makeText(this, R.string.error_email_in_use, Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("email").commit();
            editor.remove("Id").commit();
            editor.putString("email", User.Login);
            AppService.setUserId(User.Id,this);

        }
        User.Email = email;
        User.Login = login;
        User.Password = password;
        db.updateUser(User.Id,User);
        onBackPressed();
    }
}