package com.example.mydocsapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mydocsapp.models.DBHelper;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.ItemAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainContentActivity extends AppCompatActivity {
    ArrayList<Item> items = new ArrayList<Item>();
    RecyclerView recyclerView;
    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);
        Intent intent = getIntent();
        TextView gtxt = findViewById(R.id.login_txt);
        gtxt.setText(intent.getStringExtra("Login"));
        // начальная инициализация списка
        // создаем базу данных
        db = new DBHelper(this);
        try {
            db.create_db();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        setInitialData();
        setViewsTagOff();
        recyclerView = findViewById(R.id.container);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // создаем адаптер
        ItemAdapter adapter = new ItemAdapter(this, items);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);

        Button flowBtn = findViewById(R.id.flow_button);
        flowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddMenuClick(view);
            }
        });
    }

    private void setInitialData() {
        items.clear();
        Cursor cur = db.getItems();
        Item item;
        while(cur.moveToNext()){
            item = new Item(cur.getString(1),
                    cur.getString(2),
                    cur.getBlob(3),
                    cur.getInt(4),
                    cur.getInt(5),
                    cur.getInt(6),
                    cur.getInt(7),
                    cur.getInt(8));
            items.add(item);
        }
    }

    private void setViewsTagOff() {
        if(findViewById(R.id.checkTranitionState).getAlpha()==0.0) {
            findViewById(R.id.flow_button).setTag("off");
            findViewById(R.id.menubar_options).setTag("off");
            (findViewById(R.id.menubar_options)).setEnabled(true);
            (findViewById(R.id.flow_button)).setEnabled(true);
        }
    }
    public void itemPanelClick(View view) {
        view.setBackgroundColor(getResources().getColor(R.color.purple_200));
    }

    public void makeFolderClick(View view) {
        EditText fname = findViewById(R.id.folderNameTxt);
        if(fname.getText().toString().equals(""))
            return;
        db.insertItem(new Item(fname.getText().toString(), "Папка", null, 0, 0, 0, 0, 0));
        setInitialData();
        reFillContentPanel(items);
        cancelMakeFolderClick(view);

//        RecyclerView contentPanel = findViewById(R.id.container);
//        LinearLayout l1 = new LinearLayout(this);
//        int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());
//        int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 195, getResources().getDisplayMetrics());
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width,height);
//        int marginpx = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
//        layoutParams.setMargins(marginpx,marginpx,marginpx,marginpx);
//        l1.setLayoutParams(layoutParams);
//        l1.setBackgroundColor(getResources().getColor(R.color.black));
//        contentPanel.addView(l1);
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
        reFillContentPanel(_items);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortCardClick(View view) {
        ArrayList<Item> _items = new ArrayList<>(items.stream().filter((x)->x.Type.equals("Карта")).collect(Collectors.toList()));
        reFillContentPanel(_items);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortImageClick(View view) {
        ArrayList<Item> _items = new ArrayList<>(items.stream().filter((x)->x.Type.equals("Картинка")).collect(Collectors.toList()));
        reFillContentPanel(_items);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortDocClick(View view) {
        ArrayList<Item> _items = new ArrayList<>(items.stream()
                .filter((x)->!x.Type.equals("Картинка")& !x.Type.equals("Карта")& !x.Type.equals("Папка"))
                .collect(Collectors.toList()));
        reFillContentPanel(_items);
    }
    void reFillContentPanel(ArrayList<Item> _items){
        recyclerView.removeAllViews();
        // создаем адаптер
        ItemAdapter adapter = new ItemAdapter(this, _items);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
    }
    public void sortCancelClick(View view) {
        reFillContentPanel(items);
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
}