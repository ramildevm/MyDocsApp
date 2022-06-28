package com.example.mydocsapp.models;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.R;

import java.util.List;

public class ItemAdapter  extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Item> items;

    public ItemAdapter(Context context, List<Item> items) {
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);
        if(item.Image==null) {
            if (item.Type.equals("Папка"))
                holder.imageView.setImageResource(0);
            else if (item.Type.equals("Паспорт"))
                holder.imageView.setImageResource(R.drawable.passport_image);
            else
                holder.imageView.setImageResource(R.drawable.passport_image);
        }
        holder.itemPanel.setTag(item);
        holder.titleView.setText(item.Title);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView titleView;
        final ConstraintLayout itemPanel;
        ViewHolder(View view){
            super(view);
            imageView = view.findViewById(R.id.image_panel);
            titleView = view.findViewById(R.id.title_txt);
            itemPanel = view.findViewById(R.id.item_panel);
        }
    }
}
