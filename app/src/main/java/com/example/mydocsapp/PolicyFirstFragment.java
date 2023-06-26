package com.example.mydocsapp;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.databinding.FragmentPolicyFirstBinding;
import com.example.mydocsapp.interfaces.Changedable;
import com.example.mydocsapp.interfaces.IFragmentDataSaver;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Polis;
import com.example.mydocsapp.models.PolisStateViewModel;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.ValidationService;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PolicyFirstFragment extends Fragment implements IFragmentDataSaver {
    private static final int DB_IMAGE = 1;
    private static final int BITMAP_IMAGE = 0;
    FragmentPolicyFirstBinding binding;
    PolisStateViewModel model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void copyTextClick(View view){
        ViewParent parent = view.getParent();
        if (parent instanceof ConstraintLayout) {
            ConstraintLayout constraintLayout = (ConstraintLayout) parent;
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            int referencedId = constraintSet.getConstraint(view.getId()).layout.endToEnd;
            EditText linkedView = binding.getRoot().findViewById(referencedId);
            copyToClipboard(linkedView);
        }
    }

    @Override
    public boolean IsValidData() {
        String dateBirth = binding.editTextDateBirth.getText().toString();
        boolean flag = true;
        if (!ValidationService.isValidDateFormat(dateBirth,"dd-MM-yyyy") & !dateBirth.isEmpty()){
            binding.editTextDateBirth.setError(getString(R.string.invalid_date));
            binding.copy3.setVisibility(View.INVISIBLE);
            flag =  false;
        }
        return flag;
    }

    public void copyToClipboard(EditText editText) {
        String text = editText.getText().toString().trim();
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), R.string.text_copied, Toast.LENGTH_SHORT).show();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPolicyFirstBinding.inflate(getLayoutInflater());
        model = new ViewModelProvider(requireActivity()).get(PolisStateViewModel.class);
        loadData();
        binding.maleCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            ((Changedable)getContext()).setIsChanged(true);
        });
        binding.femaleCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            ((Changedable)getContext()).setIsChanged(true);
        });
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((Changedable)getActivity()).setIsChanged(true);
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        binding.editTextNumber.addTextChangedListener(textWatcher);
        binding.editTextFullName.addTextChangedListener(textWatcher);
        binding.editTextDateBirth.addTextChangedListener(textWatcher);
        binding.editTextDateValidUntil.addTextChangedListener(textWatcher);
        return binding.getRoot();
    }
    private void loadData() {
        Polis Polis = model.getState().getValue();
        binding.editTextNumber.setText(Polis.Number);
        binding.editTextFullName.setText(Polis.FIO);
        binding.editTextDateBirth.setText(Polis.BirthDate);
        if (Polis.Gender.equals("M"))
            binding.maleCheck.setChecked(true);
        else
            binding.femaleCheck.setChecked(true);
        binding.editTextDateValidUntil.setText(Polis.ValidUntil);
    }
    @Override
    public void SaveData() {
        Polis Polis = model.getState().getValue();
        Polis.Number = binding.editTextNumber.getText().toString();
        Polis.FIO = binding.editTextFullName.getText().toString();
        Polis.BirthDate = binding.editTextDateBirth.getText().toString();
        Polis.ValidUntil = binding.editTextDateValidUntil.getText().toString();
        if (binding.maleCheck.isChecked())
            Polis.Gender = "M";
        else
            Polis.Gender = "F";
        model.setState(Polis);
    }

    @Override
    public List<Bitmap> getPhotos() {
        return null;
    }
}