package com.example.mydocsapp.models;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.R;
import com.example.mydocsapp.apputils.ImageSaveService;
import com.example.mydocsapp.apputils.ItemMoveCallback;
import com.example.mydocsapp.interfaces.ItemAdapterActivity;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.blurry.Blurry;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {

    private final LayoutInflater inflater;
    private Context context;
    private final List<Item> items;
    private boolean isSelectMode;
    private DBHelper db = null;


    public ItemAdapter(Context context, List<Item> items, boolean isSelectMode) {
        this.context = context;
        this.db = new DBHelper(context);
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.isSelectMode = isSelectMode;
    }
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);

        if (item.Type.equals("Папка")) {
            if(db.getItemFolderItemsCount(item.Id)>0) {
                ArrayList<Item> items = new ArrayList<>();

                Cursor cur = db.getItemsByFolder(item.Id);
                //Cursor cur = db.getItems();
                Item _item;
                while(cur.moveToNext()) {
                    _item = new Item(cur.getInt(0),
                            cur.getString(1),
                            cur.getString(2),
                            cur.getBlob(3),
                            cur.getInt(4),
                            cur.getInt(5),
                            cur.getInt(6),
                            cur.getInt(7),
                            cur.getInt(8));
                    items.add(_item);
                }
                holder.recyclerFolder.setLayoutManager(new GridLayoutManager(context, 2));
                FolderItemAdapter adapter = new FolderItemAdapter(context, items, false);
                holder.recyclerFolder.setAdapter(adapter);

            }
        }
        if(item.Image!=null) {
            Bitmap image = BitmapFactory.decodeByteArray(item.Image, 0, item.Image.length);
            image = ImageSaveService.scaleDown(image,ImageSaveService.dpToPx(context,150),true);
            holder.imageView.setImageBitmap(image);
        }
        else if (item.Type.equals("Паспорт"))
            holder.imageView.setImageResource(R.drawable.passport_image);
        else
            holder.imageView.setImageResource(R.drawable.passport_image);

        holder.itemPanel.setTag(item);
        holder.selectBtn.setTag(item);
        if(isSelectMode) {
            holder.selectBtn.setVisibility(View.VISIBLE);
            if(item.isSelected==1)
                holder.selectBtn.setBackgroundResource(R.drawable.selected_circle);
        }
        if(item.Priority > 0)
            holder.pinBtn.setVisibility(View.VISIBLE);
        holder.titleView.setText(item.Title);
        holder.titleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ((ItemAdapterActivity)context).setIsTitleClicked(true);
                return true;
            }
        });
        holder.folderContentBack.post(()->{
                    Blurry.with(context)
                            .radius(4)
                            .onto((ViewGroup) holder.folderContentBack);
        });
        holder.titleView.setTag(item);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onRowMoved(RecyclerView recyclerView, int fromPosition, int toPosition) {

    }


    @Override
    public void onRowSelected(ViewHolder myViewHolder) {
        myViewHolder.rowView.setAlpha(0.8f);
    }
    @Override
    public void onRowClear(ViewHolder myViewHolder, int fromPosition, int toPosition) {
        myViewHolder.rowView.setAlpha(1f);
        if(fromPosition != -1 && fromPosition!=toPosition) {
            Item item = items.get(fromPosition);
            item.FolderId = items.get(toPosition).Id;
            items.remove(fromPosition);
            db.updateItem(item.Id,item);
            notifyItemRemoved(fromPosition);
        }
    }

    public void onItemDelete(int position) {
        //int position = items.indexOf(item);
        items.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final ImageView selectBtn;
        final ImageView pinBtn;
        final TextView titleView;
        final ConstraintLayout itemPanel;
        final RecyclerView recyclerFolder;
        final ConstraintLayout folderContentBack;
        View rowView;
        ViewHolder(View view){
            super(view);
            rowView = view;
            folderContentBack = view.findViewById(R.id.folder_content_back);
            imageView = view.findViewById(R.id.image_panel);
            titleView = view.findViewById(R.id.title_txt);
            itemPanel = view.findViewById(R.id.item_panel);
            recyclerFolder = view.findViewById(R.id.recycler_folder);
            selectBtn = view.findViewById(R.id.select_btn);
            pinBtn = view.findViewById(R.id.pin_btn);
        }
    }
}
