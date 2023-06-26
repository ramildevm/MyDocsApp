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
import com.example.mydocsapp.interfaces.StateViewModel;
import com.example.mydocsapp.models.Inn;
import com.example.mydocsapp.models.InnStateViewModel;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;
import com.example.mydocsapp.models.Polis;
import com.example.mydocsapp.models.PolisStateViewModel;
import com.example.mydocsapp.models.Snils;
import com.example.mydocsapp.models.SnilsStateViewModel;
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

public class MainDocumentPatternActivity extends AppCompatActivity implements Changedable {
    DBHelper db;
    StateViewModel model;
    private com.example.mydocsapp.models.Passport Passport;
    private com.example.mydocsapp.models.Snils Snils;
    private com.example.mydocsapp.models.Inn Inn;
    private com.example.mydocsapp.models.Polis Polis;

    private IFragmentDataSaver listenerForF1;
    private IFragmentDataSaver listenerForF2;

    private Item CurrentItem;
    private Boolean isChanged = false;
    private boolean isCreateMode = false;
    private ViewPager2 viewPager2;
    String type;
    private boolean isPassport = false;
    private boolean isSnils = false;
    private boolean isInn = false;
    private boolean isPolis = false;

    public Item getCurrentItem() {
        return CurrentItem;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_passport_pattern);
        getExtraData(getIntent());
        db = new DBHelper(this, AppService.getUserId(this));
        if (CurrentItem == null) isCreateMode = true;
        setWindowData();
        setOnClickListeners();
        setTopPatternPanelData();
    }

    private void setModel() {
        if(isPassport) {
            model = new ViewModelProvider(this).get(PassportStateViewModel.class);
            model.getState().observe(this, item -> {
                this.Passport = (Passport) item;
            });
            model.setState(this.Passport);
        }
        if(isSnils){
            model = new ViewModelProvider(this).get(SnilsStateViewModel.class);
            model.getState().observe(this, item -> {
                this.Snils = (com.example.mydocsapp.models.Snils) item;
            });
            model.setState(this.Snils);
        }
        if(isInn){
            model = new ViewModelProvider(this).get(InnStateViewModel.class);
            model.getState().observe(this, item -> {
                this.Inn = (com.example.mydocsapp.models.Inn) item;
            });
            model.setState(this.Inn);
        }
        if(isPolis){
            model = new ViewModelProvider(this).get(PolisStateViewModel.class);
            model.getState().observe(this, item -> {
                this.Polis = (com.example.mydocsapp.models.Polis) item;
            });
            model.setState(this.Polis);
        }
    }

    private void setWindowData() {
        ((TextView)(findViewById(R.id.top_panel_txt))).setText(type);
        if(isCreateMode) {
            findViewById(R.id.arrow_image_view).setVisibility(View.VISIBLE);
            (findViewById(R.id.top_panel_txt)).setOnClickListener(v -> {
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
                MotionLayout ml = findViewById(R.id.motion_layout);
                ml.setTransition(R.id.transTop);
                ml.transitionToEnd();
            findViewById(R.id.top_panel_txt).setTag("on");
            findViewById(R.id.right_menu_delete_btn).setVisibility(View.GONE);
            findViewById(R.id.right_menu_save_as_btn).setVisibility(View.GONE);
        }
        else{
            setViewPagerData();
            setDataFromDb();
            setModel();
        }
    }

    private void setViewPagerData() {
        ((TextView)(findViewById(R.id.top_panel_txt))).setText(type);
        findViewById(R.id.active_page_image1).setVisibility(View.GONE);
        findViewById(R.id.active_page_image2).setVisibility(View.GONE);
        int pageCount = 1;
        if(isPassport) {
            listenerForF1 = new PassportFirstFragment();
            listenerForF2 = new PassportSecondFragment();
            pageCount = 2;
            findViewById(R.id.active_page_image1).setVisibility(View.VISIBLE);
            findViewById(R.id.active_page_image2).setVisibility(View.VISIBLE);
        }
        if(isSnils){
            listenerForF1 = new SnilsFirstFragment();
            listenerForF2 = new SnilsFirstFragment();
        }
        if(isInn){
            listenerForF1 = new InnFirstFragment();
            listenerForF2 = new InnFirstFragment();
        }
        if(isPolis){
            listenerForF1 = new PolicyFirstFragment();
            listenerForF2 = new PolicySecondFragment();
            pageCount = 2;
            findViewById(R.id.active_page_image1).setVisibility(View.VISIBLE);
            findViewById(R.id.active_page_image2).setVisibility(View.VISIBLE);
        }
        viewPager2 = findViewById(R.id.frame_container);
        viewPager2.setAdapter(new MyFragmentAdapter(pageCount ,getSupportFragmentManager(), getLifecycle(), listenerForF1,listenerForF2));
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
    }

    private void setOnClickListeners() {
        findViewById(R.id.menubar_options).setOnClickListener(this::menuOptionsBtnClick);
        findViewById(R.id.confirm_button).setOnClickListener(v->{
            if(saveMethod())
            onBackPressed();
        });
        findViewById(R.id.menubar_Back).setOnClickListener(this::goBackMainPageClick);
        findViewById(R.id.INN_top_btn).setOnClickListener(v->{
            findViewById(R.id.top_panel_txt).callOnClick();
            type = getString(R.string.inn);
            isInn=true;
            isSnils = isPassport = isPolis = false;
            setViewPagerData();
            setDataFromDb();
            setModel();
        });
        findViewById(R.id.passport_top_btn).setOnClickListener(v->{
            findViewById(R.id.top_panel_txt).callOnClick();
            type = getString(R.string.passport);
            isPassport=true;
            isSnils = isInn = isPolis = false;
            setViewPagerData();
            setDataFromDb();
            setModel();
        });
        findViewById(R.id.SNILS_top_btn).setOnClickListener(v->{
            findViewById(R.id.top_panel_txt).callOnClick();
            type = getString(R.string.snils);
            isSnils=true;
            isInn= isPassport = isPolis = false;
            setViewPagerData();
            setDataFromDb();
            setModel();
        });
        findViewById(R.id.policy_top_btn).setOnClickListener(view -> {
            findViewById(R.id.top_panel_txt).callOnClick();
            type = getString(R.string.policy);
            isPolis=true;
            isInn= isPassport = isSnils = false;
            setViewPagerData();
            setDataFromDb();
            setModel();
        });
        findViewById(R.id.right_menu_save_btn).setOnClickListener(v->{
            if(saveMethod())
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
            Intent intent = new Intent(MainDocumentPatternActivity.this, TemplateActivity.class);
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
        type = intent.getStringExtra("type");
        if(type.equals("Passport")) {
            isPassport = true;
            type = getString(R.string.passport);
        }
        else if(type.equals("SNILS")) {
            type = getString(R.string.snils);
            isSnils = true;
        }
        else if(type.equals("INN")) {
            type = getString(R.string.inn);
            isInn = true;
        }
        else if(type.equals("Polis")) {
            type = getString(R.string.policy);
            isPolis = true;
        }
        else
            type = getString(R.string.document_string);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }
    private void setDataFromDb() {
        Item item = CurrentItem;
        if (item != null) {
            if(isPassport)
                this.Passport = db.getPassportById(item.Id);
            if(isInn)
                this.Inn = db.getInnById(item.Id);
            if(isSnils)
                this.Snils = db.getSnilsById(item.Id);
            if(isPolis)
                this.Polis = db.getPolisById(item.Id);
        } else {
            if(isPassport)
                this.Passport = new Passport(NULL_UUID, "", "", "", "", "", "", "M", "", "", null, null, null, null);
            if(isInn)
                this.Inn = new Inn(NULL_UUID, "", "",  "M", "", "", "", null, null);
            if(isPolis)
                this.Polis = new Polis(NULL_UUID,"" ,"","M","",null,null,"", null);
            if(isSnils)
                this.Snils = new Snils(NULL_UUID, "", "",  "M", "", "", "", null, null);
        }
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
            if(saveMethod())
            onBackPressed();
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
        saveMethod();
        isChanged = false;
        if(isPassport)
            if(PDFMaker.createPassportPDF( Passport,AppService.getUserId(this)))
                Toast.makeText(this,R.string.pdf_document_created,Toast.LENGTH_SHORT).show();
        if(isSnils)
            if(PDFMaker.createSnilsPDF( Snils,AppService.getUserId(this)))
                Toast.makeText(this,R.string.pdf_document_created,Toast.LENGTH_SHORT).show();
        if(isInn)
            if(PDFMaker.createInnPDF( Inn,AppService.getUserId(this)))
                Toast.makeText(this,R.string.pdf_document_created,Toast.LENGTH_SHORT).show();
        if(isPolis)
            if(PDFMaker.createPolisPDF( Polis,AppService.getUserId(this)))
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
    private boolean saveMethod() {
        if (!listenerForF1.IsValidData())
            return false;
        listenerForF1.SaveData();
        Item item;
        UUID ItemId;
        if (isCreateMode) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
            String time = df.format(new Date());

            if(isPassport) {
                item = new Item(NULL_UUID, getString(R.string.passport) + db.selectLastItemId(), "Passport", null, 0, 0, 0, time, NULL_UUID, 0, "");
                ItemId = db.insertItem(item, true);
                createPassport(item, ItemId);
                db.updateItem(ItemId, item,false);
            }
            if(isSnils) {
                item = new Item(NULL_UUID, getString(R.string.snils) + db.selectLastItemId(), "SNILS", null, 0, 0, 0, time, NULL_UUID, 0, "");
                ItemId = db.insertItem(item, true);
                createSnils(ItemId);
            }
            if(isInn) {
                item = new Item(NULL_UUID, getString(R.string.inn) + db.selectLastItemId(), "INN", null, 0, 0, 0, time, NULL_UUID, 0, "");
                ItemId = db.insertItem(item, true);
                createInn(ItemId);
            }
            if(isPolis) {
                item = new Item(NULL_UUID, getString(R.string.policy) + db.selectLastItemId(), "Polis", null, 0, 0, 0, time, NULL_UUID, 0, "");
                ItemId = db.insertItem(item, true);
                createPolis(ItemId);
            }
        } else {
            if(isPassport)
                updatePassport();
            if(isSnils)
                updateSnils();
            if(isInn)
                updateInn();
            if(isPolis)
                updatePolis();
            db.updateItem(CurrentItem.Id, CurrentItem,false);
        }
        return true;
    }

    private void updatePassport() {
        List<Bitmap> bitmaps = listenerForF1.getPhotos();
        Passport.FacePhoto = savePhoto(bitmaps.get(0),CurrentItem.Id,Passport.FacePhoto,"Passport", "FacePhoto");
        bitmaps = listenerForF2.getPhotos();
        Passport.PhotoPage1 = savePhoto(bitmaps.get(0),CurrentItem.Id,Passport.PhotoPage1,"Passport", "PhotoPage1");
        Passport.PhotoPage2 = savePhoto(bitmaps.get(1),CurrentItem.Id,Passport.PhotoPage2,"Passport", "PhotoPage2");
        CurrentItem.Image = this.Passport.FacePhoto;
        db.updatePassport(CurrentItem.Id, this.Passport, false);
    }

    private void createPassport(Item item, UUID ItemId) {
        this.Passport.Id = ItemId;
        db.insertPassport(this.Passport);
        List<Bitmap> bitmaps = listenerForF1.getPhotos();
        Passport.FacePhoto = savePhoto(bitmaps.get(0), ItemId,null,"Passport", "FacePhoto");
        bitmaps = listenerForF2.getPhotos();
        Passport.PhotoPage1 = savePhoto(bitmaps.get(0), ItemId,null,"Passport", "PhotoPage1");
        Passport.PhotoPage2 = savePhoto(bitmaps.get(1), ItemId,null,"Passport", "PhotoPage2");
        db.updatePassport(ItemId, this.Passport, false);
        item.Image = this.Passport.FacePhoto;
    }
    private void updatePolis() {
        List<Bitmap> bitmaps = listenerForF2.getPhotos();
        Polis.PhotoPage1 = savePhoto(bitmaps.get(0),CurrentItem.Id,Polis.PhotoPage1,"Polis", "PhotoPage1");
        Polis.PhotoPage2 = savePhoto(bitmaps.get(1),CurrentItem.Id,Polis.PhotoPage2,"Polis", "PhotoPage2");
        db.updatePolis(CurrentItem.Id, this.Polis, false);
    }

    private void createPolis(UUID ItemId) {
        this.Polis.Id = ItemId;
        db.insertPolis(this.Polis);
        List<Bitmap> bitmaps = listenerForF2.getPhotos();
        Polis.PhotoPage1 = savePhoto(bitmaps.get(0), ItemId,null,"Polis", "PhotoPage1");
        Polis.PhotoPage2 = savePhoto(bitmaps.get(1), ItemId,null,"Polis", "PhotoPage2");
        db.updatePolis(ItemId, this.Polis, false);
    }
    private void updateSnils() {
        List<Bitmap> bitmaps = listenerForF1.getPhotos();
        Snils.PhotoPage1 = savePhoto(bitmaps.get(0),CurrentItem.Id,Snils.PhotoPage1,"SNILS", "PhotoPage1");
        db.updateSnils(CurrentItem.Id, this.Snils, false);
    }

    private void createSnils( UUID ItemId) {
        this.Snils.Id = ItemId;
        db.insertSnils(this.Snils);
        List<Bitmap> bitmaps = listenerForF1.getPhotos();
        Snils.PhotoPage1 = savePhoto(bitmaps.get(0), ItemId,null,"SNILS", "PhotoPage1");
        db.updateSnils(ItemId, this.Snils, false);
    }
    private void updateInn() {
        List<Bitmap> bitmaps = listenerForF1.getPhotos();
        Inn.PhotoPage1 = savePhoto(bitmaps.get(0),CurrentItem.Id,Inn.PhotoPage1,"INN", "PhotoPage1");
        db.updateInn(CurrentItem.Id, this.Inn, false);
    }

    private void createInn(UUID ItemId) {
        this.Inn.Id = ItemId;
        db.insertInn(this.Inn);
        List<Bitmap> bitmaps = listenerForF1.getPhotos();
        Inn.PhotoPage1 = savePhoto(bitmaps.get(0), ItemId,null,"INN", "PhotoPage1");
        db.updateInn(ItemId, this.Inn, false);
    }

    private String savePhoto(Bitmap photo, UUID ItemId, String imageField, String type, String name) {
        if (photo != null) {
            File rootDir = getApplicationContext().getFilesDir();
            String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME +"/"+ AppService.getUserId(this)+ "/Item" + ItemId.toString() + "/"+type+"/";
            File dir = new File(imgPath);
            if (!dir.exists())
                dir.mkdirs();
            String imgName = name;
            File imgFile = new File(dir, imgName);
            if (imageField != null) {
                File filePath = new File(imageField);
                filePath.delete();
                imgFile = new File(imageField);
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
        return imageField;
    }

    @Override
    protected void onResume() {
        super.onResume();
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