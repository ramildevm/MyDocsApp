package com.example.mydocsapp.services;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.R;
import com.example.mydocsapp.models.Template;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder>{
    private final LayoutInflater inflater;
    private Context context;
    private List<Template> items;
    private boolean isSelectMode;
    private DBHelper db;
    public TemplateAdapter(Context context, List<Template> items, boolean isSelectMode) {
        this.context = context;
        this.db = new DBHelper(context, AppService.getUserId(context));
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.isSelectMode = isSelectMode;
    }
    public Template getTemplate(int position){
        return items.get(position);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.list_template, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Template item = items.get(position);
        if(item.Id==0){
            holder.templatePanel.setVisibility(View.GONE);
            holder.splitLine.setVisibility(View.VISIBLE);
        }
        else{
            holder.txtName.setText(item.Name);
            holder.templatePanel.setVisibility(View.VISIBLE);
            holder.splitLine.setVisibility(View.GONE);
            if(isSelectMode){
                holder.selectBtn.setVisibility(View.VISIBLE);
                if (item.isSelected)
                    holder.selectBtn.setBackgroundResource(R.drawable.selected_circle);
                else
                    holder.selectBtn.setBackgroundResource(R.drawable.unselected_circle);
            }
            else{
                holder.selectBtn.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void onItemChanged(int position,Template item) {
        items.set(position,item);
        notifyItemChanged(position);
    }
    public void onItemChanged(Template item) {
        int position = items.indexOf(item);
        items.set(position,item);
        notifyItemChanged(position);
    }

    public void setSelectMode(Boolean _isSelectMode) {
        isSelectMode = _isSelectMode;
    }

    public void onItemDeleted(List<Template> templateSet) {
        for (Template temp :
                templateSet) {
            int position = items.indexOf(temp);
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName;
        final ConstraintLayout templatePanel;
        final CardView splitLine;
        final ImageView selectBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.template_name_txt);
            templatePanel = itemView.findViewById(R.id.template_panel);
            splitLine = itemView.findViewById(R.id.split_line);
            selectBtn = itemView.findViewById(R.id.select_btn);
        }
    }
}
