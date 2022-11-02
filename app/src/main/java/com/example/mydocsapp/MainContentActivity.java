package com.example.mydocsapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydocsapp.api.User;
import com.example.mydocsapp.apputils.RecyclerItemClickListener;
import com.example.mydocsapp.interfaces.ItemAdapterActivity;
import com.example.mydocsapp.models.DBHelper;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.ItemAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainContentActivity extends AppCompatActivity implements ItemAdapterActivity {
    ArrayList<Item> items = new ArrayList<Item>();
    RecyclerView recyclerView;
    String filterOption = "All";
    DBHelper db;
    boolean isSelectMode;
    boolean isSortMode = false;
    private int selectedItemsNum = 0;
    private ActivityResultLauncher<Intent> registerForAR;
    private RecyclerView recyclerFolderView;
    Dialog makeRenameDialog;
    ItemAdapter adapter;

    private User CurrentUser;
    private Item CurrentItem;
    private ArrayList<Item> CurrentItemsSet;
    private ArrayList<Item> CurrentFolderItemsSet;
    private boolean isTitleClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);

        registerForAR = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                int resultCode = result.getResultCode();
                CurrentFolderItemsSet = getItemsFromDb(CurrentItem.Id);
                reFillContentPanel(recyclerFolderView, CurrentFolderItemsSet);
                setInitialData();
                reFillContentPanel(recyclerView, items);
            }
        });
        CurrentItem = null;
        CurrentFolderItemsSet = null;
        isTitleClicked = false;

        // начальная инициализация списка
        // создаем базу данных
        db = new DBHelper(this);
        Cursor cur = db.getUserById(1);
        cur.moveToFirst();
        CurrentUser = new User(cur.getString(1),cur.getString(2));

        isSelectMode = false;
        TextView gtxt = findViewById(R.id.login_txt);
        gtxt.setText((CurrentUser.login));

        setInitialData();
        setSelectedPropertyZero();
        setViewsTagOff();
        recyclerView = findViewById(R.id.container);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // создаем адаптер
        adapter = new ItemAdapter(this, items, isSelectMode);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);

        int spanCount = 2; // 2 columns
        int spacing = 20; // 10px
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new com.example.mydocsapp.GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        setOnClickListeners();
    }

    private void setSelectedPropertyZero() {
        for (Item x: items) {
            x.isSelected = 0;
            db.updateItem(x.Id, x);
        }
    }
    private ArrayList<String> selectedItemsSet = new ArrayList<>();
    private void setOnClickListeners() {
        recyclerView.addOnItemTouchListener(
            new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override public void onItemClick(View view, int position) {
                    if (findViewById(R.id.container).getAlpha()==0.5f)
                        return;
                    Item item = (Item) view.getTag();
                    if(isSelectMode) {
                        int newItemId = CurrentItemsSet.indexOf(item);
                        if (item.isSelected == 0) {
                            item.isSelected = 1;
                            selectedItemsNum++;
                            selectedItemsSet.add(position + "");
                        } else {
                            item.isSelected = 0;
                            selectedItemsNum--;
                            selectedItemsSet.remove(position + "");
                        }
                        view.setTag(item);
                        CurrentItemsSet.set(position, item);
                        ((TextView)findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) +" "+ selectedItemsNum);
                        reFillContentPanel(recyclerView, CurrentItemsSet);
                        
                    }
                    else{
                        CurrentItem = item;
                        if(isTitleClicked){
                            makeRenameDialogMethod(recyclerView,items,"Rename");
                            makeRenameDialog.show();
                        }
                        else {
                            if (item.Type.equals("Папка")) {
                                Dialog dialog = new Dialog(MainContentActivity.this);
                                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                dialog.setContentView(R.layout.folder_items_panel_layout);
                                dialog.getWindow().setGravity(Gravity.TOP);
                                dialog.setCancelable(true);

                                int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
                                int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
                                dialog.getWindow().setLayout(width, height);

                                //set up text
                                TextView text = (TextView) dialog.findViewById(R.id.title_folder_txt);
                                text.setText(item.Title);

                                //items set
                                CurrentFolderItemsSet = getItemsFromDb(item.Id);

                                recyclerFolderView = (RecyclerView) dialog.findViewById(R.id.folder_container);

                                recyclerFolderView.setLayoutManager(new GridLayoutManager(MainContentActivity.this, 2));
                                // создаем адаптер
                                ItemAdapter adapter = new ItemAdapter(MainContentActivity.this, CurrentFolderItemsSet, false);
                                // устанавливаем для списка адаптер
                                recyclerFolderView.setAdapter(adapter);
                                recyclerFolderView.addOnItemTouchListener(
                                    new RecyclerItemClickListener(MainContentActivity.this, recyclerFolderView, new RecyclerItemClickListener.OnItemClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.Q)
                                        @Override
                                        public void onItemClick(View view, int position) {
                                            CurrentItem = (Item) view.getTag();
                                            if(isTitleClicked){
                                                makeRenameDialogMethod(recyclerFolderView, CurrentFolderItemsSet, "FRename");
                                                makeRenameDialog.show();
                                            }
                                            else if (CurrentItem.Type.equals("Паспорт")) {
                                                goPatternClick(view);
                                            }
                                        }
                                        @Override
                                        public void onLongItemClick(View view, int position) {
                                            Item folderItem = (Item) view.getTag();
                                            folderItem.FolderId = 0;
                                            db.updateItem(folderItem.Id, folderItem);
                                            CurrentFolderItemsSet = getItemsFromDb(item.Id);
                                            reFillContentPanel(recyclerFolderView, CurrentFolderItemsSet);
                                        }
                                    }));
                                //set buttons
                                Button flowButton = (Button) dialog.findViewById(R.id.flow_folder_button);
                                flowButton.setOnClickListener(view1 -> {
                                    Intent i = new Intent(MainContentActivity.this, FolderAddItemActivity.class);
                                    CurrentItem = item;
                                    i.putExtra("item",CurrentItem);
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
                            } else if (item.Type.equals("Паспорт")) {
                                goPatternClick(view);
                            }
                        }
                    }
                }
                @Override public void onLongItemClick(View view, int position) {
                if(!isSortMode) {
                    selectedItemsSet = new ArrayList<>();
                    selectedItemsSet.add(position + "");
                    if (findViewById(R.id.container).getAlpha()==0.5f)
                        return;
                    MotionLayout ml = findViewById(R.id.motion_layout);
                    ml.setTransition(R.id.transGoSelect);
                    ml.transitionToEnd();
                    isSelectMode = true;

                    Item item = (Item) view.getTag();
                    int newItemId = CurrentItemsSet.indexOf(item);
                    item.isSelected = 1;
                    selectedItemsNum = 1;
                    ((TextView) findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) +" "+ + selectedItemsNum);

                    CurrentItemsSet.set(newItemId, item);
                    view.setTag(item);

                    reFillContentPanel(recyclerView, CurrentItemsSet);
                }
            }
            })
        );
        Button flowBtn = findViewById(R.id.flow_button);
        flowBtn.setOnClickListener(view -> openAddMenuClick(view));
    }

    private void makeRenameDialogMethod(RecyclerView recyclerView, ArrayList<Item> items, String mode) {
        makeRenameDialog = new Dialog(MainContentActivity.this);
        makeRenameDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        makeRenameDialog.setContentView(R.layout.make_rename_layout);
        makeRenameDialog.setCancelable(true);
        makeRenameDialog.getWindow().getAttributes().windowAnimations = R.style.MakeRenameDialogAnimation;

        EditText editText = makeRenameDialog.findViewById(R.id.folderNameTxt);
        editText.setSelection(editText.getText().length());
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        if(mode.equals("Make")){
            ((Button)makeRenameDialog.findViewById(R.id.make_rename_item_btn)).setText(R.string.create_a_folder);
            editText.setText("");
        }
        else {
            ((Button)makeRenameDialog.findViewById(R.id.make_rename_item_btn)).setText(R.string.rename_an_item);
            editText.append(CurrentItem.Title);
        }
        makeRenameDialog.findViewById(R.id.make_rename_item_btn).setOnClickListener(view2 ->{
            if (editText.getText().toString().equals(""))
                return;
            if(mode.equals("Make")) {
                db.insertItem(new Item(0, editText.getText().toString(), "Папка", null, 0, 0, 0, 0, 0));
            }
            else{
                Item item = CurrentItem;
                item.Title = editText.getText().toString();
                db.updateItem(item.Id,item);
            }
            if(mode.equals("Rename") | mode.equals("Make")) {
                setInitialData();
                reFillContentPanel(recyclerView, items);
            }
            else if(mode.equals("FRename")) {
                CurrentFolderItemsSet = getItemsFromDb(CurrentItem.FolderId);
                reFillContentPanel(recyclerView, CurrentFolderItemsSet);
            }
            isTitleClicked = false;
            CurrentItem = null;
            makeRenameDialog.dismiss();
        });
        makeRenameDialog.findViewById(R.id.cancel_btn).setOnClickListener(view12 -> {
            CurrentItem = null;
            isTitleClicked = false;
            makeRenameDialog.dismiss();
        });
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
        CurrentItemsSet = items;
        CurrentItem = null;
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
    public void goAccountClick(View view) {
        startActivity(new Intent(MainContentActivity.this, AccountActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.alpha_out);
    }

    public void goPatternClick(View view) {
        Intent intent = new Intent(MainContentActivity.this, MainPassportPatternActivity.class);
        intent.putExtra("item",CurrentItem);
        startActivity(intent);
    }

    public void openSortMenuClick(View view) {
        setViewsTagOff();
        MotionLayout ml = findViewById(R.id.motion_layout);
        if(view.getTag().toString() =="off") {
            isSortMode = true;
            ml.setTransition(R.id.trans3);
            ml.transitionToEnd();
            view.setTag("on");
            (findViewById(R.id.flow_button)).setEnabled(false);
        }
        else{
            isSortMode = false;
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
            //ml.setTransition(R.id.trans2);
            ml.transitionToStart();
            view.setTag("off");
            (findViewById(R.id.menubar_options)).setEnabled(true);
        }
    }

    public void menuMakeFolderClick(View view) {
        makeRenameDialogMethod(recyclerView,items,"Make");
        makeRenameDialog.show();
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
        adapter = new ItemAdapter(this, _items, isSelectMode);
        // устанавливаем для списка адаптер
        _recyclerView.setAdapter(adapter);
    }
    public void sortCancelClick(View view) {
        reFillContentPanel(recyclerView, items);
    }
    public void bottomPinClick(View view) {
        for (Item x:
                CurrentItemsSet) {
            if(x.isSelected == 1) {
                x.Priority = (x.Priority==1)?0:1;
                x.isSelected = 0;
                selectedItemsNum--;

                db.updateItem(x.Id, x);
            }
        }
        setInitialData();
        CurrentItemsSet = items;
        reFillContentPanel(recyclerView,items);
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) +" "+ selectedItemsNum);
    }

    public void bottomHideClick(View view) {
        for (Item x:
                CurrentItemsSet) {
            if(x.isSelected == 1) {
                x.isHiden = 1;
                x.isSelected = 0;
                selectedItemsNum--;
                db.updateItem(x.Id, x);
            }
        }
        setInitialData();
        CurrentItemsSet = items;
        reFillContentPanel(recyclerView,items);
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) +" "+ selectedItemsNum);
    }

    public void bottomDeleteClick(View view) {
        for (Item x: CurrentItemsSet) {
            if(x.isSelected == 1){
                db.deleteItem(x.Id);
                selectedItemsNum--;
                if(x.ObjectId!=0){
                    if(x.Type.equals("Паспорт"))
                        db.deletePassport(x.ObjectId);
                }
            }
        }
        for (String index:
             selectedItemsSet) {
            adapter.onItemDelete(Integer.valueOf(index));
        }
        setInitialData();
        CurrentItemsSet = items;

        //reFillContentPanel(recyclerView,items);
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) +" "+ selectedItemsNum);
    }

    public void topSelectAllClick(View view) {
        selectedItemsNum = 0;
        for (Item x:
                CurrentItemsSet) {
            int newItemId = CurrentItemsSet.indexOf(x);
            x.isSelected = 1;
            CurrentItemsSet.set(newItemId,x);
            selectedItemsNum++;
        }
        ((TextView)findViewById(R.id.top_select_picked_txt)).setText(getString(R.string.selected_string) +" "+ selectedItemsNum);

        reFillContentPanel(recyclerView,CurrentItemsSet);
    }

    public void topSelectBackClick(View view) {
        MotionLayout ml = findViewById(R.id.motion_layout);
        ml.setTransition(R.id.transGoSelect);
        ml.transitionToStart();
        isSelectMode = false;
        setSelectedPropertyZero();
        reFillContentPanel(recyclerView,items);
    }
    @Override
    public void onBackPressed(){
        setInitialData();
        setSelectedPropertyZero();
        reFillContentPanel(recyclerView, items);
        if(isTitleClicked){
            isTitleClicked = false;
            CurrentItem = null;
            return;
        }
        if(isSelectMode) {
            topSelectBackClick(new View(this));
        }
        else if(isSortMode){
            openSortMenuClick(findViewById(R.id.menubar_options));
        }
        else
            NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public boolean getIsTitleClicked() {
        return isTitleClicked;
    }

    @Override
    public void setIsTitleClicked(boolean value) {
        this.isTitleClicked = value;
    }
}