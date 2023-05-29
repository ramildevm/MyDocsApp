package com.example.mydocsapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.apputils.RecyclerItemClickListener;
import com.example.mydocsapp.interfaces.IItemAdapterActivity;
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.DBHelper;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.services.ItemAdapter;

import java.util.ArrayList;

public class FolderAddItemActivity extends AppCompatActivity implements IItemAdapterActivity {

    ArrayList<Item> items = new ArrayList<Item>();
    ItemAdapter adapter;
    DBHelper db;
    private int selectedItemsNum;
    RecyclerView recyclerFolderView;
    private Item CurrentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_add_item);
        getExtraData(getIntent());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MotionLayout ml = findViewById(R.id.motion_layout);
                ml.setTransition(R.id.transitionToEnd);
                ml.transitionToEnd();
            }
        }, 100);
        db = new DBHelper(this, AppService.getUserId(this));
        setInitialData();
        recyclerFolderView = (RecyclerView) findViewById(R.id.container);
        ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " "  + 0);

        recyclerFolderView.setLayoutManager(new GridLayoutManager(FolderAddItemActivity.this, 2));
        // создаем адаптер
        adapter = new ItemAdapter(FolderAddItemActivity.this, items, true);
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
                        ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " "  + selectedItemsNum);
                        reFillContentPanel(recyclerFolderView, items);
                    }
                @Override
                public void onLongItemClick(View view, int position) {

                }
                }));
    }

    private void getExtraData(Intent intent) {
        CurrentItem = intent.getParcelableExtra("item");

    }

    private void setInitialData() {
        items.clear();
        Cursor cur = db.getItemsByFolderIdForAdding(CurrentItem.Id,AppService.isHideMode()?1:0);
        Item item;
        while(cur.moveToNext()){
            item = new Item(cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4),
                    cur.getInt(5),
                    cur.getInt(6),
                    cur.getString(7),
                    cur.getInt(8),
                    cur.getInt(9));
            if(item.FolderId == CurrentItem.Id){
                item.isSelected = 1;
                selectedItemsNum++;
                ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " "  + selectedItemsNum);
            }
            if(!(item.Type.equals("Папка")))
                items.add(item);
        }
    }
    public void topSelectBackClick(View view) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
        overridePendingTransition(R.anim.alpha, R.anim.alpha_to_zero);
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
            ((TextView)findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + selectedItemsNum);

            reFillContentPanel(recyclerFolderView,items);
    }
    void reFillContentPanel(RecyclerView _recyclerView,ArrayList<Item> _items){
        _recyclerView.removeAllViews();
        // создаем адаптер
        adapter = new ItemAdapter(this, _items, true);
        // устанавливаем для списка адаптер
        _recyclerView.setAdapter(adapter);
    }
    public void saveSelectedClick(View view) {
        for (Item x: items) {
            int flag;
            if(x.isSelected == 1) {
                flag = CurrentItem.Id;
            }
            else{
                flag = 0;
            }
            selectedItemsNum--;
            db.updateItemFolder(x.Id, flag);
        }
        setInitialData();
        reFillContentPanel(recyclerFolderView,items);
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) + " " + selectedItemsNum);

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }

    @Override
    public boolean getIsTitleClicked() {
        return false;
    }
    @Override
    public void setIsTitleClicked(boolean value) {

    }
}