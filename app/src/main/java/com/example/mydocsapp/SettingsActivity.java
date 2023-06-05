package com.example.mydocsapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.mydocsapp.databinding.ActivitySettingsBinding;
import com.example.mydocsapp.services.AppService;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setOnClickListeners();
    }
    private void setOnClickListeners() {
        binding.menubarBack.setOnClickListener(v->goBackAccountClick(v));
        binding.languageImage.setOnClickListener(v->onLanguageBtnClick());
        binding.languageTxt.setOnClickListener(v->onLanguageBtnClick());
        binding.safetyImage.setOnClickListener(v->onSafetyBtnClick());
        binding.safetyTxt.setOnClickListener(v->onSafetyBtnClick());
        binding.helpImage.setOnClickListener(v->onHelpBtnClick());
        binding.helpTxt.setOnClickListener(v->onHelpBtnClick());
        binding.privacyPolicyImage.setOnClickListener(v->onPrivacyPolicyBtnClick());
        binding.privacyPolicyTxt.setOnClickListener(v->onPrivacyPolicyBtnClick());
        binding.aboutAppImage.setOnClickListener(v->onAboutBtnClick());
        binding.aboutAppTxt.setOnClickListener(v->onAboutBtnClick());
    }
    private void onSafetyBtnClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.pin_code_dialog_layout,null);
        AppCompatTextView pinCodeTxt = layout.findViewById(R.id.pinTextView);
        layout.findViewById(R.id.buttonConfirm).setVisibility(View.VISIBLE);
        layout.findViewById(R.id.buttonRemove).setVisibility(View.VISIBLE);
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
            if(pinCodeTxt.getText().toString().length()==4) {
                onConfirmPinCodeClick(pinCodeTxt.getText().toString());
                dialog.dismiss();
            }
            else
                textInputLayout.setError(getString(R.string.pin_code_not_enough));
        });
        layout.findViewById(R.id.buttonRemove).setOnClickListener(v->{
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(AppService.getUserId(this)+"").commit();
            Toast.makeText(this,R.string.pin_code_deleted,Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });
        layout.findViewById(R.id.buttonDelete).setOnClickListener(v->{
            textInputLayout.setError(null);
            if(pinCodeTxt.getText().toString().length() != 0){
                String text = pinCodeTxt.getText().toString();
                pinCodeTxt.setText(text.substring(0, text.length() - 1));
            }
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

    private void onConfirmPinCodeClick(String pinCode) {
        Toast.makeText(this,R.string.pin_code_created,Toast.LENGTH_LONG).show();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(AppService.getUserId(this)+"").commit();
        editor.putString(AppService.getUserId(this)+"",pinCode);
        editor.apply();
    }
    private void onNumberClick(View v, TextView pinCodeTxt, TextInputLayout textInputLayout) {
        textInputLayout.setError(null);
        if(pinCodeTxt.getText().toString().length()<4) {
            String text = pinCodeTxt.getText().toString();
            pinCodeTxt.setText(text+((Button) v).getText());
        }
    }
    private void onAboutBtnClick() {
        String text = getString(R.string.long_about_text);
        makeSettingsDialog(text);
    }
    private void onPrivacyPolicyBtnClick() {
        makeSettingsDialog(getString(R.string.privacy_policy_dialog_text));
    }
    private void onHelpBtnClick() {
        String link = "http:/vk.com/ebooii";
        makeSettingsDialog(getString(R.string.help_dialog_text) + link);
    }
    private void makeSettingsDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ScrollView scrollView = (ScrollView) getLayoutInflater().inflate(R.layout.settings_dialog_layout,null);
        TextView textView = scrollView.findViewById(R.id.textView);
        textView.setText(text);
        builder.setView(scrollView);
        builder.setPositiveButton("ОК", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.item_layout_bg);
        dialog.show();
    }
    private void onLanguageBtnClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_language);
        String[] languageOptions = {"Русский", "English"};
        Locale currentLocale = Locale.getDefault();
        String languageCode = currentLocale.getLanguage();
        int checkedItem = languageCode.equals("ru")?0:1;
        builder.setSingleChoiceItems(languageOptions, checkedItem, (dialog, which) -> {
            switch (which) {
                case 0:
                    setLocale("ru");
                    break;
                case 1:
                    setLocale("en");
                    break;
            }
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.item_layout_bg);
        dialog.show();
    }
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        Resources resources = getResources();
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        recreate();
    }
    public void goBackAccountClick(View view) {
        startActivity(new Intent(SettingsActivity.this, MainMenuActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }
}