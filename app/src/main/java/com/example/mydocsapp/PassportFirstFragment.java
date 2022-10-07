package com.example.mydocsapp;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mydocsapp.MainPassportPatternActivity.SELECT_USER_PHOTO;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.apputils.ImageSaveService;
import com.example.mydocsapp.databinding.FragmentPassportFirstBinding;
import com.example.mydocsapp.interfaces.FragmentSaveViewModel;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class PassportFirstFragment extends Fragment implements FragmentSaveViewModel {

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
        binding.userPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
        binding.userPassportPhoto.setImageBitmap(bitmap);
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPassportFirstBinding.inflate(getLayoutInflater());
        model = new ViewModelProvider(requireActivity()).get(PassportStateViewModel.class);
        loadData();

        binding.loadProfilePhotoBtn.setOnClickListener(v->{
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            getActivity().startActivityForResult(photoPickerIntent,SELECT_USER_PHOTO);
        });
        binding.userPassportPhoto.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(),ImageActivity.class);

            intent.putExtra("text",((App)getActivity().getApplicationContext()).CurrentItem.Title);

            byte[] imageByte = ImageSaveService.bitmapToByteArray(((BitmapDrawable) binding.userPassportPhoto.getDrawable()).getBitmap());
            String fileName = "SomeName.png";
            try {
                FileOutputStream fileOutStream = getContext().openFileOutput(fileName, MODE_PRIVATE);
                fileOutStream.write(imageByte);  //b is byte array
                //(used if you have your picture downloaded
                // from the *Web* or got it from the *devices camera*)
                //otherwise this technique is useless
                fileOutStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            intent.putExtra("imageFile", fileName);
            getActivity().startActivity(intent);
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
                binding.userPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                binding.userPassportPhoto.setImageBitmap(BitmapFactory.decodeByteArray(passport.FacePhoto, 0, passport.FacePhoto.length));
            }
        }
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
            byte[] imgByte = ImageSaveService.bitmapToByteArray(((BitmapDrawable) binding.userPassportPhoto.getDrawable()).getBitmap());
            passport.FacePhoto = imgByte;
        }
        model.setState(passport);
    }
}