package com.example.mydocsapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mydocsapp.databinding.ActivityMainTemplateBinding;
import com.example.mydocsapp.interfaces.Template1FragmentListener;

public class MainTemplateActivity extends AppCompatActivity {

    ActivityMainTemplateBinding binding;
    private FragmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainTemplateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        adapter = new FragmentAdapter(getSupportFragmentManager(), getLifecycle());
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        switch (position) {
                            case 0:
                                TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.active_page_image1).getBackground();
                                transition.startTransition(200);
                                TransitionDrawable transition2 = (TransitionDrawable) findViewById(R.id.active_page_image2).getBackground();
                                transition2.startTransition(200);
                                hideShowBottomButtons(0f, 1f);
                                return;
                            case 1:
                                TransitionDrawable transition_1 = (TransitionDrawable) findViewById(R.id.active_page_image1).getBackground();
                                transition_1.reverseTransition(200);
                                TransitionDrawable transition_2 = (TransitionDrawable) findViewById(R.id.active_page_image2).getBackground();
                                transition_2.reverseTransition(200);
                                hideShowBottomButtons(1f, 0f);
                                return;
                        }
                    }
                });
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        binding.bottomDeleteBtn.setOnClickListener(v-> onDeleteBtnClick(v));
        binding.bottomDeleteTxt.setOnClickListener(v-> onDeleteBtnClick(v));
    }

    private void onDeleteBtnClick(View v) {
            adapter.onTemplateDelete();
    }

    private void hideShowBottomButtons(float start, float end) {
        ObjectAnimator fadeOutAnimatorAdd = ObjectAnimator.ofFloat(binding.bottomAddBtn, "alpha", start, end).setDuration(200);
        ObjectAnimator fadeOutAnimatorAddTxt = ObjectAnimator.ofFloat(binding.bottomAddTxt, "alpha", start, end).setDuration(200);
        ObjectAnimator fadeOutAnimatorPublish = ObjectAnimator.ofFloat(binding.bottomPublishBtn, "alpha", start, end).setDuration(200);
        ObjectAnimator fadeOutAnimatorPublishTxt = ObjectAnimator.ofFloat(binding.bottomPublishTxt, "alpha", start, end).setDuration(200);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeOutAnimatorAdd, fadeOutAnimatorAddTxt, fadeOutAnimatorPublish, fadeOutAnimatorPublishTxt);
        animatorSet.start();
    }

    public void goBackMainMenuClick(View view) {
        onBackPressed();
    }

    public class FragmentAdapter extends FragmentStateAdapter {
        Template1FragmentListener template1FragmentListener;
        public FragmentAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }
        public void onTemplateDelete(){
            template1FragmentListener.onTemplateDelete();
        }
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                template1FragmentListener = new Template1Fragment();
                return (Template1Fragment)template1FragmentListener;
            } else {
                return new Template2Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}