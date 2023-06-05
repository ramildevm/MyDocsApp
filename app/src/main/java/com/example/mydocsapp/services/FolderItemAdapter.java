package com.example.mydocsapp.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.R;
import com.example.mydocsapp.apputils.ImageService;
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

import jp.wasabeef.blurry.Blurry;

public class FolderItemAdapter extends BaseAdapter {
    private Context context;
    private List<Item> items;
    public FolderItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_folder_item, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.image_panel);
            viewHolder.titleView = convertView.findViewById(R.id.title_txt);
            viewHolder.itemPanel = convertView.findViewById(R.id.item_panel);
            viewHolder.folderItemPanel = convertView.findViewById(R.id.folder_content_back);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Item item = items.get(position);
        if(item.Image==null) {
            viewHolder.folderItemPanel.setBackgroundResource(R.drawable.rounded_border_white_14);
            if (item.Type.equals("Паспорт"))
                viewHolder.imageView.setImageResource(R.drawable.passport_image_folder);
            else if (item.Type.equals("Паспорт"))
                viewHolder.imageView.setImageResource(R.drawable.image_credit_card_folder);
            else
                viewHolder.imageView.setImageResource(R.drawable.passport_image_folder);
        }
        else{
            viewHolder.folderItemPanel.setBackgroundResource(R.drawable.rounded_transparent);
            viewHolder.imageView.setVisibility(View.VISIBLE);
            File outputFile = new File(item.Image+"_copy");
            File encFile = new File(item.Image);
            try {
                MyEncrypter.decryptToFile(AppService.getMy_key(), AppService.getMy_spec_key(), new FileInputStream(encFile), new FileOutputStream(outputFile));
                Bitmap image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(outputFile));
                image = ImageService.scaleDown(image, ImageService.dpToPx(context, 50), true);
                int sizeInDP = 3;
                int marginInDp = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, sizeInDP, this.context.getResources()
                                .getDisplayMetrics());
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewHolder.imageView.getLayoutParams();
                params.leftMargin = marginInDp; params.topMargin = marginInDp; params.rightMargin = marginInDp; params.bottomMargin=marginInDp;
                viewHolder.imageView.setImageBitmap(image);
                //Glide.with(context).load(image).transform(new RoundedCorners(radius)).into(viewHolder.imageView);
                outputFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        viewHolder.titleView.setText(item.Title);

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView titleView;
        ConstraintLayout itemPanel;
        ConstraintLayout folderItemPanel;
    }
}
