package com.example.mydocsapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.databinding.FragmentPassportFirstBinding;
import com.example.mydocsapp.interfaces.FragmentSaveViewModel;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;

import java.io.ByteArrayOutputStream;


public class PassportFirstFragment extends Fragment implements FragmentSaveViewModel {

    private static final int SELECT_PHOTO = 1;
    FragmentPassportFirstBinding binding;
    PassportStateViewModel model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }
    public void loadProfileImage(Bitmap bitmap){
        binding.userPassportPhoto.setTag(1);
        binding.userPassportPhoto.setImageBitmap(bitmap);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPassportFirstBinding.inflate(getLayoutInflater());
        model = new ViewModelProvider(requireActivity()).get(PassportStateViewModel.class);
        loadData();

        binding.userPassportPhoto.setOnClickListener(v->{
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            getActivity().startActivityForResult(photoPickerIntent,SELECT_PHOTO);
        });
        return binding.getRoot();
    }

    private void loadData() {
        Passport passport = model.getState().getValue();
        //photo load
        binding.editTextSeriesNumber.setText(passport.SeriaNomer);
        binding.editTextDivisionCode.setText(passport.DivisionCode);
        binding.editTextDateIssue.setText(passport.GiveDate);
        binding.editTextIssuedWhom.setText(passport.ByWhom);
        binding.editTextFullName.setText(passport.FIO);
        binding.editTextDateBirth.setText(passport.BirthDate);
        if(passport.Gender.equals("M"))
            binding.maleCheck.setChecked(true);
        else
            binding.femaleCheck.setChecked(true);
        binding.editTextPlaceBirth.setText(passport.BirthPlace);
        binding.editTextPlaceResidence.setText(passport.ResidencePlace);
        if(passport.FacePhoto != null) {
            if (passport.FacePhoto.length != 0) {
                binding.userPassportPhoto.setTag(1);
                binding.userPassportPhoto.setImageBitmap(BitmapFactory.decodeByteArray(passport.FacePhoto, 0, passport.FacePhoto.length));
            }
        }
    }
    public byte[] bitmapToByteArray(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    @Override
    public void SaveData() {
        Passport passport = model.getState().getValue();
        passport.FacePhoto = null;
        passport.SeriaNomer = binding.editTextSeriesNumber.getText().toString();
        passport.DivisionCode =binding.editTextDivisionCode.getText().toString();
        passport.GiveDate =binding.editTextDateIssue.getText().toString();
        passport.ByWhom =binding.editTextIssuedWhom.getText().toString();
        passport.FIO = binding.editTextFullName.getText().toString();
        passport.BirthDate = binding.editTextDateBirth.getText().toString();
        if(binding.maleCheck.isChecked())
            passport.Gender = "M";
        else
            passport.Gender = "F";
        passport.BirthPlace = binding.editTextPlaceBirth.getText().toString();
        passport.ResidencePlace = binding.editTextPlaceResidence.getText().toString();

        if(binding.userPassportPhoto.getTag().toString().equals("1")) {
            byte[] imgByte = bitmapToByteArray(((BitmapDrawable) binding.userPassportPhoto.getDrawable()).getBitmap());
            passport.FacePhoto = imgByte;
        }
        model.setState(passport);
    }
}