package com.example.mydocsapp;

import static com.example.mydocsapp.services.AppService.NULL_UUID;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.apputils.PDFMaker;
import com.example.mydocsapp.interfaces.Changedable;
import com.example.mydocsapp.interfaces.IFragmentDataSaver;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;
import com.example.mydocsapp.models.Template;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainPassportPatternActivity extends AppCompatActivity implements Changedable {
    DBHelper db;
    private com.example.mydocsapp.models.Passport Passport;
    PassportStateViewModel model;
    private IFragmentDataSaver listenerForF1;
    private IFragmentDataSaver listenerForF2;
    private Item CurrentItem;
    private Boolean isChanged = false;
    private boolean isCreateMode = false;
    private ViewPager2 viewPager2;

    public Item getCurrentItem() {
        return CurrentItem;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_passport_pattern);
        getExtraData(getIntent());
        db = new DBHelper(this, AppService.getUserId(this));
        setDataFromDb();

        model = new ViewModelProvider(this).get(PassportStateViewModel.class);
        model.getState().observe(this, item -> {
            this.Passport = item;
        });
        model.setState(this.Passport);
        if (CurrentItem == null) isCreateMode = true;
        setWindowData();
        setOnClickListeners();
        setTopPatternPanelData();
    }
    private void setWindowData() {
        listenerForF1 = new PassportFirstFragment();
        listenerForF2 = new PassportSecondFragment();
        viewPager2 = findViewById(R.id.frame_container);
        viewPager2.setAdapter(new MyFragmentAdapter(2,getSupportFragmentManager(), getLifecycle(), listenerForF1,listenerForF2));
        viewPager2.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback()
                {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        switch (position) {
                            case 0:
                                TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.active_page_image1).getBackground();
                                transition.startTransition(200);
                                TransitionDrawable transition2 = (TransitionDrawable) findViewById(R.id.active_page_image2).getBackground();
                                transition2.startTransition(200);
                                return;
                            case 1:
                                TransitionDrawable transition_1 = (TransitionDrawable) findViewById(R.id.active_page_image1).getBackground();
                                transition_1.reverseTransition(200);
                                TransitionDrawable transition_2 = (TransitionDrawable) findViewById(R.id.active_page_image2).getBackground();
                                transition_2.reverseTransition(200);
                                return;
                        }
                    }
                });
        if(isCreateMode) {
            findViewById(R.id.arrow_image_view).setVisibility(View.VISIBLE);
            (findViewById(R.id.passport_txt)).setOnClickListener(v -> {
                boolean btnFlag = v.getTag().equals("off");
                ImageView arrowImageView = findViewById(R.id.arrow_image_view);
                ObjectAnimator animator = btnFlag ? ObjectAnimator.ofFloat(arrowImageView, View.ROTATION_X, 0f, 180f) : ObjectAnimator.ofFloat(arrowImageView, View.ROTATION_X, 180f, 0f);
                animator.setDuration(600);
                animator.start();
                MotionLayout ml = findViewById(R.id.motion_layout);
                ml.setTransition(R.id.transTop);
                if (btnFlag) {
                    ml.transitionToEnd();
                    v.setTag("on");
                } else {
                    ml.transitionToStart();
                    v.setTag("off");
                }
            });
            findViewById(R.id.right_menu_delete_btn).setVisibility(View.GONE);
            findViewById(R.id.right_menu_save_as_btn).setVisibility(View.GONE);
        }
    }
    private void setOnClickListeners() {
        findViewById(R.id.menubar_options).setOnClickListener(this::menuOptionsBtnClick);
        findViewById(R.id.confirm_button).setOnClickListener(v->{
            savePassportMethod();
            onBackPressed();
        });
        findViewById(R.id.menubar_Back).setOnClickListener(this::goBackMainPageClick);
        findViewById(R.id.INN_top_btn).setOnClickListener(this::goInnClick);
        findViewById(R.id.policy_top_btn).setOnClickListener(this::goPolicyClick);
        findViewById(R.id.right_menu_save_btn).setOnClickListener(v->{
            savePassportMethod();
            onBackPressed();
        });
        findViewById(R.id.right_menu_save_as_btn).setOnClickListener(this::saveAsBtnClick);
        findViewById(R.id.right_menu_delete_btn).setOnClickListener(this::deleteBtnClick);
    }
    private void deleteBtnClick(View v) {
        db.deleteItem(CurrentItem.Id);
        onBackPressed();
    }
    private void setTopPatternPanelData() {
        LinearLayout patternContainer = findViewById(R.id.pattern_container_layout);
        ArrayList<Template> templates = (ArrayList<Template>) db.getTemplateByUserId(AppService.getUserId(this));
        ArrayList<Template> downloadedTemplates = (ArrayList<Template>) db.getTemplateDownload(AppService.getUserId(this));
        if(templates.size()>0)
            patternContainer.addView(makePatternTopTextView(getString(R.string.my_templates)+":"));
        for (Template template:
             templates) {
            patternContainer.addView(makePatternTopButton(template));
        }
        patternContainer.addView(makePatternTopTextView(getString(R.string.downloaded)));
        for (Template template:
                downloadedTemplates) {
            patternContainer.addView(makePatternTopButton(template));
        }
    }
    private View makePatternTopButton(Template template) {
        Button button = (Button) getLayoutInflater().inflate(R.layout.pattern_button_layout, null);
        button.setText(template.Name);
        if(template.Status.equals("Downloaded"))
            button.setCompoundDrawables(getDrawable(R.drawable.ic_downloaded_template),null,null,null);
        button.setTag(template);
        button.setOnClickListener(v->{
            Template temp = (Template) v.getTag();
            Intent intent = new Intent(MainPassportPatternActivity.this, TemplateActivity.class);
            intent.putExtra("template", temp);
            if(temp!=null) {
                startActivity(intent);
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
            }
        });
        return button;
    }
    private View makePatternTopTextView(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.yellow));
        textView.setText(text);
        textView.setTextSize(18);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }
    private void getExtraData(Intent intent) {
        CurrentItem = intent.getParcelableExtra("item");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }
    private void setDataFromDb() {
        Item item = CurrentItem;
        if (item != null) {
            this.Passport = db.getPassportById(item.Id);
        } else
            this.Passport = new Passport(NULL_UUID,"","","","","","","M","","",null,null,null,null);
    }
    public void copyTextClick(View view) {
        listenerForF1.copyTextClick(view);
    }

    public void goBackMainPageClick(View view) {
        if (!getIsChanged()) {
            onBackPressed();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_changes);
        builder.setMessage(R.string.do_you_want_save);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            savePassportMethod();
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.exit, (dialog, which) -> {
            onBackPressed();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setTextColor(Color.parseColor("#FFC700"));
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTextColor(Color.WHITE);
    }
    private void saveAsBtnClick(View v) {
        savePassportMethod();
        isChanged = false;
        if(PDFMaker.createPassportPDF( Passport,AppService.getUserId(this)))
            Toast.makeText(this,R.string.pdf_document_created,Toast.LENGTH_SHORT).show();
    }
    public void menuOptionsBtnClick(View v) {
        if (v.getTag().equals("off")) {
            MotionLayout ml = findViewById(R.id.motion_layout);
            ml.setTransition(R.id.transRightMenu);
            ml.transitionToEnd();
            v.setTag("on");
        } else if (v.getTag().equals("on")) {
            MotionLayout ml = findViewById(R.id.motion_layout);
            ml.setTransition(R.id.transRightMenu);
            ml.transitionToStart();
            v.setTag("off");
        }
    }
    private void savePassportMethod() {
        if (!listenerForF1.IsValidData())
            return;
        listenerForF1.SaveData();
        Item item;
        if (isCreateMode) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
            String time = df.format(new Date());
            item = new Item(NULL_UUID, getString(R.string.passport) + db.selectLastItemId(), "Passport", null, 0, 0, 0, time, NULL_UUID, 0, "");

            UUID ItemId = db.insertItem(item, true);
            this.Passport.Id = ItemId;
            db.insertPassport(this.Passport);
            List<Bitmap> bitmaps = listenerForF1.getPhotos();
            Passport.FacePhoto = savePhoto(bitmaps.get(0),ItemId,null,"FacePhoto");
            bitmaps = listenerForF2.getPhotos();
            Passport.PhotoPage1 = savePhoto(bitmaps.get(0),ItemId,null,"PhotoPage1");
            Passport.PhotoPage2 = savePhoto(bitmaps.get(1),ItemId,null,"PhotoPage2");
            db.updatePassport(ItemId, this.Passport, false);
            item.Image = this.Passport.FacePhoto;
            db.updateItem(ItemId, item,false);
        } else {
            List<Bitmap> bitmaps = listenerForF1.getPhotos();
            Passport.FacePhoto = savePhoto(bitmaps.get(0),CurrentItem.Id,Passport.FacePhoto,"FacePhoto");
            bitmaps = listenerForF2.getPhotos();
            Passport.PhotoPage1 = savePhoto(bitmaps.get(0),CurrentItem.Id,Passport.PhotoPage1,"PhotoPage1");
            Passport.PhotoPage2 = savePhoto(bitmaps.get(1),CurrentItem.Id,Passport.PhotoPage2,"PhotoPage2");
            CurrentItem.Image = this.Passport.FacePhoto;
            db.updatePassport(CurrentItem.Id, this.Passport, false);
            db.updateItem(CurrentItem.Id, CurrentItem,false);
        }
    }

    private String savePhoto(Bitmap photo, UUID ItemId, String passportField, String name) {
        if (photo != null) {
            File rootDir = getApplicationContext().getFilesDir();
            String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME +"/"+ AppService.getUserId(this)+ "/Item" + ItemId.toString() + "/Passport/";
            File dir = new File(imgPath);
            if (!dir.exists())
                dir.mkdirs();
            String imgName = name;
            File imgFile = new File(dir, imgName);
            if (passportField != null) {
                File filePath = new File(passportField);
                filePath.delete();
                imgFile = new File(passportField);
            }
            try {
                imgFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            InputStream is = new ByteArrayInputStream(stream.toByteArray());
            try {
                MyEncrypter.encryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), is, new FileOutputStream(imgFile));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return imgFile.getAbsolutePath();
        }
        return passportField;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    public void goInnClick(View view) {
        startActivity(new Intent(MainPassportPatternActivity.this, INNPatternActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }
    public void goPolicyClick(View view) {
        startActivity(new Intent(MainPassportPatternActivity.this, PolicyPatternActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }
    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }
    @Override
    public void setIsChanged(Boolean value) {
        isChanged = value;
    }
    @Override
    public Boolean getIsChanged() {
        return isChanged;
    }
    public static class MyFragmentAdapter extends FragmentStateAdapter {
        private static int NUM_ITEMS = 2;
        IFragmentDataSaver f1;
        IFragmentDataSaver f2;
        public MyFragmentAdapter(int count, @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, IFragmentDataSaver _f1, IFragmentDataSaver _f2) {
            super(fragmentManager, lifecycle);
            this.NUM_ITEMS = count;
            this.f1 = _f1;
            this.f2 = _f2;
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return (Fragment) f1;
                case 1:
                    return (Fragment) f2;
                default:
                    return null;
            }
        }
        @Override
        public int getItemCount() {
            return NUM_ITEMS;
        }
    }
}