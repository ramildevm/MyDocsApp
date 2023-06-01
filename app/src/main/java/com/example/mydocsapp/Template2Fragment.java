package com.example.mydocsapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mydocsapp.apputils.RecyclerItemClickListener;
import com.example.mydocsapp.databinding.Template2FragmentBinding;
import com.example.mydocsapp.models.Template;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;
import com.example.mydocsapp.services.TemplateAdapter;

import java.util.ArrayList;
import java.util.List;

public class Template2Fragment extends Fragment {
    Template2FragmentBinding binding;
    private DBHelper db;
    private TemplateAdapter adapter;
    private List<Template> publishedTemplatesList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = Template2FragmentBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();

        db= new DBHelper(getContext(), AppService.getUserId(getContext()));
        setListData();
        adapter = new TemplateAdapter(getContext(), publishedTemplatesList, false);
        binding.templateContainer.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.templateContainer.setAdapter(adapter);
        binding.templateContainer.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), binding.templateContainer, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Template item = adapter.getTemplate(position);
                startActivity(new Intent(getActivity(),TemplateActivity.class).putExtra("template", item).putExtra("isReview",1));
                getActivity().overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
            }
            @Override
            public void onLongItemClick(View view, int position) {
            }
        }));
        binding.searchBarTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                setListData();
                String filter =""+editable.toString();
                adapter.onItemFilter((ArrayList<Template>) publishedTemplatesList,filter );
            }
        });

        return rootView;
    }
    private void setListData() {
        publishedTemplatesList = db.getTemplatePublished();
    }

}