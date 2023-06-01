package com.example.mydocsapp;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.mydocsapp.interfaces.Changedable;
import com.example.mydocsapp.interfaces.IFragmentDataSaver;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;
import com.example.mydocsapp.models.Template;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainPassportPatternActivity extends AppCompatActivity implements Changedable {
    DBHelper db;
    private com.example.mydocsapp.models.Passport Passport;
    PassportStateViewModel model;
    private IFragmentDataSaver listenerForF1;
    private IFragmentDataSaver listenerForF2;

    public static final int SELECT_USER_PHOTO = 1;
    public static final int SELECT_PAGE1_PHOTO = 2;
    public static final int SELECT_PAGE2_PHOTO = 3;
    private Item CurrentItem;
    private Boolean isChanged = false;
    private boolean isCreateMode = false;

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
        ViewPager2 viewPager2 = findViewById(R.id.frame_container);
        viewPager2.setAdapter(new MyFragmentAdapter(getSupportFragmentManager(), getLifecycle(), this));
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
                Boolean btnFlag = v.getTag().equals("off");
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
        findViewById(R.id.menubar_options).setOnClickListener(v->menuOptionsBtnClick(v));
        findViewById(R.id.confirm_button).setOnClickListener(v->savePassportMethod());
        findViewById(R.id.menubar_Back).setOnClickListener(v->goBackMainPageClick(v));
        findViewById(R.id.INN_top_btn).setOnClickListener(v->goInnClick(v));
        findViewById(R.id.policy_top_btn).setOnClickListener(v->goPolicyClick(v));
        findViewById(R.id.right_menu_save_btn).setOnClickListener(v->savePassportMethod());
        findViewById(R.id.right_menu_save_as_btn).setOnClickListener(v->saveAsBtnClick(v));
        findViewById(R.id.right_menu_delete_btn).setOnClickListener(v->deleteBtnClick(v));
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
            this.Passport = new Passport(0,"","","","","","","M","","",null,null,null);
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
        listenerForF1.SaveData();
        String itemImagePath = null;
        Item item;
        if (isCreateMode) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.US);
            String time = df.format(new Date());
            item = new Item(0, "Паспорт" + db.selectLastItemId(), "Паспорт", null, 0, 0, 0, time, 0, 0);
            db.insertItem(item);
            int ItemId = db.selectLastItemId();
            this.Passport.Id = ItemId;
            db.insertPassport(this.Passport);
            listenerForF1.SavePhotos(ItemId);
            itemImagePath = this.Passport.FacePhoto;
            if (listenerForF2 != null) {
                listenerForF2.SavePhotos(ItemId);
            }
            db.updatePassport(ItemId, this.Passport);
            item.Image = itemImagePath;
            db.updateItem(ItemId, item);
        } else {
            listenerForF1.SavePhotos(CurrentItem.Id);
            itemImagePath = this.Passport.FacePhoto;
            if (listenerForF2 != null) {
                listenerForF2.SavePhotos(CurrentItem.Id);
            }
            if (listenerForF2 != null)
                CurrentItem.Image = itemImagePath;
            db.updatePassport(CurrentItem.Id, this.Passport);
            db.updateItem(CurrentItem.Id, CurrentItem);
        }
        onBackPressed();
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

    void setListenerForF1(IFragmentDataSaver fragment) {
        listenerForF1 = fragment;
    }

    void setListenerForF2(IFragmentDataSaver fragment2) {
        listenerForF2 = fragment2;
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
        MainPassportPatternActivity activity;

        public MyFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, MainPassportPatternActivity activity) {
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