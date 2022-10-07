package com.example.mydocsapp;

import static com.example.mydocsapp.MainPassportPatternActivity.SELECT_PAGE1_PHOTO;
import static com.example.mydocsapp.MainPassportPatternActivity.SELECT_PAGE2_PHOTO;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.apputils.ImageSaveService;
import com.example.mydocsapp.databinding.FragmentPassportSecondBinding;
import com.example.mydocsapp.interfaces.FragmentSaveViewModel;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;

public class PassportSecondFragment extends Fragment implements FragmentSaveViewModel {

    FragmentPassportSecondBinding binding;
    PassportStateViewModel model;
    public PassportSecondFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPassportSecondBinding.inflate(inflater);
        binding.firstPassportPhoto.setOnClickListener(view -> {
            Passport passport = model.getState().getValue();
            passport.PhotoPage1 = null;
            model.setState(passport);
        });
        model = new ViewModelProvider(requireActivity()).get(PassportStateViewModel.class);

        loadData();
        binding.firstPassportPhoto.setOnClickListener(v->{
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            getActivity().startActivityForResult(photoPickerIntent,SELECT_PAGE1_PHOTO);
        });
        binding.secondPassportPhoto.setOnClickListener(v->{
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            getActivity().startActivityForResult(photoPickerIntent,SELECT_PAGE2_PHOTO);
        });
        return binding.getRoot();
    }

    private void loadData() {
        Passport passport = model.getState().getValue();
        //photo load
        if(passport.PhotoPage1 != null) {
            if (passport.PhotoPage1.length != 0) {
                binding.firstPassportPhoto.setTag(1);
                binding.firstPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                binding.firstPassportPhoto.setImageBitmap(BitmapFactory.decodeByteArray(passport.PhotoPage1, 0, passport.PhotoPage1.length));
            }
        }
        if(passport.PhotoPage2 != null) {
            if (passport.PhotoPage2.length != 0) {
                binding.secondPassportPhoto.setTag(1);
                binding.secondPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                binding.secondPassportPhoto.setImageBitmap(BitmapFactory.decodeByteArray(passport.PhotoPage2, 0, passport.PhotoPage2.length));
            }
        }
    }

    @Override
    public void SaveData() {
        Passport passport = model.getState().getValue();

        if(binding.firstPassportPhoto.getTag().toString().equals("1")) {
            byte[] imgByte = ImageSaveService.bitmapToByteArray(((BitmapDrawable) binding.firstPassportPhoto.getDrawable()).getBitmap());
            passport.PhotoPage1 = imgByte;
        }
        if(binding.secondPassportPhoto.getTag().toString().equals("1")) {
            byte[] imgByte = ImageSaveService.bitmapToByteArray(((BitmapDrawable) binding.secondPassportPhoto.getDrawable()).getBitmap());
            passport.PhotoPage2 = imgByte;
        }
        model.setState(passport);
    }

    public void loadPassportPage1(Bitmap bitmap) {
        binding.firstPassportPhoto.setTag(1);
        binding.firstPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
        binding.firstPassportPhoto.setImageBitmap(bitmap);
    }
    public void loadPassportPage2(Bitmap bitmap) {
        binding.secondPassportPhoto.setTag(1);
        binding.secondPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
        binding.secondPassportPhoto.setImageBitmap(Bitmap.createScaledBitmap(bitmap, binding.secondPassportPhoto.getWidth(),ImageSaveService.dpToPx(getContext(),230),false));
    }
}