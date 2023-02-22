package com.example.mydocsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;

import com.example.mydocsapp.apputils.ImageSaveService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.databinding.ActivityCardPatternBinding;
import com.example.mydocsapp.databinding.FragmentPassportFirstBinding;
import com.example.mydocsapp.models.CreditCard;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.NoSuchPaddingException;

public class CardPatternActivity extends AppCompatActivity {

    private static final int BITMAP_IMAGE = 0;
    private static final int DB_IMAGE = 1;
    DBHelper db;
    private Item CurrentItem;
    private ActivityResultLauncher<Intent> registerForARImage;
    ActivityCardPatternBinding binding;
    private Bitmap profilePhoto;
    private CreditCard Card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCardPatternBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        db = new DBHelper(this, AppService.getUserId());
        getExtraData(getIntent());
        LoadData();
        setOnClickEvents();
        registerForARImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Log.d("RequestCode",result.getResultCode()+"");
            if (result.getResultCode() == RESULT_OK) {
                CropImage.ActivityResult cropImageResult = CropImage.getActivityResult(result.getData());
                if (result.getData() != null) {
                    // Get the URI of the selected file
                    final Uri uri = cropImageResult.getUri();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                        loadProfileImage(decoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void loadProfileImage(Bitmap bitmap) {
        profilePhoto = bitmap;
        bitmap = ImageSaveService.scaleDown(bitmap, ImageSaveService.dpToPx(this, binding.cardPhoto.getWidth()), true);
        binding.cardPhoto.setBackgroundColor(Color.TRANSPARENT);
        binding.cardPhoto.setImageBitmap(bitmap);
    }
    private void setOnClickEvents() {
        binding.loadPage1PhotoBtn.setOnClickListener(v->{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = CropImage.activity()
                        .getIntent(this);
                registerForARImage.launch(intent);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        });
        binding.cardPhoto.setOnClickListener(v -> {
            if (Card.PhotoPage1 == null)
                return;
            Intent intent = new Intent(this, ImageActivity.class);
            Item item = CurrentItem;
            intent.putExtra("text", item.Title);
            String fileName = Card.PhotoPage1;
            intent.putExtra("item", item);
            if (profilePhoto != null) {
                fileName = ImageSaveService.createImageFromBitmap(profilePhoto, this);
                intent.putExtra("type", BITMAP_IMAGE);
            } else
                intent.putExtra("type", DB_IMAGE);
            intent.putExtra("imageFile", fileName);
            startActivity(intent);
        });
    }

    private void LoadData() {
        Item item = CurrentItem;
        if(item!= null) {
            Cursor cur = db.getCreditCardById(item.Id);
            cur.moveToFirst();
            Card = new CreditCard(cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4),
                    cur.getString(5));
        }
        else
            Card = new CreditCard(
                    0,
                    "",
                    "",
                    "",
                    0,
                    null
            );

        binding.editTextCardNumber.setText(Card.Number);
        binding.editTextCVV.setText(String.valueOf(Card.CVV));
        binding.editTextFullName.setText(Card.FIO);
        binding.editTextValidThru.setText(Card.ExpiryDate);
        if (Card.PhotoPage1 != null) {
            if (!Card.PhotoPage1.isEmpty()) {
                binding.cardPhoto.setBackgroundColor(Color.TRANSPARENT);
                File outputFile = new File(Card.PhotoPage1 + "_copy");
                File encFile = new File(Card.PhotoPage1);
                try {
                    MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                    binding.cardPhoto.setImageURI(Uri.fromFile(outputFile));

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
    private void getExtraData(Intent intent) {
        CurrentItem= intent.getParcelableExtra("item");
    }

    public void goBackMainPageClick(View view) {
        Log.d("CardCard",binding.editTextCardNumber.getText().toString());
        SaveData();
        String itemImagePath = null;

        Item item;
        if(CurrentItem == null){
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.US);
            String time = df.format(new Date());
            item = new Item(0, "Кредитная карта" + db.selectLastItemId(), "Карта", null, 0,0,0, time, 0, 0);
            db.insertItem(item);
            int ItemId = db.selectLastItemId();
            this.Card.Id = ItemId;

            db.insertCreditCard(this.Card);

            SavePhotos(ItemId);
            itemImagePath = this.Card.PhotoPage1;
            db.updateCreditCard(ItemId,this.Card);
            item.Image = itemImagePath;
            db.updateItem(ItemId,item);
        }
        else{
            SavePhotos(CurrentItem.Id);
            itemImagePath = this.Card.PhotoPage1;
            CurrentItem.Image = itemImagePath;
            db.updateCreditCard(CurrentItem.Id, this.Card);
            db.updateItem(CurrentItem.Id,CurrentItem);
        }
        onBackPressed();
    }

    private void SavePhotos(int ItemId) {
        if (profilePhoto != null) {
            File filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String imgPath = filepath.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/Item" + ItemId + "/";
            File dir = new File(imgPath);
            if (!dir.exists())
                dir.mkdirs();
            String imgName = "CreditCardImage" + ItemId + System.currentTimeMillis();
            File imgFile = new File(dir, imgName);
            if (Card.PhotoPage1 != null) {
                File filePath = new File(Card.PhotoPage1);
                filePath.delete();
                imgFile = new File(Card.PhotoPage1);
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            profilePhoto.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            InputStream is = new ByteArrayInputStream(stream.toByteArray());
            try {
                MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
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
            Card.PhotoPage1 = imgFile.getAbsolutePath();
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }
    private void SaveData() {
        Card.Number = binding.editTextCardNumber.getText().toString();
        Card.FIO = binding.editTextFullName.getText().toString();
        Card.ExpiryDate = binding.editTextValidThru.getText().toString();
        Card.CVV = Integer.parseInt(binding.editTextCVV.getText().toString());
    }
}