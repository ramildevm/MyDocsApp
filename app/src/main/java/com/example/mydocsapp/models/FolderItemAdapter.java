package com.example.mydocsapp.models;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.R;
import com.example.mydocsapp.apputils.ImageSaveService;

import java.util.ArrayList;
import java.util.List;

public class FolderItemAdapter extends RecyclerView.Adapter<FolderItemAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private Context context;
    private final List<Item> items;
    private boolean isSelectMode;
    private DBHelper db = null;


    public FolderItemAdapter(Context context, List<Item> items, boolean isSelectMode) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public FolderItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_folder_item, parent, false);
        return new FolderItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FolderItemAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);
        if(item.Image==null) {
            if (item.Type.equals("Паспорт"))
                holder.imageView.setImageResource(R.drawable.passport_image);
            else
                holder.imageView.setImageResource(R.drawable.passport_image);
        }
        else{
            holder.imageView.setVisibility(View.VISIBLE);
            Bitmap image = BitmapFactory.decodeFile(item.Image);
            image = ImageSaveService.scaleDown(image, ImageSaveService.dpToPx(context, 50), true);
            holder.imageView.setImageBitmap(image);
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
