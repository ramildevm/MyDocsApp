package com.example.mydocsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydocsapp.databinding.FragmentPassportFirstBinding;
import com.example.mydocsapp.interfaces.FragmentSaveViewModel;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.PassportStateViewModel;


public class PassportFirstFragment extends Fragment implements FragmentSaveViewModel {

    FragmentPassportFirstBinding binding;
    PassportStateViewModel model;

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

        binding = FragmentPassportFirstBinding.inflate(getLayoutInflater());
        model = new ViewModelProvider(requireActivity()).get(PassportStateViewModel.class);
        loadData();
        return binding.getRoot();
    }

    private void loadData() {
        Passport passport = model.getState().getValue();
        //photo load
        binding.editTextSeriesNumber.setText(passport.SeriaNomer);
        binding.editTextDivisionCode.setText(passport.DivisionCode);
        binding.editTextDateIssue.setText(passport.GiveDate);
        binding.editTextIssuedWhom.setText(passport.ByWhom);
        binding.editTextFullName.setText(passport.FIO);
        binding.editTextDateBirth.setText(passport.BirthDate);
        if(passport.Gender.equals("M"))
            binding.maleCheck.setChecked(true);
        else
            binding.femaleCheck.setChecked(true);
        binding.editTextPlaceBirth.setText(passport.BirthPlace);
        binding.editTextPlaceResidence.setText(passport.ResidencePlace);
    }

    @Override
    public void SaveData() {
        Passport passport = model.getState().getValue();
        passport.FacePhoto = null;
        passport.SeriaNomer = binding.editTextSeriesNumber.getText().toString();
        passport.DivisionCode =binding.editTextDivisionCode.getText().toString();
        passport.GiveDate =binding.editTextDateIssue.getText().toString();
        passport.ByWhom =binding.editTextIssuedWhom.getText().toString();
        passport.FIO = binding.editTextFullName.getText().toString();
        passport.BirthDate = binding.editTextDateBirth.getText().toString();
        if(binding.maleCheck.isChecked())
            passport.Gender = "M";
        else
            passport.Gender = "F";
        passport.BirthPlace = binding.editTextPlaceBirth.getText().toString();
        passport.ResidencePlace = binding.editTextPlaceResidence.getText().toString();
        model.setState(passport);
    }
}