package com.example.mydocsapp.models;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.MainPassportPatternActivity;
import com.example.mydocsapp.R;
import com.example.mydocsapp.apputils.ImageSaveService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.interfaces.ItemAdapterActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import jp.wasabeef.blurry.Blurry;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private Context context;
    private List<Item> items;
    private boolean isSelectMode;
    private DBHelper db;
    public static final String PAYLOAD_SELECT_MODE = "PAYLOAD_SELECT_MODE";

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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            final Item item = items.get(position);
            for (final Object payload : payloads) {
                if (payload.equals(PAYLOAD_SELECT_MODE)) {
                    if (isSelectMode) {
                        holder.selectBtn.setVisibility(View.VISIBLE);
                        if (item.isSelected == 1)
                            holder.selectBtn.setBackgroundResource(R.drawable.selected_circle);
                        else
                            holder.selectBtn.setBackgroundResource(R.drawable.unselected_circle);
                        if (item.Priority>0)
                            holder.pinBtn.setVisibility(View.VISIBLE);
                        else
                            holder.pinBtn.setVisibility(View.INVISIBLE);
                    } else {
                        holder.selectBtn.setVisibility(View.INVISIBLE);
                    }
                }
            }
        } else
            super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);
        if (item.Image == null) {
            if (item.Type.equals("Папка")) {
                holder.imageView.setVisibility(View.INVISIBLE);
                holder.recyclerFolder.setVisibility(View.VISIBLE);
                if (db.getItemFolderItemsCount(item.Id) > 0) {
                    ArrayList<Item> items = new ArrayList<>();
                    Cursor cur = db.getItemsByFolder(item.Id);
                    //Cursor cur = db.getItems();
                    Item _item;
                    while (cur.moveToNext()) {
                        _item = new Item(cur.getInt(0),
                                cur.getString(1),
                                cur.getString(2),
                                cur.getString(3),
                                cur.getInt(4),
                                cur.getInt(5),
                                cur.getInt(6),
                                cur.getString(7),
                                cur.getInt(8),
                                cur.getInt(9));
                        items.add(_item);
                    }
                    holder.recyclerFolder.setLayoutManager(new GridLayoutManager(context, 2));
                    FolderItemAdapter adapter = new FolderItemAdapter(context, items, false);
                    holder.recyclerFolder.setAdapter(adapter);
                }
            }
            else if (item.Type.equals("Паспорт")){
                holder.recyclerFolder.setVisibility(View.INVISIBLE);
                holder.imageView.setImageResource(R.drawable.passport_image);
                holder.imageView.setVisibility(View.VISIBLE);
            }
            else{
                holder.recyclerFolder.setVisibility(View.INVISIBLE);
                holder.imageView.setImageResource(R.drawable.passport_image);
                holder.imageView.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.recyclerFolder.setVisibility(View.INVISIBLE);
            File outputFile = new File(item.Image+"_copy");
            File encFile = new File(item.Image);
            try {
                MyEncrypter.decryptToFile(MainPassportPatternActivity.getMy_key(), MainPassportPatternActivity.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                Bitmap image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(outputFile));
                image = ImageSaveService.scaleDown(image, ImageSaveService.dpToPx(context, 150), true);
                holder.imageView.setImageBitmap(image);
                outputFile.delete();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (item.Type.equals("Папка"))
            holder.icoImg.setImageResource(R.drawable.ic_folder);
        else if (item.Type.equals("Паспорт"))
            holder.icoImg.setImageResource(R.drawable.ic_personalcard);
        else if (item.Type.equals("Изображение"))
            holder.icoImg.setImageResource(R.drawable.ic_image);
        else
            holder.icoImg.setImageResource(R.drawable.ic_document);


        holder.itemPanel.setTag(item);
        holder.selectBtn.setTag(item);
        if (isSelectMode) {
            holder.selectBtn.setVisibility(View.VISIBLE);
            if (item.isSelected == 1)
                holder.selectBtn.setBackgroundResource(R.drawable.selected_circle);
            else
                holder.selectBtn.setBackgroundResource(R.drawable.unselected_circle);
        } else {
            holder.selectBtn.setVisibility(View.INVISIBLE);
            holder.selectBtn.setBackgroundResource(R.drawable.unselected_circle);
        }
        if (item.Priority > 0)
            holder.pinBtn.setVisibility(View.VISIBLE);
        else
            holder.pinBtn.setVisibility(View.INVISIBLE);

        holder.titleView.setText(item.Title);
        holder.titleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ((ItemAdapterActivity) context).setIsTitleClicked(true);
                return true;
            }
        });
        holder.folderContentBack.post(() -> {
            Blurry.with(context)
                    .radius(4)
                    .onto((ViewGroup) holder.folderContentBack);
        });
        holder.titleView.setTag(item);

    }

    public void setSelectMode(Boolean value) {
        isSelectMode = value;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public void onItemDelete(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }
    public void onItemSetChange(ArrayList<Item> _items) {
        items = _items;
        notifyDataSetChanged();
    }

    public void onItemMoved(int oldPosition, int newPosition) {
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(oldPosition, newPosition);
    }
    public void onMovedItemChanged(Item item, int position) {
        items.set(position,item);
        notifyItemChanged(position);
    }
    public void onItemChanged(Item item) {
        Item newItem;
        int position;
        Item oldItem = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            oldItem = items.stream().filter(item1 -> item1.Id == item.Id).findFirst().get();
        }
        newItem = oldItem;
        position = items.indexOf(oldItem);
        newItem.isSelected = item.isSelected;
        newItem.Priority = item.Priority;
        items.set(position, newItem);
        if (!isSelectMode)
            notifyItemChanged(position);
        else
            notifyItemChanged(position, PAYLOAD_SELECT_MODE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final ImageView selectBtn;
        final ImageView pinBtn;
        final ImageView icoImg;
        final TextView titleView;
        final ConstraintLayout itemPanel;
        final RecyclerView recyclerFolder;
        final ConstraintLayout folderContentBack;
        View rowView;

        ViewHolder(View view) {
            super(view);
            rowView = view;
            folderContentBack = view.findViewById(R.id.folder_content_back);
            imageView = view.findViewById(R.id.image_panel);
            titleView = view.findViewById(R.id.title_txt);
            itemPanel = view.findViewById(R.id.item_panel);
            recyclerFolder = view.findViewById(R.id.recycler_folder);
            selectBtn = view.findViewById(R.id.select_btn);
            pinBtn = view.findViewById(R.id.pin_btn);
            icoImg = view.findViewById(R.id.ico_img);
        }
    }
}
