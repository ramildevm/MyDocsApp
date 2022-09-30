package com.example.mydocsapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
import com.example.mydocsapp.models.DBHelper;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;

public class MainPassportPatternActivity extends AppCompatActivity {
DBHelper db;
    private com.example.mydocsapp.models.Passport Passport;
    PassportStateViewModel model;
    private FragmentSaveViewModel listenerForF1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_passport_pattern);
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
        db = new DBHelper(this);
        setDataFromDb();

        model = new ViewModelProvider(this).get(PassportStateViewModel.class);
        model.getState().observe(this,item ->{
            this.Passport = item;
        });
        model.setState(this.Passport);

        if(((App)getApplicationContext()).CurrentItem  == null) {
            ((TextView) findViewById(R.id.passport_txt)).setOnClickListener(v -> {
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

    private void setDataFromDb() {
        Item item = ((App)getApplicationContext()).CurrentItem;
        if(item!= null) {
            Cursor cur = db.getPassportById(item.ObjectId);
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
                    cur.getBlob(10),
                    cur.getBlob(11),
                    cur.getBlob(12));
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
        Passport p = this.Passport;
        if(((App)getApplicationContext()).CurrentItem == null){
            db.insertPassport(p);
            int id = Integer.parseInt(db.selectLastId());
            db.insertItem(new Item(0, "Паспорт", "Паспорт", null, 0, 0, 0, id, 0));
        }
        else{
            db.updatePassport(((App)getApplicationContext()).CurrentItem.ObjectId, p);
        }
        onBackPressed();
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
            switch (position) {
                case 0:
                    PassportFirstFragment f1 = new PassportFirstFragment();
                    activity.setListenerForF1(f1);
                    return f1;
                case 1:
                    return new PassportSecondFragment();
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