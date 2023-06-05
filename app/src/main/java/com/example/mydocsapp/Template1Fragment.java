package com.example.mydocsapp;

import android.content.Intent;
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
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;
import com.example.mydocsapp.services.TemplateAdapter;

import java.util.ArrayList;
import java.util.List;

public class Template1Fragment extends Fragment implements Template1FragmentListener {
    Template1FragmentBinding binding;
    Boolean isSelectMode;
    private List<Template> selectedItemsSet;
    private ArrayList<Template> userTemplatesList;
    private ArrayList<Template> downloadedTemplatesList;
    private TemplateAdapter adapter;
    DBHelper db;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = Template1FragmentBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        db= new DBHelper(getContext(), AppService.getUserId(getContext()));
        setListData();
        isSelectMode = false;
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
                    startActivity(new Intent(getActivity(),TemplateActivity.class).putExtra("template", adapter.getTemplate(position)).putExtra("isReview",1));
                    getActivity().overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
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
        userTemplatesList = (ArrayList<Template>) db.getTemplateByUserId(AppService.getUserId(getContext()));
        userTemplatesList.add(null);
        downloadedTemplatesList = new ArrayList<>();
        downloadedTemplatesList = (ArrayList<Template>) db.getTemplateDownload(AppService.getUserId(getContext()));
        userTemplatesList.addAll(downloadedTemplatesList);
    }
    @Override
    public void onTemplateDelete() {
        for (Template template :
                selectedItemsSet) {
            db.deleteTemplate(template.Id);
        }
        adapter.onItemDeleted(selectedItemsSet);
        selectedItemsSet.clear();
        isSelectMode = false;
        reFillRecyclerView();
    }
    @Override
    public void onTemplatePublish() {
        adapter.onItemPublished(selectedItemsSet);
        selectedItemsSet.clear();
        isSelectMode = false;
        reFillRecyclerView();
    }
}