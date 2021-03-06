package com.example.mydocsapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.mydocsapp.apputils.RecyclerItemClickListener;
import com.example.mydocsapp.models.DBHelper;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.ItemAdapter;
import com.example.mydocsapp.models.SystemContext;

import java.util.ArrayList;

public class FolderAddItemActivity extends AppCompatActivity {

    ArrayList<Item> items = new ArrayList<Item>();
    DBHelper db;
    private int selectedItemsNum;
    RecyclerView recyclerFolderView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_add_item);
        db = new DBHelper(this);
        setInitialData();
        recyclerFolderView = (RecyclerView) findViewById(R.id.container);

        recyclerFolderView.setLayoutManager(new GridLayoutManager(FolderAddItemActivity.this, 2));
        // создаем адаптер
        ItemAdapter adapter = new ItemAdapter(FolderAddItemActivity.this, items, true);
        // устанавливаем для списка адаптер
        recyclerFolderView.setAdapter(adapter);
        recyclerFolderView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerFolderView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onItemClick(View view, int position) {
                        Item item = (Item) view.getTag();

                            int newItemId = items.indexOf(item);

                            Log.e("select", item.isSelected + "");
                            if (item.isSelected == 0) {
                                item.isSelected = 1;
                                selectedItemsNum++;

                            } else {
                                item.isSelected = 0;
                                selectedItemsNum--;
                            }
                            view.setTag(item);
                        items.set(newItemId, item);
                            ((TextView) findViewById(R.id.top_select_picked_txt)).setText("Selected: " + selectedItemsNum);

                            reFillContentPanel(recyclerFolderView, items);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                }));
    }

    private void setInitialData() {
        items.clear();
        Cursor cur = db.getItemsByFolder0();
        Item item;
        while(cur.moveToNext()){
            item = new Item(cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getBlob(3),
                    cur.getInt(4),
                    cur.getInt(5),
                    cur.getInt(6),
                    cur.getInt(7),
                    cur.getInt(8));
            if(!(item.Type.equals("Папка")))
                items.add(item);
        }
    }
    public void topSelectBackClick(View view) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }

    public void topSelectAllClick(View view) {
            selectedItemsNum = 0;
            for (Item x:
                    items) {
                int newItemId = items.indexOf(x);
                x.isSelected = 1;
                items.set(newItemId,x);
                selectedItemsNum++;
            }
            ((TextView)findViewById(R.id.top_select_picked_txt)).setText("Selected: "+ selectedItemsNum);

            reFillContentPanel(recyclerFolderView,items);
    }
    void reFillContentPanel(RecyclerView _recyclerView,ArrayList<Item> _items){
        _recyclerView.removeAllViews();
        // создаем адаптер
        ItemAdapter adapter = new ItemAdapter(this, _items, true);
        // устанавливаем для списка адаптер
        _recyclerView.setAdapter(adapter);
    }
    public void saveSelectedClick(View view) {
        for (Item x:
                items) {
            if(x.isSelected == 1) {
                x.FolderId = SystemContext.CurrentItem.Id;
                x.isSelected = 0;
                selectedItemsNum--;
                db.updateItem(x.Id, x);
            }
        }
        setInitialData();
        reFillContentPanel(recyclerFolderView,items);
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText("Selected: "+ selectedItemsNum);

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }
}