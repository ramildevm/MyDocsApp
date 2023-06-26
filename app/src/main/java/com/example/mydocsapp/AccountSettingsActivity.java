package com.example.mydocsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mydocsapp.api.MainApiService;
import com.example.mydocsapp.api.ResponseCallback;
import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.models.User;
import com.example.mydocsapp.databinding.ActivityAccountSettingsBinding;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.CryptoService;
import com.example.mydocsapp.services.DBHelper;
import com.google.gson.Gson;

import java.io.File;

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
        binding.editTextEmail.addTextChangedListener(new TextWatcher() {
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
                    binding.editTextEmail.setError(getString(R.string.invalid_email));
                } else
                    binding.editTextPassword.setError(null);
            }
        });
    }
    private void loadUserData() {
        User = db.getUserById(AppService.getUserId(this));
        ((TextView)findViewById(R.id.editTextEmail)).setText(User.Email);
        ((TextView)findViewById(R.id.editTextLogin)).setText(User.Login);
    }
    private void saveMethod() {
        binding.editTextEmail.setError(null);
        binding.editTextLogin.setError(null);
        binding.oldEditTextPassword.setError(null);
        binding.editTextPassword.setError(null);
        binding.editTextPasswordConfirm.setError(null);

        String email = (binding.editTextEmail).getText().toString();
        String login = ((binding.editTextLogin)).getText().toString();
        String oldPassword = ((binding.oldEditTextPassword)).getText().toString();
        String password = ((TextView)findViewById(R.id.editTextPassword)).getText().toString();
        String passwordConfirm = ((TextView)findViewById(R.id.editTextPasswordConfirm)).getText().toString();

        User user = db.getUserById(AppService.getUserId(this));
        if(email.isEmpty()){
            binding.editTextEmail.setError(getString(R.string.error_not_all_fields_filled));
            return;
        }
        if(login.length()<1){
            binding.editTextLogin.setError(getString(R.string.error_login_short));
            return;
        }
        if(oldPassword.isEmpty()){
            binding.oldEditTextPassword.setError(getString(R.string.error_not_all_fields_filled));
            return;
        }
        if (!oldPassword.equals(db.getUserById(AppService.getUserId(this)).Password)) {
            binding.oldEditTextPassword.setError(getString(R.string.error_dont_match_passwords));
            return;
        }
        if(!(password.isEmpty() && passwordConfirm.isEmpty()) && !passwordConfirm.equals(password) )
        {
            binding.editTextPassword.setError(getString(R.string.error_dont_match_passwords));
            binding.editTextPasswordConfirm.setError(getString(R.string.error_dont_match_passwords));
            return;
        }
        if((password.equals(passwordConfirm)) && password.length() < 8 && password.length() > 0)
        {
            binding.editTextPassword.setError(getString(R.string.password_short));
            return;
        }
        MainApiService mainApiService = new MainApiService(this);
        user.Photo = ImageService.getPhoto(User.Photo);
        user.Email = email;
        user.Login = login;
        if(!password.isEmpty())
            user.Password = password;
        mainApiService.UpdateUser(user, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = null;
                try {
                    decryptedData = CryptoService.Decrypt(encryptedData);
                    User user = new Gson().fromJson(decryptedData, User.class);
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + user.Id+"UserPhotoFolder";
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    user.Photo = ImageService.getPhotoFile(user.Photo, imgPath+"/UserPhoto");
                    db.updateUser(user.Id,user);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AccountSettingsActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("email").commit();
                    editor.putString("email", user.Email);
                    onBackPressed();
                }
                catch (Exception ex){
                }
            }
            @Override
            public void onConflict() {
                Toast msg = Toast.makeText(AccountSettingsActivity.this, R.string.error_email_in_use, Toast.LENGTH_SHORT);
                msg.show();
            }
            @Override
            public void onError(String errorMessage) {
                Log.e("Login error",errorMessage);
            }
        });
    }
}