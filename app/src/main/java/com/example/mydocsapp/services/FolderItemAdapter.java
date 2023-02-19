package com.example.mydocsapp.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.R;
import com.example.mydocsapp.apputils.ImageSaveService;
import com.example.mydocsapp.apputils.MyEncrypter;
import com.example.mydocsapp.models.Item;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

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
            File outputFile = new File(item.Image+"_copy");
            File encFile = new File(item.Image);
            try {
                MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                Bitmap image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(outputFile));
                image = ImageSaveService.scaleDown(image, ImageSaveService.dpToPx(context, 50), true);
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
