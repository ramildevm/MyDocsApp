package com.example.mydocsapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

import com.example.mydocsapp.interfaces.FragmentSaveViewModel;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainPassportPatternActivity extends AppCompatActivity {
DBHelper db;
    private com.example.mydocsapp.models.Passport Passport;
    PassportStateViewModel model;
    private FragmentSaveViewModel listenerForF1;
    private FragmentSaveViewModel listenerForF2;

    public static final int SELECT_USER_PHOTO = 1;
    public static final int SELECT_PAGE1_PHOTO = 2;
    public static final int SELECT_PAGE2_PHOTO = 3;
    private Item CurrentItem;

    public Item getCurrentItem() {
        return CurrentItem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_passport_pattern);
        getExtraData(getIntent());
        ViewPager2 viewPager2 = findViewById(R.id.frame_container);
        viewPager2.setAdapter(new MyFragmentAdapter(getSupportFragmentManager(), getLifecycle(),this));
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
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
        db = new DBHelper(this, AppService.getUserId());
        setDataFromDb();

        model = new ViewModelProvider(this).get(PassportStateViewModel.class);
        model.getState().observe(this,item ->{
            this.Passport = item;
        });
        model.setState(this.Passport);

        if(CurrentItem  == null) {
            (findViewById(R.id.passport_txt)).setOnClickListener(v -> {
                if (v.getTag().equals("off")) {
                    MotionLayout ml = findViewById(R.id.motion_layout);
                    ml.setTransition(R.id.transTop);
                    ml.transitionToEnd();
                    v.setTag("on");
                } else if (v.getTag().equals("on")) {
                    MotionLayout ml = findViewById(R.id.motion_layout);
                    ml.setTransition(R.id.transTop);
                    ml.transitionToStart();
                    v.setTag("off");
                }
            });
        }
    }

    private void getExtraData(Intent intent) {
        CurrentItem= intent.getParcelableExtra("item");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }
    private void setDataFromDb() {
        Item item = CurrentItem;
        if(item!= null) {
            Cursor cur = db.getPassportById(item.Id);
            cur.moveToFirst();
            this.Passport = new Passport(0,
                cur.getString(1),
                cur.getString(2),
                cur.getString(3),
                cur.getString(4),
                cur.getString(5),
                cur.getString(6),
                cur.getString(7),
                cur.getString(8),
                cur.getString(9),
                cur.getString(10),
                cur.getString(11),
                cur.getString(12));
        }
        else
            this.Passport = new Passport(
                0,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null
            );
    }

    public void goBackMainPageClick(View view) {
        listenerForF1.SaveData();
        String itemImagePath = null;

        Item item;
        if(CurrentItem == null){
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.US);
            String time = df.format(new Date());
            item = new Item(0, "Паспорт" + db.selectLastItemId(), "Паспорт", null, 0,0,0, time, 0, 0);
            db.insertItem(item);
            int ItemId = db.selectLastItemId();
            this.Passport.Id = ItemId;

            db.insertPassport(this.Passport);

            listenerForF1.SavePhotos(ItemId);
            if (listenerForF2!=null) {
                listenerForF2.SavePhotos(ItemId);
                if (((PassportSecondFragment) listenerForF2).getPhotoOption())
                    itemImagePath = this.Passport.PhotoPage1;
            }
            db.updatePassport(ItemId,this.Passport);
            item.Image = itemImagePath;
            db.updateItem(ItemId,item);
        }
        else{
            listenerForF1.SavePhotos(CurrentItem.Id);
            if(listenerForF2!=null) {
                listenerForF2.SavePhotos(CurrentItem.Id);
                if (((PassportSecondFragment) listenerForF2).getPhotoOption())
                    itemImagePath = this.Passport.PhotoPage1;
                else
                    CurrentItem.Image = null;
            }
            if(listenerForF2!=null)
                CurrentItem.Image = itemImagePath;
            db.updatePassport(CurrentItem.Id, this.Passport);
            db.updateItem(CurrentItem.Id,CurrentItem);
        }
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    public void goInnClick(View view) {
        startActivity(new Intent(MainPassportPatternActivity.this, INNPatternActivity.class));
    }

    public void goPolicyClick(View view) {
        startActivity(new Intent(MainPassportPatternActivity.this, PolicyPatternActivity.class));
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }
    void setListenerForF1(FragmentSaveViewModel fragment){
        listenerForF1 = fragment;
    }

    void setListenerForF2(FragmentSaveViewModel fragment2) {
        listenerForF2 = fragment2;
    }


    public static class MyFragmentAdapter extends FragmentStateAdapter {
        private static int NUM_ITEMS = 2;
        MainPassportPatternActivity activity;

        public MyFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,MainPassportPatternActivity activity) {
            super(fragmentManager, lifecycle);
            this.activity = activity;
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            PassportFirstFragment f1 = new PassportFirstFragment();
            PassportSecondFragment f2 = new PassportSecondFragment();
            switch (position) {
                case 0:
                    activity.setListenerForF1(f1);
                    return f1;
                case 1:
                    activity.setListenerForF2(f2);
                    return f2;
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