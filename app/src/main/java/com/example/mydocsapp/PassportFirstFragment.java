package com.example.mydocsapp;

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


public class PassportFirstFragment extends Fragment implements IFragmentDataSaver {

    private static final int DB_IMAGE = 1;
    private static final int BITMAP_IMAGE = 0;
    FragmentPassportFirstBinding binding;
    PassportStateViewModel model;
    Bitmap profilePhoto = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }
    public void copyTextClick(View view){
        ViewParent parent = view.getParent();
        if (parent instanceof ConstraintLayout) {
            ConstraintLayout constraintLayout = (ConstraintLayout) parent;
            // Get the ConstraintSet object for the layout
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            // Get the IDs of the views that are referenced by the constraint for textView1's end edge
            int referencedId = constraintSet.getConstraint(view.getId()).layout.endToEnd;
            // Get the view that is referenced by the constraint
            EditText linkedView = binding.getRoot().findViewById(referencedId);
            copyToClipboard(linkedView);
        }
    }
    public void copyToClipboard(EditText editText) {
        // Get the text from the EditText
        String text = editText.getText().toString().trim();
        // Get the system clipboard manager
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
        // Create a ClipData object to hold the text to be copied
        ClipData clip = ClipData.newPlainText("text", text);
        // Copy the text to the clipboard
        clipboard.setPrimaryClip(clip);
        // Show a Toast message to indicate that the text has been copied
        Toast.makeText(getContext(), R.string.text_copied, Toast.LENGTH_SHORT).show();
    }
    private void loadProfileImage(Bitmap bitmap) {
        profilePhoto = bitmap;
        bitmap = ImageService.scaleDown(bitmap, ImageService.dpToPx(getContext(), binding.userPassportPhoto.getWidth()), true);
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
    public void SavePhotos(int ItemId) {
        Passport passport = model.getState().getValue();
        if (profilePhoto != null) {
            //File filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File rootDir = getContext().getApplicationContext().getFilesDir();
            String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME +"/"+ AppService.getUserId(getContext())+ "/Item" + ItemId + "/";
            File dir = new File(imgPath);
            if (!dir.exists())
                dir.mkdirs();
            String imgName = "PassportProfileImage" + ItemId + System.currentTimeMillis();
            File imgFile = new File(dir, imgName);
            if (passport.FacePhoto != null) {
                File filePath = new File(passport.FacePhoto);
                filePath.delete();
                imgFile = new File(passport.FacePhoto);

            }
            try {
                imgFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("MyDocsAppPassport1",e.getMessage());
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            profilePhoto.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            InputStream is = new ByteArrayInputStream(stream.toByteArray());
            try {
                MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
                Log.d("MyDocsAppPassport2",e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Log.d("MyDocsAppPassport3",e.getMessage());
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
                Log.d("MyDocsAppPassport4",e.getMessage());
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                Log.d("MyDocsAppPassport5",e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("MyDocsAppPassport6",e.getMessage());
            }
            passport.FacePhoto = imgFile.getAbsolutePath();
            Log.d("MyDocsAppPassport",passport.FacePhoto);
        }
        model.setState(passport);
    }
}