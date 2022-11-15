package com.example.mydocsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.apputils.ImageSaveService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.databinding.FragmentPassportFirstBinding;
import com.example.mydocsapp.interfaces.FragmentSaveViewModel;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;


public class PassportFirstFragment extends Fragment implements FragmentSaveViewModel {

    FragmentPassportFirstBinding binding;
    PassportStateViewModel model;
    Bitmap profilePhoto = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    private void loadProfileImage(Bitmap bitmap) {
        profilePhoto = bitmap;
        bitmap = ImageSaveService.scaleDown(bitmap, ImageSaveService.dpToPx(getContext(), binding.userPassportPhoto.getWidth()), true);
        binding.userPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
        binding.userPassportPhoto.setImageBitmap(bitmap);
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
                    loadProfileImage(decoded);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPassportFirstBinding.inflate(getLayoutInflater());
        model = new ViewModelProvider(requireActivity()).get(PassportStateViewModel.class);
        loadData();

        binding.loadProfilePhotoBtn.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = CropImage.activity()
                        .getIntent(getContext());
                startActivityForResult(intent, MainPassportPatternActivity.SELECT_USER_PHOTO);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        });
        binding.userPassportPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ImageActivity.class);
            if(model.getState().getValue().FacePhoto!=null) {
                intent.putExtra("text", ((MainPassportPatternActivity) getActivity()).getCurrentItem().Title);
                String fileName = model.getState().getValue().FacePhoto;
                intent.putExtra("imageFile", fileName);
                getActivity().startActivity(intent);
            }
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
        if (passport.Gender.equals("M"))
            binding.maleCheck.setChecked(true);
        else
            binding.femaleCheck.setChecked(true);
        binding.editTextPlaceBirth.setText(passport.BirthPlace);
        binding.editTextPlaceResidence.setText(passport.ResidencePlace);
        if (passport.FacePhoto!=null) {
            if (!passport.FacePhoto.isEmpty()) {
                binding.userPassportPhoto.setTag(1);
                binding.userPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                File outputFile = new File(passport.FacePhoto+"_copy");
                File encFile = new File(passport.FacePhoto);
                try {
                    MyEncrypter.decryptToFile(((MainPassportPatternActivity)getActivity()).getMy_key(), ((MainPassportPatternActivity)getActivity()).getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                    binding.userPassportPhoto.setImageURI(Uri.fromFile(outputFile));

                    outputFile.delete();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Bitmap image = BitmapFactory.decodeFile(passport.FacePhoto);
                binding.userPassportPhoto.setImageBitmap(image);
            }
        }
    }

    @Override
    public void SaveData() {
        Passport passport = model.getState().getValue();
        passport.SeriaNomer = binding.editTextSeriesNumber.getText().toString();
        passport.DivisionCode = binding.editTextDivisionCode.getText().toString();
        passport.GiveDate = binding.editTextDateIssue.getText().toString();
        passport.ByWhom = binding.editTextIssuedWhom.getText().toString();
        passport.FIO = binding.editTextFullName.getText().toString();
        passport.BirthDate = binding.editTextDateBirth.getText().toString();
        if (binding.maleCheck.isChecked())
            passport.Gender = "M";
        else
            passport.Gender = "F";
        passport.BirthPlace = binding.editTextPlaceBirth.getText().toString();
        passport.ResidencePlace = binding.editTextPlaceResidence.getText().toString();
        model.setState(passport);
    }

    @Override
    public void SavePhotos(int PassportId, int ItemId) {
        Passport passport = model.getState().getValue();
        if (profilePhoto != null) {
            if (passport.FacePhoto!=null) {
                File filePath = new File(passport.FacePhoto);
                filePath.delete();
            }
            File filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String imgPath = filepath.getAbsolutePath() + "/"+ MainContentActivity.APPLICATION_NAME + "/Item" + ItemId + "/";
            File dir = new File(imgPath);
            if(!dir.exists())
                dir.mkdirs();
            String imgName = "PassportProfileImage" + PassportId + System.currentTimeMillis();
            File imgFile = new File(dir, imgName);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            profilePhoto.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            InputStream is = new ByteArrayInputStream(stream.toByteArray());
            try {
                MyEncrypter.encryptToFile(((MainPassportPatternActivity)getActivity()).getMy_key(), ((MainPassportPatternActivity)getActivity()).getMy_spec_key(), is, new FileOutputStream(imgFile));
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            passport.FacePhoto = imgFile.getAbsolutePath();
        }
        model.setState(passport);
    }
}