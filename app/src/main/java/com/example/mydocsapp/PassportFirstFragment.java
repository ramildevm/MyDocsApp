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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.databinding.FragmentPassportFirstBinding;
import com.example.mydocsapp.interfaces.IFragmentDataSaver;
import com.example.mydocsapp.interfaces.Changedable;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.ValidationService;
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
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;


public class PassportFirstFragment extends Fragment implements IFragmentDataSaver {
    private static final int DB_IMAGE = 1;
    private static final int BITMAP_IMAGE = 0;
    FragmentPassportFirstBinding binding;
    PassportStateViewModel model;
    Bitmap profilePhoto = null;
    private ActivityResultLauncher<Intent> registerForARFacePhoto;

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
        String dateIssue = binding.editTextDateIssue.getText().toString();
        String dateBirth = binding.editTextDateBirth.getText().toString();
        boolean flag = true;
        if (!ValidationService.isValidDateFormat(dateBirth,"dd-MM-yyyy") & !dateBirth.isEmpty()){
            binding.editTextDateBirth.setError(getString(R.string.invalid_date));
            flag =  false;
        }
        if (!ValidationService.isValidDateFormat(dateIssue,"dd-MM-yyyy") & !dateIssue.isEmpty()){
            binding.editTextDateIssue.setError(getString(R.string.invalid_date));
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
        binding = FragmentPassportFirstBinding.inflate(getLayoutInflater());
        model = new ViewModelProvider(requireActivity()).get(PassportStateViewModel.class);
        registerForARFacePhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                CropImage.ActivityResult cropImageResult = CropImage.getActivityResult(result.getData());
                if (result.getData() != null) {
                    final Uri uri = cropImageResult.getUri();
                    try {
                        profilePhoto = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        profilePhoto.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                        binding.userPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                        binding.userPassportPhoto.setImageBitmap(decoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        loadData();

        binding.loadProfilePhotoBtn.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = CropImage.activity()
                        .getIntent(getContext());
                registerForARFacePhoto.launch(intent);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        });
        binding.userPassportPhoto.setOnClickListener(v -> {
            if (model.getState().getValue().FacePhoto == null)
                return;
            Intent intent = new Intent(getActivity(), ImageActivity.class);
            Item item = ((MainPassportPatternActivity) getActivity()).getCurrentItem();
            intent.putExtra("text", item.Title);
            String fileName = model.getState().getValue().FacePhoto;
            intent.putExtra("item", item);
            if (profilePhoto != null) {
                fileName = ImageService.createImageFromBitmap(profilePhoto, getContext());
                intent.putExtra("type", BITMAP_IMAGE);
            } else
                intent.putExtra("type", DB_IMAGE);
            intent.putExtra("imageFile", fileName);
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        });
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
        binding.editTextSeriesNumber.addTextChangedListener(textWatcher);
        binding.editTextFullName.addTextChangedListener(textWatcher);
        binding.editTextDivisionCode.addTextChangedListener(textWatcher);
        binding.editTextDateIssue.addTextChangedListener(textWatcher);
        binding.editTextDateBirth.addTextChangedListener(textWatcher);
        binding.editTextIssuedWhom.addTextChangedListener(textWatcher);
        binding.editTextPlaceBirth.addTextChangedListener(textWatcher);
        binding.editTextPlaceResidence.addTextChangedListener(textWatcher);
        return binding.getRoot();
    }
    private void loadData() {
        Passport passport = model.getState().getValue();
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
        if (passport.FacePhoto != null) {
            if (!passport.FacePhoto.isEmpty()) {
                binding.userPassportPhoto.setTag(1);
                binding.userPassportPhoto.setBackgroundColor(Color.TRANSPARENT);
                File outputFile = new File(passport.FacePhoto + "_copy");
                File encFile = new File(passport.FacePhoto);
                try {
                    MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                    binding.userPassportPhoto.setImageURI(Uri.fromFile(outputFile));

                    outputFile.delete();
                }  catch (IOException e) {
                    e.printStackTrace();
                }
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
    public List<Bitmap> getPhotos() {
        List<Bitmap> bitmaps = new ArrayList<>();
        bitmaps.add(profilePhoto);
        return bitmaps;
    }
}