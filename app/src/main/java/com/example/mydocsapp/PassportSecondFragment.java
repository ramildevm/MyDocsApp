package com.example.mydocsapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.databinding.FragmentPassportSecondBinding;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;

public class PassportSecondFragment extends Fragment{

    String TAG = "FFragment2";
    FragmentPassportSecondBinding binding;
    PassportStateViewModel model;
    public PassportSecondFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPassportSecondBinding.inflate(inflater);
        binding.firstPassportPhoto.setOnClickListener(view -> {
            Passport passport = model.getState().getValue();
            passport.PhotoPage1 = null;
            model.setState(passport);
        });
        loadData();
        model = new ViewModelProvider(requireActivity()).get(PassportStateViewModel.class);
        return binding.getRoot();
    }

    private void loadData() {
        //load data here
    }

}