package com.example.mydocsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mydocsapp.apputils.RecyclerItemClickListener;
import com.example.mydocsapp.databinding.Template1FragmentBinding;
import com.example.mydocsapp.interfaces.Template1FragmentListener;
import com.example.mydocsapp.models.Template;
import com.example.mydocsapp.services.TemplateAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Template1Fragment extends Fragment implements Template1FragmentListener {

    Template1FragmentBinding binding;
    Boolean isSelectMode;
    private List<Template> selectedItemsSet;
    private ArrayList<Template> userTemplatesList;
    private ArrayList<Template> downloadedTemplatesList;
    private TemplateAdapter adapter;

    public static Template1Fragment newInstance() {
        return new Template1Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = Template1FragmentBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setListData();
        adapter = new TemplateAdapter(getContext(), userTemplatesList, false);
        binding.templateContainer.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.templateContainer.setAdapter(adapter);
        binding.templateContainer.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), binding.templateContainer, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Template item = adapter.getTemplate(position);
                if(item.Id==0)
                    return;
                if (isSelectMode) {
                    if (item.isSelected == false) {
                        item.isSelected = true;
                        selectedItemsSet.add(item);
                    } else {
                        item.isSelected = false;
                        selectedItemsSet.remove(item);
                    }
                    if (selectedItemsSet.size()==0){
                        isSelectMode = false;
                        reFillRecyclerView();
                        return;
                    }
                    adapter.onItemChanged(position, item);
                } else {

                }
            }

            @Override
            public void onLongItemClick(View view, int position) {
                Template item = adapter.getTemplate(position);
                selectedItemsSet = new ArrayList<>();
                selectedItemsSet.add(item);
                isSelectMode = true;
                item.isSelected = true;
                adapter.onItemChanged(position, item);
                reFillRecyclerView();

            }
        }));
        return rootView;

    }

    private void reFillRecyclerView() {
        adapter.setSelectMode(isSelectMode);
        for (Template temp :
                userTemplatesList) {
            adapter.onItemChanged(temp);
        }
    }

    private void setListData() {
        selectedItemsSet = new ArrayList<>();
        userTemplatesList = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.US);
            String time = df.format(new Date());
            userTemplatesList.add(new Template(i, "My template" + i, time, "New", i));
        }
        userTemplatesList.add(null);

        downloadedTemplatesList = new ArrayList<>();
        for (int i = 1; i < 12; i++) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS", Locale.US);
            String time = df.format(new Date());
            downloadedTemplatesList.add(new Template(i, "Download template" + i, time, "New", i));
        }
        userTemplatesList.addAll(downloadedTemplatesList);
    }

    @Override
    public void onTemplateDelete() {
        adapter.onItemDeleted(selectedItemsSet);
        selectedItemsSet.clear();
    }
}