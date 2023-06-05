package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.mydocsapp.databinding.ActivitySyncBinding;

public class SyncActivity extends AppCompatActivity {
    ActivitySyncBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyncBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.menubarBack.setOnClickListener(v->goBackSettingsClick(v));
        binding.uploadBtn.setOnClickListener(v->uploadBtnClick());
        binding.downloadBtn.setOnClickListener(v->downloadBtnClick());
    }
    private void uploadBtnClick() {
    }
    private void downloadBtnClick() {
    }
    public void goBackSettingsClick(View view) {
        onBackPressed();
    }
}