package com.example.mydocsapp;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.content.Context.MODE_PRIVATE;
import static com.example.mydocsapp.MainPassportPatternActivity.SELECT_PAGE1_PHOTO;
import static com.example.mydocsapp.MainPassportPatternActivity.SELECT_PAGE2_PHOTO;
import static com.example.mydocsapp.MainPassportPatternActivity.SELECT_USER_PHOTO;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.apputils.ImageSaveService;
import com.example.mydocsapp.databinding.FragmentPassportSecondBinding;
import com.example.mydocsapp.interfaces.FragmentSaveViewModel;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
            Intent intent = new Intent(getActivity(), ImageActivity.class);
            if(model.getState().getValue().PhotoPage1!=null) {
                intent.putExtra("text", ((MainPassportPatternActivity) getActivity()).getCurrentItem().Title);
                String fileName = model.getState().getValue().PhotoPage1;
                intent.putExtra("imageFile", fileName);
                getActivity().startActivity(intent);
            }
        });
        binding.secondPassportPhoto.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(), ImageActivity.class);
            if(model.getState().getValue().PhotoPage2!=null) {
                intent.putExtra("text", ((MainPassportPatternActivity) getActivity()).getCurrentItem().Title);
                String fileName = model.getState().getValue().PhotoPage2;
                intent.putExtra("imageFile", fileName);
                getActivity().startActivity(intent);
            }
        });
        binding.loadPage1PhotoBtn.setOnClickListener(v->{
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = CropImage.activity()
                        .getIntent(getContext());
                startActivityForResult(intent, MainPassportPatternActivity.SELECT_PAGE1_PHOTO);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        });
        binding.loadPage2PhotoBtn.setOnClickListener(v->{
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = CropImage.activity()
                        .getIntent(getContext());
                startActivityForResult(intent, MainPassportPatternActivity.SELECT_PAGE2_PHOTO);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
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

        if(((MainPassportPatternActivity)getActivity()).getCurrentItem()!=null)
            binding.usePhotoOption.setChecked(((MainPassportPatternActivity)getActivity()).getCurrentItem().Image!=null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        if (resultCode == MainPassportPatternActivity.RESULT_OK) {
            if (data != null) {
                // Get the URI of the selected file
                final Uri uri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                    switch (requestCode){
                        case SELECT_PAGE1_PHOTO:
                            loadPassportPage1(decoded);
                            break;
                        case SELECT_PAGE2_PHOTO:
                            loadPassportPage2(decoded);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private void loadPassportPage1(Bitmap bitmap) {
        pagePhoto1 = bitmap;
        bitmap = ImageSaveService.scaleDown(bitmap, ImageSaveService.dpToPx(getContext(), binding.firstPassportPhoto.getWidth()), true);
        binding.firstPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
        binding.firstPassportPhoto.setImageBitmap(bitmap);
    }
    private void loadPassportPage2(Bitmap bitmap) {
        pagePhoto2 = bitmap;
        bitmap = ImageSaveService.scaleDown(bitmap, ImageSaveService.dpToPx(getContext(), binding.secondPassportPhoto.getWidth()), true);
        binding.secondPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
        binding.secondPassportPhoto.setImageBitmap(bitmap);
    }
}