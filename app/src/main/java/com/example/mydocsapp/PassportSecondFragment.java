package com.example.mydocsapp;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mydocsapp.MainPassportPatternActivity.SELECT_PAGE1_PHOTO;
import static com.example.mydocsapp.MainPassportPatternActivity.SELECT_PAGE2_PHOTO;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PassportSecondFragment extends Fragment implements FragmentSaveViewModel {

    FragmentPassportSecondBinding binding;
    PassportStateViewModel model;
    private Bitmap pagePhoto1;
    private Bitmap pagePhoto2;
    private OutputStream out;

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

    public boolean getPhotoOption(){
        return binding.usePhotoOption.isChecked();
    }
    private void loadData() {
        Passport passport = model.getState().getValue();
        //photo load
        if(passport.PhotoPage1 != null) {
            if (passport.PhotoPage1.length() != 0) {
                binding.firstPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                binding.firstPassportPhoto.setImageBitmap(BitmapFactory.decodeFile(passport.PhotoPage1));
            }
        }
        if(passport.PhotoPage2 != null) {
            if (passport.PhotoPage2.length() != 0) {
                binding.secondPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                binding.secondPassportPhoto.setImageBitmap(BitmapFactory.decodeFile(passport.PhotoPage2));
            }
        }

        binding.usePhotoOption.setChecked(((MainPassportPatternActivity)getActivity()).getCurrentItem().Image!=null);
    }

    @Override
    public void SaveData() {
    }

    @Override
    public void SavePhotos(int PassportId, int ItemId) {
        Passport passport = model.getState().getValue();
        if (pagePhoto1 != null) {
            if (passport.PhotoPage1!=null) {
                File filePath = new File(passport.PhotoPage1);
                filePath.delete();
            }
            passport.PhotoPage1 = savePhotoToFile(PassportId, ItemId,pagePhoto1,"PassportPagePhotoFirst");
        }
        if (pagePhoto2 != null) {
            if (passport.PhotoPage2 != null) {
                File filePath = new File(passport.PhotoPage2);
                filePath.delete();
            }
            passport.PhotoPage2 = savePhotoToFile(PassportId, ItemId,pagePhoto2, "PassportPagePhotoSecond");
        }
        model.setState(passport);
    }

    @NonNull
    private String savePhotoToFile(int PassportId, int ItemId, Bitmap pagePhoto, String fileName) {
        File filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String imgPath = filepath.getAbsolutePath() + "/"+ MainContentActivity.APPLICATION_NAME + "/Item" + ItemId + "/";
        File dir = new File(imgPath);
        if(!dir.exists())
            dir.mkdirs();
        String imgName = fileName + PassportId + System.currentTimeMillis() + ".jpg";
        File imgFile = new File(dir, imgName);
        try {
            out = new FileOutputStream(imgFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pagePhoto.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgFile.getAbsolutePath();
    }

    public void loadPassportPage1(Bitmap bitmap) {
        pagePhoto1 = bitmap;
        binding.firstPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
        binding.firstPassportPhoto.setImageBitmap(bitmap);
    }
    public void loadPassportPage2(Bitmap bitmap) {
        pagePhoto2 = bitmap;
        binding.secondPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
        binding.secondPassportPhoto.setImageBitmap(Bitmap.createScaledBitmap(bitmap, binding.secondPassportPhoto.getWidth(),ImageSaveService.dpToPx(getContext(),230),false));
    }
}