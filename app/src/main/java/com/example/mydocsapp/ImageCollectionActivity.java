package com.example.mydocsapp;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mydocsapp.apputils.ImageSaveService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.databinding.ActivityImageCollectionBinding;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Photo;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;
import com.example.mydocsapp.services.ImageAdapter;
import com.example.mydocsapp.services.ImageCollectionService;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.crypto.NoSuchPaddingException;

public class ImageCollectionActivity extends AppCompatActivity {
    private static final int SESSION_MODE_CREATE = 1;
    private static final int SESSION_MODE_OPEN = 2;
    private ActivityResultLauncher<Intent> registerForARImageCollection;
    ImageCollectionService imagesService;
    ImageAdapter imageAdapter;
    ActivityImageCollectionBinding binding;
    private int SessionMode;
    private Item CurrentItem;
    int ItemId;
    DBHelper db;
    private ArrayList<Photo> photoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageCollectionBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getExtraData(getIntent());
        imagesService = new ImageCollectionService();
        photoList = new ArrayList<>();
        db = new DBHelper(this,getIntent().getIntExtra("userId",0));
        registerForARImageCollection = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                // get selected image URIs
                ClipData clipData = result.getData().getClipData();
                if (clipData != null) {
                    // multiple images selected
                    Item item = createItem(clipData.getItemCount());
                    ItemId = db.selectLastItemId();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        try {
                            // convert selected image to bitmap
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            String filePath = createPhoto(bitmap, i);
                            if(i==0)
                                item.Image=filePath;
                            // add bitmap to array
                            imagesService.add(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    db.updateItem(ItemId,item);
                    imageAdapter = new ImageAdapter(imagesService.get());
                    binding.viewPager2.setAdapter(imageAdapter);
                    changeArrowColor();
                }
            } else {
                onBackPressed();
            }
        });
        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                imagesService.setCurrentImage(position);
                changeArrowColor();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        binding.goNextBtn.setOnClickListener(v -> {
            binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1, true);
            changeArrowColor();
        });
        binding.goPreviousBtn.setOnClickListener(v -> {
            binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() - 1, true);
            changeArrowColor();
        });
        createSession();
    }

    private String createPhoto(Bitmap bitmap, int index) {
        File rootDir = getApplicationContext().getFilesDir();
        String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/Item" + ItemId + "/";

        File dir = new File(imgPath);
        if (!dir.exists())
            dir.mkdirs();
        String imgName = "Image" + index + System.currentTimeMillis();
        File imgFile = new File(dir, imgName);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
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
        String filePath = imgFile.getAbsolutePath();
        Photo photo = new Photo(0,filePath,ItemId);
        db.insertPhoto(photo);
        return filePath;
    }

    private Item createItem(int count) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.US);
        String time = df.format(new Date());
        ItemId = db.selectLastItemId();
        String name = (count>1)?"Альбом":"Фото";
        String type = (count>1)?"Альбом":"Изображение";
        Item item =new Item(0, name + ItemId, type, null, 0, 0, 0, time, 0, 0);
        db.insertItem(item);
        return item;
    }

    private void createSession() {
        if(SessionMode==SESSION_MODE_CREATE){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            registerForARImageCollection.launch(intent);
        }
        else if(SessionMode==SESSION_MODE_OPEN){
            Cursor cur = db.getPhotos(CurrentItem.Id);
            Photo photo;
            while (cur.moveToNext()) {
                photo = new Photo(cur.getInt(0),
                        cur.getString(1),
                        cur.getInt(2));

                File outputFile = new File(photo.Path+"_copy");
                File filePath = new File(photo.Path);
                try {
                    MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(filePath), new FileOutputStream(outputFile));
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
                imagesService.add(ImageSaveService.fileToBitmap(outputFile));
                photoList.add(photo);
                outputFile.delete();
            }
            imageAdapter = new ImageAdapter(imagesService.get());
            binding.viewPager2.setAdapter(imageAdapter);
            changeArrowColor();
        }
    }

    private void getExtraData(Intent intent) {
        SessionMode = intent.getIntExtra("mode",0);
        CurrentItem = getIntent().getParcelableExtra("item");
    }

    private void changeArrowColor() {
        if (imagesService.getSize() != 1) {
            if (binding.viewPager2.getCurrentItem() == 0) {
                binding.goPreviousBtn.setImageResource(R.drawable.right_arrow_white);
                binding.goNextBtn.setImageResource(R.drawable.right_arrow_yellow);
            } else if (binding.viewPager2.getCurrentItem() == imagesService.getSize() - 1) {
                binding.goNextBtn.setImageResource(R.drawable.right_arrow_white);
                binding.goPreviousBtn.setImageResource(R.drawable.right_arrow_yellow);
            } else {
                binding.goNextBtn.setImageResource(R.drawable.right_arrow_yellow);
                binding.goPreviousBtn.setImageResource(R.drawable.right_arrow_yellow);
            }
        } else {
            binding.bottomArrows.setAlpha(0);
        }
    }

    public void goBackClick(View view) {
        onBackPressed();
    }

    public void saveCropImage(View view) {
        Bitmap bitmap = binding.imageCropper.getCroppedImage();
        binding.imageCropper.setImageBitmap(bitmap);
        imagesService.set(imagesService.getCurrentImage(), bitmap);
        imageAdapter.onItemChange(imagesService.getCurrentImage(), bitmap);
        goCropBackClick(new View(this));
    }

    public void goCropBackClick(View view) {
        binding.cropLayout.setVisibility(View.INVISIBLE);
        binding.motionLayout.setVisibility(View.VISIBLE);
    }

    public void menuCropBtnClick(View view) {
        binding.cropLayout.setVisibility(View.VISIBLE);
        binding.motionLayout.setVisibility(View.INVISIBLE);
        binding.imageCropper.setImageBitmap(imagesService.get(imagesService.getCurrentImage()));
    }

    public void rotateCropImage(View view) {
        Bitmap originalBitmap = binding.imageCropper.getCroppedImage();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
        binding.imageCropper.setImageBitmap(rotatedBitmap);
    }

    public void menuChangeImageClick(View view) {
    }

    public void menuSaveAsClick(View view) {

    }

    public void menuOptionsBtnClick(View v) {
        if (v.getTag().equals("off")) {
            MotionLayout ml = findViewById(R.id.motion_layout);
            ml.setTransition(R.id.transOptionMenu);
            ml.transitionToEnd();
            v.setTag("on");
        } else if (v.getTag().equals("on")) {
            MotionLayout ml = findViewById(R.id.motion_layout);
            ml.setTransition(R.id.transOptionMenu);
            ml.transitionToStart();
            v.setTag("off");
        }
    }

    public void menuDeleteBtnClick(View view) {

    }
}