package com.example.mydocsapp;

import static com.example.mydocsapp.services.AppService.NULL_UUID;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mydocsapp.apputils.ImageService;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ImageCollectionActivity extends AppCompatActivity {
    private static final int SESSION_MODE_CREATE = 1;
    private static final int SESSION_MODE_OPEN = 2;
    private ActivityResultLauncher<Intent> registerForARImageCollection;
    private ActivityResultLauncher<Intent> registerForARImageChange;
    private ActivityResultLauncher<Intent> registerForARImageAddCollection;
    ImageCollectionService imagesService;
    ImageAdapter imageAdapter;
    ActivityImageCollectionBinding binding;
    private int SessionMode;
    private Item CurrentItem;
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
        registerForARImageChange = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    Bitmap bitmap = ImageService.getBitmapFormUri(this,imageUri);
                    imagesService.set(imagesService.getCurrentImage(), bitmap);
                    imageAdapter.onItemChange(imagesService.getCurrentImage(), bitmap);
                    changePhotoFile(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        registerForARImageCollection = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                // get selected image URIs
                ClipData clipData = result.getData().getClipData();
                if (clipData != null) {
                    // multiple images selected
                    CurrentItem = createItem(clipData.getItemCount());
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        try {
                            // convert selected image to bitmap
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            createPhoto(bitmap);
                            CurrentItem.Image="null_path";
                            // add bitmap to array
                            imagesService.add(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    fillPhotoArray(CurrentItem.Id);
                    db.updateItem(CurrentItem.Id,CurrentItem,false);
                    imageAdapter = new ImageAdapter(imagesService.get());
                    binding.viewPager2.setAdapter(imageAdapter);
                    changeArrowColor();
                }
            } else {
                onBackPressed();
            }
        });
        registerForARImageAddCollection = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                ClipData clipData = result.getData().getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            createPhoto(bitmap);
                            imagesService.add(bitmap);
                            imageAdapter.notifyItemInserted(imagesService.getSize());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    fillPhotoArray(CurrentItem.Id);
                    changeArrowColor();
                }
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
        setOnClickListeners();
        createSession();
    }

    private void setOnClickListeners() {
        binding.goNextBtn.setOnClickListener(v -> {
            if(imagesService.getCurrentImage()==imagesService.getSize()-1){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                registerForARImageAddCollection.launch(intent);
                return;
            }
            binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1, true);
            changeArrowColor();
        });
        binding.goPreviousBtn.setOnClickListener(v -> {
            binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() - 1, true);
            changeArrowColor();
        });
        binding.menubarOptions.setOnClickListener(v->menuOptionsBtnClick(v));
    }

    private void changePhotoFile(Bitmap bitmap) {
        Photo photo = photoList.get(imagesService.getCurrentImage());
        File imgFile = new File(photo.Image);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        InputStream is = new ByteArrayInputStream(stream.toByteArray());
        try {
            MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void fillPhotoArray(UUID itemId) {
        photoList = (ArrayList<Photo>) db.getPhotos(itemId);
    }
    private String createPhoto(Bitmap bitmap) {
        File rootDir = getApplicationContext().getFilesDir();
        String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME +"/"+ AppService.getUserId(this)+ "/Item" + CurrentItem.Id.toString() + "/Image/";
        File dir = new File(imgPath);
        if (!dir.exists())
            dir.mkdirs();
        String imgName = "Image" + CurrentItem.Id.toString();
        File imgFile = new File(dir, imgName);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        InputStream is = new ByteArrayInputStream(stream.toByteArray());
        try {
            MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filePath = imgFile.getAbsolutePath();
        Photo photo = new Photo(NULL_UUID,filePath,CurrentItem.Id,null);
        db.insertPhoto(photo, true);
        return filePath;
    }
    private Item createItem(int count) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        String time = df.format(new Date());
        int lastItemId = db.selectLastItemId();
        String name = (count>1)?getString(R.string.album):getString(R.string.photo);
        String type = "Collection";
        Item item =new Item(NULL_UUID, name + lastItemId, type, null, 0, 0, 0, time, NULL_UUID, 0, "");
        UUID ItemId =  db.insertItem(item, true);
        item.Id = ItemId;
        CurrentItem = item;
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
            fillPhotoArray(CurrentItem.Id);
            for (Photo photo :
                    photoList) {
                File outputFile = new File(photo.Image +"_copy");
                File filePath = new File(photo.Image);
                try {
                    MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(filePath), new FileOutputStream(outputFile));
                }  catch (IOException e) {
                    e.printStackTrace();
                }
                imagesService.add(ImageService.fileToBitmap(outputFile));
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
            binding.goPreviousBtn.setVisibility(View.VISIBLE);
            if (binding.viewPager2.getCurrentItem() == 0) {
                binding.goPreviousBtn.setImageResource(R.drawable.left_arrow_white);
                binding.goNextBtn.setImageResource(R.drawable.left_arrow_yellow);
            } else if (binding.viewPager2.getCurrentItem() == imagesService.getSize() - 1) {
                binding.goNextBtn.setImageResource(R.drawable.ic_round_add_photo_alternate_24);
                binding.goPreviousBtn.setImageResource(R.drawable.left_arrow_yellow);
            } else {
                binding.goNextBtn.setImageResource(R.drawable.left_arrow_yellow);
                binding.goPreviousBtn.setImageResource(R.drawable.left_arrow_yellow);
            }
        } else {
            binding.goPreviousBtn.setVisibility(View.GONE);
            binding.goNextBtn.setImageResource(R.drawable.ic_round_add_photo_alternate_24);
        }
        binding.currentImageTxt.setText(binding.viewPager2.getCurrentItem()+1 + "/" + imagesService.getSize());
    }
    public void goBackClick(View view) {
        onBackPressed();
    }
    @Override
    public void onBackPressed() {
        if(CurrentItem!=null) {
            if (imagesService.getSize() >= 1) {
                CurrentItem.Type = "Collection";
            }
            db.updateItem(CurrentItem.Id,CurrentItem,false);
        }
        super.onBackPressed();
    }
    public void saveCropImage(View view) {
        Bitmap bitmap = binding.imageCropper.getCroppedImage();
        binding.imageCropper.setImageBitmap(bitmap);
        imagesService.set(imagesService.getCurrentImage(), bitmap);
        imageAdapter.onItemChange(imagesService.getCurrentImage(), bitmap);
        changePhotoFile(bitmap);
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
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        registerForARImageChange.launch(intent);
    }
    public void menuSaveAsClick(View view) {
        Photo photo = photoList.get(imagesService.getCurrentImage());
        String fileName = "photo"+ photo.Id.toString()+".png";
        File imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File outputFile = new File(imagesDir, fileName);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        File outputCopyPath = new File(photo.Image +"_copy");
        File filePath = new File(photo.Image);
        try {
            MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(filePath), new FileOutputStream(outputFile));
        }  catch (IOException e) {
            e.printStackTrace();
        }
        if (outputCopyPath.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(outputCopyPath.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                Toast.makeText(this, R.string.image_saved,Toast.LENGTH_SHORT).show();
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
        }
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
        Photo photo = photoList.get(imagesService.getCurrentImage());
        if(imagesService.getSize()==1){
            db.deletePhoto(photo.Id);
            db.deleteItem(photo.CollectionId);
            File file = new File(photo.Image);
            file.delete();
            onBackPressed();
            return;
        }
        if(imagesService.getCurrentImage()==0)
        {
            CurrentItem.Image = photoList.get(1).Image;
            db.updateItem(CurrentItem.Id,CurrentItem,false);
        }
        File file = new File(photo.Image);
        file.delete();
        photoList.remove(imagesService.getCurrentImage());
        db.deletePhoto(photo.Id);
        imageAdapter.onItemDelete(imagesService.getCurrentImage());
        changeArrowColor();
        menuOptionsBtnClick(binding.menubarOptions);
    }
}