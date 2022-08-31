package com.example.mydocsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydocsapp.models.DBHelper;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.SystemContext;
import com.santalu.maskara.widget.MaskEditText;

public class MainPassportPatternActivity extends AppCompatActivity {
DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_passport_pattern);
        db = new DBHelper(this);
        setDataFromDb();
        if(SystemContext.CurrentItem  == null) {
            ((TextView) findViewById(R.id.passport_txt)).setOnClickListener(v -> {
                if (v.getTag().equals("off")) {
                    MotionLayout ml = findViewById(R.id.motion_layout);
                    ml.setTransition(R.id.transTop);
                    ml.transitionToEnd();
                    v.setTag("on");
                } else if (v.getTag().equals("on")) {
                    MotionLayout ml = findViewById(R.id.motion_layout);
                    ml.setTransition(R.id.transTop);
                    ml.transitionToStart();
                    v.setTag("off");
                }
            });
        }
    }

    private void setDataFromDb() {
        Item item = SystemContext.CurrentItem;
        if(item!= null) {
            Cursor cur = db.getPassportById(item.ObjectId);
            cur.moveToFirst();
            ((MaskEditText) findViewById(R.id.editTextSeriesNumber)).setText(cur.getString(1));
            ((MaskEditText) findViewById(R.id.editTextDivisionCode)).setText(cur.getString(2));
            ((MaskEditText) findViewById(R.id.editTextDateIssue)).setText(cur.getString(3));
            ((EditText) findViewById(R.id.editTextIssuedWhom)).setText(cur.getString(4));
            ((EditText) findViewById(R.id.editTextFullName)).setText(cur.getString(5));
            ((MaskEditText) findViewById(R.id.editTextDateBirth)).setText(cur.getString(6));
            if(cur.getString(7).equals("M"))
                ((RadioButton) findViewById(R.id.maleCheck)).setChecked(true);
            else
                ((RadioButton) findViewById(R.id.femaleCheck)).setChecked(true);
            ((EditText) findViewById(R.id.editTextPlaceBirth)).setText(cur.getString(8));
            ((EditText) findViewById(R.id.editTextPlaceResidence)).setText(cur.getString(9));
        }
    }

    public void goBackMainPageClick(View view) {
        MaskEditText editTextSN = findViewById(R.id.editTextSeriesNumber);
        Passport p = new Passport(0,
                editTextSN.getText().toString(),
                ((MaskEditText) findViewById(R.id.editTextDivisionCode)).getText().toString(),
                ((MaskEditText) findViewById(R.id.editTextDateIssue)).getText().toString(),
                ((EditText) findViewById(R.id.editTextIssuedWhom)).getText().toString(),
                ((EditText) findViewById(R.id.editTextFullName)).getText().toString(),
                ((MaskEditText) findViewById(R.id.editTextDateBirth)).getText().toString(),
                "M",
                ((EditText) findViewById(R.id.editTextPlaceBirth)).getText().toString(),
                ((EditText) findViewById(R.id.editTextPlaceResidence)).getText().toString(),
                null,
                null,
                null
        );
        if(((RadioButton) findViewById(R.id.maleCheck)).isChecked())
            p.Gender = "M";
        else
            p.Gender = "F";
        if(SystemContext.CurrentItem == null){
            db.insertPassport(p);
            int id = Integer.parseInt(db.selectLastId());
            db.insertItem(new Item(0, "Паспорт", "Паспорт", null, 0, 0, 0, id, 0));
        }
        else{
            db.updatePassport(SystemContext.CurrentItem.ObjectId, p);
        }
        startActivity(new Intent(MainPassportPatternActivity.this, MainContentActivity.class));
    }

    public void goSecondPagePassportPatternClick(View view) {
        startActivity(new Intent(MainPassportPatternActivity.this, SecondPassportPatternActivity.class).putExtra("Login", getString(R.string.extra_guest)));
    }

    public void goInnClick(View view) {
        startActivity(new Intent(MainPassportPatternActivity.this, INNPatternActivity.class));
    }

    public void goPolicyClick(View view) {
        startActivity(new Intent(MainPassportPatternActivity.this, PolicyPatternActivity.class));
    }
}