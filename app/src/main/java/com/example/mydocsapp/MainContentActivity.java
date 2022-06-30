package com.example.mydocsapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mydocsapp.apputils.RecyclerItemClickListener;
import com.example.mydocsapp.models.DBHelper;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.ItemAdapter;
import com.example.mydocsapp.models.SystemContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainContentActivity extends AppCompatActivity {
    ArrayList<Item> items = new ArrayList<Item>();
    RecyclerView recyclerView;
    String filterOption = "All";
    DBHelper db;
    boolean isSelectMode;
    private int selectedItemsNum = 0;
    private ActivityResultLauncher<Intent> registerForAR;
    private RecyclerView recyclerFolderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);

        registerForAR = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                int resultCode = result.getResultCode();
                SystemContext.CurrentFolderItemsSet = getItemsFromDb(SystemContext.CurrentItem.Id);
                reFillContentPanel(recyclerFolderView, SystemContext.CurrentFolderItemsSet);
                setInitialData();
                reFillContentPanel(recyclerView, items);
            }
        });

        TextView gtxt = findViewById(R.id.login_txt);
        gtxt.setText(SystemContext.CurrentUser.login);
        // начальная инициализация списка
        // создаем базу данных
        db = new DBHelper(this);
        try {
            db.create_db();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        isSelectMode = false;
        setInitialData();
        setViewsTagOff();
        recyclerView = findViewById(R.id.container);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // создаем адаптер
        ItemAdapter adapter = new ItemAdapter(this, items, isSelectMode);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);

        setOnClickListeners();
    }
    private void setOnClickListeners() {
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override public void onItemClick(View view, int position) {
                        Item item = (Item) view.getTag();
                        if(isSelectMode) {
                            int newItemId = SystemContext.CurrentItemsSet.indexOf(item);

                            Log.e("select", item.isSelected + "");
                            if (item.isSelected == 0) {
                                item.isSelected = 1;
                                selectedItemsNum++;

                            } else {
                                item.isSelected = 0;
                                selectedItemsNum--;
                            }
                            view.setTag(item);
                            SystemContext.CurrentItemsSet.set(newItemId, item);
                            ((TextView)findViewById(R.id.top_select_picked_txt)).setText("Selected: "+ selectedItemsNum);

                            reFillContentPanel(recyclerView, SystemContext.CurrentItemsSet);
                        }
                        else{
                            SystemContext.CurrentItem = item;
                            if(item.Type.equals("Папка")) {
                                Dialog dialog = new Dialog(MainContentActivity.this);
                                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                dialog.setContentView(R.layout.folder_items_panel_layout);
                                dialog.setCancelable(true);

                                int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
                                int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
                                dialog.getWindow().setLayout(width, height);

                                //set up text
                                TextView text = (TextView) dialog.findViewById(R.id.title_folder_txt);
                                text.setText(item.Title);

                                //items set
                                SystemContext.CurrentFolderItemsSet = getItemsFromDb(item.Id);

                                recyclerFolderView = (RecyclerView) dialog.findViewById(R.id.folder_container);

                                recyclerFolderView.setLayoutManager(new GridLayoutManager(MainContentActivity.this, 2));
                                // создаем адаптер
                                ItemAdapter adapter = new ItemAdapter(MainContentActivity.this, SystemContext.CurrentFolderItemsSet, false);
                                // устанавливаем для списка адаптер
                                recyclerFolderView.setAdapter(adapter);
                                recyclerFolderView.addOnItemTouchListener(
                                        new RecyclerItemClickListener(MainContentActivity.this, recyclerFolderView ,new RecyclerItemClickListener.OnItemClickListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.Q)
                                            @Override
                                            public void onItemClick(View view, int position) {

                                            }
                                            @Override
                                            public void onLongItemClick(View view, int position) {
                                                Item folderItem = (Item)view.getTag();
                                                folderItem.FolderId = 0;
                                                db.updateItem(folderItem.Id, folderItem);
                                                SystemContext.CurrentFolderItemsSet = getItemsFromDb(item.Id);
                                                reFillContentPanel(recyclerFolderView, SystemContext.CurrentFolderItemsSet);
                                            }
                                        }));
                                //set buttons
                                Button flowButton = (Button) dialog.findViewById(R.id.flow_folder_button);
                                flowButton.setOnClickListener(view1 -> {
                                    Intent i = new Intent(MainContentActivity.this, FolderAddItemActivity.class);
                                    registerForAR.launch(i);
                                });

                                Button hideButton = (Button) dialog.findViewById(R.id.hide_folder_btn);
                                hideButton.setOnClickListener(v -> {
                                    dialog.cancel();
                                    setInitialData();
                                    reFillContentPanel(recyclerView, items);
                                });

                                //now that the dialog is set up, it's time to show it
                                dialog.show();
                            }
                        }
                    }
                    @Override public void onLongItemClick(View view, int position) {
                        MotionLayout ml = findViewById(R.id.motion_layout);
                        ml.setTransition(R.id.transGoSelect);
                        ml.transitionToEnd();
                        isSelectMode = true;

                        Item item = (Item) view.getTag();
                        int newItemId = SystemContext.CurrentItemsSet.indexOf(item);
                        item.isSelected = 1;
                        selectedItemsNum = 1;
                        ((TextView)findViewById(R.id.top_select_picked_txt)).setText("Selected: "+ selectedItemsNum);

                        SystemContext.CurrentItemsSet.set(newItemId, item);
                        view.setTag(item);

                        reFillContentPanel(recyclerView, SystemContext.CurrentItemsSet);
                    }
                })
        );
        Button flowBtn = findViewById(R.id.flow_button);
        flowBtn.setOnClickListener(view -> openAddMenuClick(view));
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
            items.add(item);
        }
        SystemContext.CurrentItemsSet = items;
    }
    private ArrayList<Item> getItemsFromDb(int id) {
        ArrayList <Item> folderItems = new ArrayList<>();

        Cursor cur = db.getItemsByFolder(id);
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
            folderItems.add(item);
        }
        return folderItems;
    }

    private void setViewsTagOff() {
        if(findViewById(R.id.checkTranitionState).getAlpha()==0.0) {
            findViewById(R.id.flow_button).setTag("off");
            findViewById(R.id.menubar_options).setTag("off");
            (findViewById(R.id.menubar_options)).setEnabled(true);
            (findViewById(R.id.flow_button)).setEnabled(true);
        }
    }

    public void makeFolderClick(View view) {
        EditText fname = findViewById(R.id.folderNameTxt);
        if(fname.getText().toString().equals(""))
            return;
        db.insertItem(new Item(0,fname.getText().toString(), "Папка", null, 0, 0, 0, 0, 0));
        setInitialData();
        reFillContentPanel(recyclerView,items);
        cancelMakeFolderClick(view);

    }

    public void goAccountClick(View view) {
        startActivity(new Intent(MainContentActivity.this, AccountActivity.class));
    }

    public void goPatternClick(View view) {
        startActivity(new Intent(MainContentActivity.this, INNPatternActivity.class));
    }

    public void openSortMenuClick(View view) {
        setViewsTagOff();
        MotionLayout ml = findViewById(R.id.motion_layout);
        if(view.getTag().toString() =="off") {
            ml.setTransition(R.id.trans3);
            ml.transitionToEnd();
            view.setTag("on");
            (findViewById(R.id.flow_button)).setEnabled(false);
        }
        else{
            ml.setTransition(R.id.trans4);
            ml.transitionToEnd();
            view.setTag("off");
            (findViewById(R.id.flow_button)).setEnabled(true);
        }
    }

    public void openAddMenuClick(View view) {
        setViewsTagOff();
        MotionLayout ml = findViewById(R.id.motion_layout);
        if(view.getTag().toString() =="off") {
            ml.setTransition(R.id.trans1);
            ml.transitionToEnd();
            view.setTag("on");
            (findViewById(R.id.menubar_options)).setEnabled(false);
        }
        else{
            ml.setTransition(R.id.trans2);
            ml.transitionToEnd();
            view.setTag("off");
            (findViewById(R.id.menubar_options)).setEnabled(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortFolderClick(View view) {
        ArrayList<Item> _items = new ArrayList<>(items.stream().filter((x)->x.Type.equals("Папка")).collect(Collectors.toList()));
        reFillContentPanel(recyclerView,_items);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortCardClick(View view) {
        ArrayList<Item> _items = new ArrayList<>(items.stream().filter((x)->x.Type.equals("Карта")).collect(Collectors.toList()));
        reFillContentPanel(recyclerView,_items);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortImageClick(View view) {
        ArrayList<Item> _items = new ArrayList<>(items.stream().filter((x)->x.Type.equals("Картинка")).collect(Collectors.toList()));
        reFillContentPanel(recyclerView,_items);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortDocClick(View view) {
        ArrayList<Item> _items = new ArrayList<>(items.stream()
                .filter((x)->!x.Type.equals("Картинка")& !x.Type.equals("Карта")& !x.Type.equals("Папка"))
                .collect(Collectors.toList()));
        reFillContentPanel(recyclerView,_items);
    }
    void reFillContentPanel(RecyclerView _recyclerView,ArrayList<Item> _items){
        _recyclerView.removeAllViews();
        // создаем адаптер
        ItemAdapter adapter = new ItemAdapter(this, _items, isSelectMode);
        // устанавливаем для списка адаптер
        _recyclerView.setAdapter(adapter);
    }
    public void sortCancelClick(View view) {
        reFillContentPanel(recyclerView, items);
    }

    public void menuMakeFolderClick(View view) {
        setViewsTagOff();
        MotionLayout ml = findViewById(R.id.motion_layout);
        ml.setTransition(R.id.trans5);
        ml.transitionToEnd();
        (findViewById(R.id.menubar_options)).setEnabled(false);
    }

    public void cancelMakeFolderClick(View view) {
        MotionLayout ml = findViewById(R.id.motion_layout);
        ml.setTransition(R.id.trans6);
        ml.transitionToEnd();
        (findViewById(R.id.menubar_options)).setEnabled(true);
    }

    public void bottomPinClick(View view) {
        for (Item x:
                SystemContext.CurrentItemsSet) {
            if(x.isSelected == 1) {
                x.Priority = (x.Priority==1)?0:1;
                x.isSelected = 0;
                selectedItemsNum--;

                db.updateItem(x.Id, x);
            }
        }
        setInitialData();
        SystemContext.CurrentItemsSet = items;
        reFillContentPanel(recyclerView,items);
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText("Selected: "+ selectedItemsNum);
    }

    public void bottomHideClick(View view) {
        for (Item x:
                SystemContext.CurrentItemsSet) {
            if(x.isSelected == 1) {
                x.isHiden = 1;
                x.isSelected = 0;
                selectedItemsNum--;
                db.updateItem(x.Id, x);
            }
        }
        setInitialData();
        SystemContext.CurrentItemsSet = items;
        reFillContentPanel(recyclerView,items);
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText("Selected: "+ selectedItemsNum);
    }

    public void bottomDeleteClick(View view) {
        for (Item x:
             SystemContext.CurrentItemsSet) {
            if(x.isSelected == 1){
                db.deleteItem(x.Id);
                selectedItemsNum--;
            }
        }
        setInitialData();
        SystemContext.CurrentItemsSet = items;
        reFillContentPanel(recyclerView,items);
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText("Selected: "+ selectedItemsNum);
    }

    public void topSelectAllClick(View view) {
        selectedItemsNum = 0;
        for (Item x:
                SystemContext.CurrentItemsSet) {
            int newItemId = SystemContext.CurrentItemsSet.indexOf(x);
            x.isSelected = 1;
            SystemContext.CurrentItemsSet.set(newItemId,x);
            selectedItemsNum++;
        }
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText("Selected: "+ selectedItemsNum);

        reFillContentPanel(recyclerView,SystemContext.CurrentItemsSet);
    }

    public void topSelectBackClick(View view) {
        MotionLayout ml = findViewById(R.id.motion_layout);
        ml.setTransition(R.id.transGoSelect);
        ml.transitionToStart();
        isSelectMode = false;
        reFillContentPanel(recyclerView,items);
    }
@Override
    public void onBackPressed(){
    setInitialData();
    reFillContentPanel(recyclerView, items);
    super.onBackPressed();
}
}