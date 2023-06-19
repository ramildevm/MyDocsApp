package com.example.mydocsapp.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.mydocsapp.R;
import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.interfaces.IItemAdapterActivity;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.blurry.Blurry;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private static final String PAYLOAD_DELETE_MODE = "PAYLOAD_DELETE_MODE";
    public static final String PAYLOAD_SELECT_MODE = "PAYLOAD_SELECT_MODE";
    private final LayoutInflater inflater;
    private Context context;
    private List<Item> items;
    private boolean isSelectMode;
    private DBHelper db;


    public ItemAdapter(Context context, List<Item> items, boolean isSelectMode) {
        this.context = context;
        this.db = new DBHelper(context, AppService.getUserId(context));
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
                else if(payload.equals(PAYLOAD_DELETE_MODE)){
                    if (item.Type.equals("Folder")) {
                        holder.imageView.setVisibility(View.INVISIBLE);
                        holder.gridFolder.setVisibility(View.VISIBLE);
                    } else {
                        holder.imageView.setVisibility(View.VISIBLE);
                        holder.gridFolder.setVisibility(View.INVISIBLE);
                    }
                }
            }
        } else
            super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.gridFolder.setAdapter(null);
        holder.gridFolder.setVisibility(View.INVISIBLE);
        holder.imageView.setVisibility(View.INVISIBLE);
        int radius = context.getResources().getDimensionPixelSize(R.dimen.corner_radius_14);
        if (item.Image == null) {
            if (item.Type.equals("Folder")) {
                holder.imageView.setVisibility(View.INVISIBLE);
                holder.gridFolder.setVisibility(View.VISIBLE);
                if (db.getItemFolderItemsCount(item.Id) > 0) {
                    ArrayList<Item> items = (ArrayList<Item>) db.getItemsByFolder(item.Id,AppService.isHideMode()?1:0);
                    FolderItemAdapter adapter = new FolderItemAdapter(context, items);
                    holder.gridFolder.setAdapter(adapter);
                }
            }
            else if (item.Type.equals("Passport")){
                holder.gridFolder.setVisibility(View.INVISIBLE);
                Glide.with(context).load(R.drawable.passport_image).transform(new RoundedCorners(radius)).into(holder.imageView);
                holder.imageView.setVisibility(View.VISIBLE);
            }
            else if (item.Type.equals("CreditCard")){
                holder.gridFolder.setVisibility(View.INVISIBLE);
                Glide.with(context).load(R.drawable.image_credit_card).transform(new RoundedCorners(radius)).into(holder.imageView);
                holder.imageView.setVisibility(View.VISIBLE);
            }
            else{
                holder.gridFolder.setVisibility(View.INVISIBLE);
                Glide.with(context).load(R.drawable.passport_image).transform(new RoundedCorners(radius)).into(holder.imageView);
                holder.imageView.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.gridFolder.setVisibility(View.INVISIBLE);
            File outputFile;
            File encFile;
            try {
                Bitmap combinedBitmap;
                Bitmap image;
                if(item.Type.equals("Collection")) {
                    ArrayList<Bitmap> photos = new ArrayList<>();
                    List<Photo> photoList = db.getPhotos(item.Id);
                    for (Photo photo :
                            photoList) {
                        outputFile = new File(photo.Image +"_copy");
                        encFile = new File(photo.Image);
                        MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                        image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(outputFile));
                        image = ImageService.scaleDown(image, ImageService.dpToPx(context, 150), true);
                        int sizeInDP = 3;
                        int marginInDp = (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, sizeInDP, this.context.getResources()
                                        .getDisplayMetrics());
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.imageView.getLayoutParams();
                        params.leftMargin = marginInDp;
                        params.topMargin = marginInDp;
                        params.rightMargin = marginInDp;
                        params.bottomMargin = marginInDp;
                        photos.add(image);
                        outputFile.delete();
                    }
                    Bitmap photoFirst = photos.get(0);
                    if(photos.size()==1){
                        image = photoFirst;
                    }
                    else {
                        int length = (photos.size() > 2) ? 3 : photos.size();
                        int maxWidth = 0;
                        int maxHeight = 0;
                        for (int i = 0; i < length; i++) {
                            Bitmap photo = photos.get(i);
                            if(photo.getWidth() > maxWidth)
                                maxWidth = photo.getWidth();
                            if(photo.getHeight()>maxHeight)
                                maxHeight = photo.getHeight();
                        }
                        maxWidth += 20 + 30*(length-1);
                        maxHeight += 20 + 30*(length-1);
                        combinedBitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(combinedBitmap);
                        for (int i = length-1; i >=0; i--) {
                            Bitmap photo = photos.get(i);
                            photo = ImageService.addStrokeToBitmap(photo, Color.BLACK,2);
                            if(i==0)
                                canvas.drawBitmap(photo, 10, 10+30*(length-1-i), null);
                            else
                                canvas.drawBitmap(photo, 10+30*i, 10+30*(length-1-i), null);
                        }
                        image = combinedBitmap;
                    }
                }
                else{
                    outputFile = new File(item.Image+"_copy");
                    encFile = new File(item.Image);
                    MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                    image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(outputFile));
                    image = ImageService.scaleDown(image, ImageService.dpToPx(context, 150), true);
                    int sizeInDP = 3;
                    int marginInDp = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, sizeInDP, this.context.getResources()
                                    .getDisplayMetrics());
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.imageView.getLayoutParams();
                    params.leftMargin = marginInDp;
                    params.topMargin = marginInDp;
                    params.rightMargin = marginInDp;
                    params.bottomMargin = marginInDp;
                    outputFile.delete();
                }

                holder.imageView.setImageBitmap(image);
                //Glide.with(context).load(image).transform(new RoundedCorners(radius)).into(holder.imageView);

            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (item.Type.equals("Folder"))
            holder.icoImg.setImageResource(R.drawable.ic_folder);
        else if (item.Type.equals("Passport"))
            holder.icoImg.setImageResource(R.drawable.ic_personalcard);
        else if (item.Type.equals("Template"))
            holder.icoImg.setImageResource(R.drawable.ic_user_template);
        else if (item.Type.equals("CreditCard"))
            holder.icoImg.setImageResource(R.drawable.ic_card);
        else if (item.Type.equals("Изображение")) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.gridFolder.setVisibility(View.INVISIBLE);
            holder.icoImg.setImageResource(R.drawable.ic_image);
        }
        else if (item.Type.equals("Альбом")) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.gridFolder.setVisibility(View.INVISIBLE);
            holder.icoImg.setImageResource(R.drawable.ic_image_collection);
        }
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
        holder.titleView.setOnTouchListener((view,motionEvent)-> {
                ((IItemAdapterActivity) context).setIsTitleClicked(true);
                return true;
        });
        if (item.Type.equals("Folder")) {
            Blurry.delete(holder.folderContentBack);
            holder.folderContentBack.post(() -> {
                Blurry.with(context)
                        .radius(6)
                        .onto(holder.folderContentBack);
            });
        }
        holder.titleView.setTag(item);
    }

    public void setSelectMode(Boolean value) {
        isSelectMode = value;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public void onItemDelete(Item item) {
        int position;
        if(items.contains(item))
            position = items.indexOf(item);
        else
            return;
        items.remove(position);
        notifyItemRemoved(position);
    }
    public void onItemSetChange(ArrayList<Item> _items) {
        if(items!=null) {
            items = _items;
            notifyDataSetChanged();
        }
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
        notifyItemChanged(position, PAYLOAD_DELETE_MODE);
    }
    public void updateItems(List<Item> newItems) {
        items.clear();
        items.addAll(newItems);

        List<Item> tempItems = new ArrayList<>(items);

        for (int i = 0; i < newItems.size(); i++) {
            Item newItem = newItems.get(i);
            int currentPosition = tempItems.indexOf(newItem);
            int expectedPosition = i;

            if (currentPosition != expectedPosition) {
                onItemMoved(currentPosition, expectedPosition);
                Collections.swap(tempItems, currentPosition, expectedPosition);
            }
        }
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
        final GridView gridFolder;
        final ConstraintLayout folderContentBack;
        View rowView;

        ViewHolder(View view) {
            super(view);
            rowView = view;
            folderContentBack = view.findViewById(R.id.folder_content_back);
            imageView = view.findViewById(R.id.image_panel);
            titleView = view.findViewById(R.id.title_txt);
            itemPanel = view.findViewById(R.id.item_panel);
            gridFolder = view.findViewById(R.id.recycler_folder);
            selectBtn = view.findViewById(R.id.select_btn);
            pinBtn = view.findViewById(R.id.pin_btn);
            icoImg = view.findViewById(R.id.ico_img);
        }
    }
}
