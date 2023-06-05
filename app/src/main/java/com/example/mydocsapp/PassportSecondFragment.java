package com.example.mydocsapp;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.databinding.FragmentPassportSecondBinding;
import com.example.mydocsapp.interfaces.IFragmentDataSaver;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;
import com.example.mydocsapp.services.AppService;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PassportSecondFragment extends Fragment implements IFragmentDataSaver {

    private static final int BITMAP_IMAGE = 0;
    private static final int DB_IMAGE = 1;
    FragmentPassportSecondBinding binding;
    PassportStateViewModel model;
    private Bitmap pagePhoto1;
    private Bitmap pagePhoto2;
    private ActivityResultLauncher<Intent> registerForARPage1;
    private ActivityResultLauncher<Intent> registerForARPage2;
    public PassportSecondFragment() {   }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void copyTextClick(View view){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassportSecondBinding.inflate(inflater);
        binding.firstPassportPhoto.setOnClickListener(view -> {
            Passport passport = model.getState().getValue();
            passport.PhotoPage1 = null;
            model.setState(passport);
        });
        model = new ViewModelProvider(requireActivity()).get(PassportStateViewModel.class);
        loadData();
        Item item = ((MainPassportPatternActivity) getActivity()).getCurrentItem();
        registerForARPage1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                CropImage.ActivityResult cropImageResult = CropImage.getActivityResult(result.getData());
                if (result.getData() != null) {
                    final Uri uri = cropImageResult.getUri();
                    try {
                        pagePhoto1 = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        pagePhoto1.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                        decoded = ImageService.scaleDown(decoded, ImageService.dpToPx(getContext(), binding.firstPassportPhoto.getWidth()), true);
                        binding.firstPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                        binding.firstPassportPhoto.setImageBitmap(decoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        registerForARPage2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                CropImage.ActivityResult cropImageResult = CropImage.getActivityResult(result.getData());
                if (result.getData() != null) {
                    final Uri uri = cropImageResult.getUri();
                    try {
                        pagePhoto2 = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        pagePhoto2.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                        //decoded = ImageService.scaleDown(decoded, ImageService.dpToPx(getContext(), binding.firstPassportPhoto.getWidth()), true);
                        binding.secondPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                        binding.secondPassportPhoto.setImageBitmap(decoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        binding.firstPassportPhoto.setOnClickListener(v -> {
            if (model.getState().getValue().PhotoPage1 == null)
                return;
            Intent intent = new Intent(getActivity(), ImageActivity.class);
            intent.putExtra("text", item.Title);
            intent.putExtra("item", item);
            String fileName = model.getState().getValue().PhotoPage1;
            if (pagePhoto1 != null) {
                fileName = ImageService.createImageFromBitmap(pagePhoto1, getContext());
                intent.putExtra("type", BITMAP_IMAGE);
            } else
                intent.putExtra("type", DB_IMAGE);
            intent.putExtra("imageFile", fileName);
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        });
        binding.secondPassportPhoto.setOnClickListener(v -> {
            if (model.getState().getValue().PhotoPage2 == null)
                return;
            Intent intent = new Intent(getActivity(), ImageActivity.class);
            intent.putExtra("text", item.Title);
            intent.putExtra("item", item);
            String fileName = model.getState().getValue().PhotoPage2;
            if (pagePhoto2 != null) {
                fileName = ImageService.createImageFromBitmap(pagePhoto2, getContext());
                intent.putExtra("type", BITMAP_IMAGE);
            } else
                intent.putExtra("type", DB_IMAGE);
            intent.putExtra("imageFile", fileName);
            getActivity().startActivity(intent);
        });
        binding.loadPage1PhotoBtn.setOnClickListener(v -> {
            Log.e("dedPerded", "clickecd");
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = CropImage.activity()
                        .getIntent(getContext());
                registerForARPage1.launch(intent);
                Log.e("dedPerded", "launched");
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        });
        binding.loadPage2PhotoBtn.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = CropImage.activity()
                        .getIntent(getContext());
                registerForARPage2.launch(intent);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        });
        return binding.getRoot();
    }
    private void loadData() {
        Passport passport = model.getState().getValue();
        File outputFile;
        File encFile;
        if (passport.PhotoPage1 != null) {
            if (passport.PhotoPage1.length() != 0) {
                binding.firstPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                outputFile = new File(passport.PhotoPage1 + "_copy");
                encFile = new File(passport.PhotoPage1);
                try {
                    MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                    binding.firstPassportPhoto.setImageURI(Uri.fromFile(outputFile));
                    outputFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (passport.PhotoPage2 != null) {
            if (passport.PhotoPage2.length() != 0) {
                binding.secondPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                outputFile = new File(passport.PhotoPage2 + "_copy");
                encFile = new File(passport.PhotoPage2);
                try {
                    MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                    binding.secondPassportPhoto.setImageURI(Uri.fromFile(outputFile));

                    outputFile.delete();
                }catch (IOException e) {
                    e.printStackTrace();
                }}}}

    @Override
    public void SaveData() {
    }
    @Override
    public List<Bitmap> getPhotos() {
        List<Bitmap> bitmaps = new ArrayList<>();
        bitmaps.add(pagePhoto1);
        bitmaps.add(pagePhoto2);
        return bitmaps;
    }
}