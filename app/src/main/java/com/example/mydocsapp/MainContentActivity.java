package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.ItemAdapter;

import java.util.ArrayList;

public class MainContentActivity extends AppCompatActivity {
    ArrayList<Item> items = new ArrayList<Item>();
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);
        Intent intent = getIntent();
        TextView gtxt = findViewById(R.id.login_txt);
        gtxt.setText(intent.getStringExtra("Login"));
        // начальная инициализация списка
        setInitialData();
        recyclerView = findViewById(R.id.container);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // создаем адаптер
        ItemAdapter adapter = new ItemAdapter(this, items);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
    }

    private void setInitialData() {
        items.add(new Item(1,"Паспорт",R.drawable.passport_image, false, "Паспорт"));
        items.add(new Item(2,"Снилс",R.drawable.passport_image, false, "Снилс"));
        items.add(new Item(3,"Полис",R.drawable.passport_image, false, "Полис"));
    }

    public void makeFolderClick(View view) {
        items.add(new Item(items.size()+1,"Новая папка", 0, false, "Папка"));
        recyclerView.removeAllViews();
        // создаем адаптер
        ItemAdapter adapter = new ItemAdapter(this, items);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);

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

    public void goAccountClick(View view) {
        startActivity(new Intent(MainContentActivity.this, AccountActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }

    public void goPatternClick(View view) {
        startActivity(new Intent(MainContentActivity.this, MainPassportPatternActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }
}