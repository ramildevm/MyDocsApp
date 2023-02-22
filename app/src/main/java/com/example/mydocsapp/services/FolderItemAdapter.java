package com.example.mydocsapp.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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
        int radius = context.getResources().getDimensionPixelSize(R.dimen.corner_radius_14);
        if(item.Image==null) {
            holder.folderItemPanel.setBackgroundResource(R.drawable.rounded_border_white_14);
            if (item.Type.equals("Паспорт"))
                holder.imageView.setImageResource(R.drawable.passport_image_folder);
            else if (item.Type.equals("Паспорт"))
                holder.imageView.setImageResource(R.drawable.image_credit_card_folder);
            else
                holder.imageView.setImageResource(R.drawable.passport_image_folder);
        }
        else{
            holder.folderItemPanel.setBackgroundResource(R.drawable.rounded_transparent);
            holder.imageView.setVisibility(View.VISIBLE);
            File outputFile = new File(item.Image+"_copy");
            File encFile = new File(item.Image);
            try {
                MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                Bitmap image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(outputFile));
                image = ImageSaveService.scaleDown(image, ImageSaveService.dpToPx(context, 50), true);
                int sizeInDP = 3;
                int marginInDp = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, sizeInDP, this.context.getResources()
                                .getDisplayMetrics());
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.imageView.getLayoutParams();
                params.leftMargin = marginInDp; params.topMargin = marginInDp; params.rightMargin = marginInDp; params.bottomMargin=marginInDp;
                holder.imageView.setImageBitmap(image);
                //Glide.with(context).load(image).transform(new RoundedCorners(radius)).into(holder.imageView);
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
        final ConstraintLayout folderItemPanel;
        ViewHolder(View view){
            super(view);
            imageView = view.findViewById(R.id.image_panel);
            titleView = view.findViewById(R.id.title_txt);
            itemPanel = view.findViewById(R.id.item_panel);
            folderItemPanel = view.findViewById(R.id.folder_content_back);
        }
    }
}
