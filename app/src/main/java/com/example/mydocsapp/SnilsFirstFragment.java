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
import com.example.mydocsapp.databinding.FragmentSnilsFirstBinding;
import com.example.mydocsapp.interfaces.Changedable;
import com.example.mydocsapp.interfaces.IFragmentDataSaver;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Snils;
import com.example.mydocsapp.models.SnilsStateViewModel;
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

public class SnilsFirstFragment extends Fragment implements IFragmentDataSaver {
    private static final int DB_IMAGE = 1;
    private static final int BITMAP_IMAGE = 0;
    FragmentSnilsFirstBinding binding;
    SnilsStateViewModel model;
    Bitmap snilsPhoto = null;
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
        String dateBirth = binding.editTextDateBirth.getText().toString();
        String dateRegistration = binding.editTextDateRegistration.getText().toString();
        boolean flag = true;
        if (!ValidationService.isValidDateFormat(dateBirth,"dd-MM-yyyy") & !dateBirth.isEmpty()){
            binding.editTextDateBirth.setError(getString(R.string.invalid_date));
            binding.copy4.setVisibility(View.INVISIBLE);
            flag =  false;
        }
        if (!ValidationService.isValidDateFormat(dateRegistration,"dd-MM-yyyy") & !dateBirth.isEmpty()){
            binding.editTextDateRegistration  .setError(getString(R.string.invalid_date));
            binding.copy5.setVisibility(View.INVISIBLE);
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
        binding = FragmentSnilsFirstBinding.inflate(getLayoutInflater());
        model = new ViewModelProvider(requireActivity()).get(SnilsStateViewModel.class);
        registerForARFacePhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                CropImage.ActivityResult cropImageResult = CropImage.getActivityResult(result.getData());
                if (result.getData() != null) {
                    final Uri uri = cropImageResult.getUri();
                    try {
                        snilsPhoto = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        snilsPhoto.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                        binding.snilsPhoto.setBackgroundColor(Color.TRANSPARENT);
                        binding.snilsPhoto.setImageBitmap(decoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        loadData();

        binding.loadPhotoBtn.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = CropImage.activity()
                        .getIntent(getContext());
                registerForARFacePhoto.launch(intent);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        });
        binding.snilsPhoto.setOnClickListener(v -> {
            if (model.getState().getValue().PhotoPage1 == null)
                return;
            Intent intent = new Intent(getActivity(), ImageActivity.class);
            Item item = ((MainDocumentPatternActivity) getActivity()).getCurrentItem();
            intent.putExtra("text", item.Title);
            String fileName = model.getState().getValue().PhotoPage1;
            intent.putExtra("item", item);
            if (snilsPhoto != null) {
                fileName = ImageService.createImageFromBitmap(snilsPhoto, getContext());
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
        binding.editTextNumber.addTextChangedListener(textWatcher);
        binding.editTextFullName.addTextChangedListener(textWatcher);
        binding.editTextDateBirth.addTextChangedListener(textWatcher);
        binding.editTextPlaceBirth.addTextChangedListener(textWatcher);
        binding.editTextDateRegistration.addTextChangedListener(textWatcher);
        return binding.getRoot();
    }
    private void loadData() {
        Snils Snils = model.getState().getValue();
        binding.editTextNumber.setText(Snils.Number);
        binding.editTextFullName.setText(Snils.FIO);
        binding.editTextDateBirth.setText(Snils.BirthDate);
        if (Snils.Gender.equals("M"))
            binding.maleCheck.setChecked(true);
        else
            binding.femaleCheck.setChecked(true);
        binding.editTextPlaceBirth.setText(Snils.BirthPlace);
        binding.editTextDateRegistration.setText(Snils.RegistrationDate);
        if (Snils.PhotoPage1 != null) {
            if (!Snils.PhotoPage1.isEmpty()) {
                binding.snilsPhoto.setTag(1);
                binding.snilsPhoto.setBackgroundColor(Color.TRANSPARENT);
                File outputFile = new File(Snils.PhotoPage1 + "_copy");
                File encFile = new File(Snils.PhotoPage1);
                try {
                    MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                    binding.snilsPhoto.setImageURI(Uri.fromFile(outputFile));

                    outputFile.delete();
                }  catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void SaveData() {
        Snils Snils = model.getState().getValue();
        Snils.Number = binding.editTextNumber.getText().toString();
        Snils.FIO = binding.editTextFullName.getText().toString();
        Snils.BirthDate = binding.editTextDateBirth.getText().toString();
        Snils.RegistrationDate = binding.editTextDateRegistration.getText().toString();
        if (binding.maleCheck.isChecked())
            Snils.Gender = "M";
        else
            Snils.Gender = "F";
        Snils.BirthPlace = binding.editTextPlaceBirth.getText().toString();
        model.setState(Snils);
    }

    @Override
    public List<Bitmap> getPhotos() {
        List<Bitmap> bitmaps = new ArrayList<>();
        bitmaps.add(snilsPhoto);
        return bitmaps;
    }
}