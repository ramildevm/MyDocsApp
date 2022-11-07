package com.example.mydocsapp.models;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mydocsapp.api.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends SQLiteOpenHelper {
    private static String DB_PATH = null; // полный путь к базе данных
    private static String DB_NAME = "UserDB.db";
    private static final int SCHEMA = 1; // версия базы данных
    private Context myContext;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, SCHEMA);
        this.myContext = context;
        context.getFilesDir().mkdir();
        DB_PATH = context.getFilesDir().getPath() + DB_NAME;
    }
    @Override
    public void onCreate(SQLiteDatabase db) { }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) { }

    public void create_db() throws IOException{
        File file = new File(DB_PATH);
        if (!file.exists()) {
            //получаем локальную бд как поток
            InputStream is = myContext.getAssets().open(DB_NAME);
            OutputStream fos = new FileOutputStream(DB_PATH);
            int length = 0;
            byte[] buffer = new byte[1024];

            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            fos.close();
            is.close();
        }
    }
    public SQLiteDatabase open()throws SQLException {
        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH;
            File file = new File(myPath);
            file.setWritable(true);
            if (file.exists() && !file.isDirectory())
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }
    //*********************************************************************************************
    //Item table
    public Boolean insertItem( Item item){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Title",item.Title);
        cv.put("Type",item.Type);
        cv.put("Image",item.Image);
        cv.put("Priority",item.Priority);
        cv.put("isHiden",item.isHiden);
        cv.put("FolderId",item.FolderId);
        cv.put("ObjectId",item.ObjectId);
        cv.put("isSelected",item.isSelected);
        long result = db.insert("Item",null,cv);
        if (result ==-1)
            return false;
        else
            return true;
    }
    public Boolean updateItem(int id, Item item){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Title",item.Title);
        cv.put("Type",item.Type);
        cv.put("Image",item.Image);
        cv.put("Priority",item.Priority);
        cv.put("isHiden",item.isHiden);
        cv.put("FolderId",item.FolderId);
        cv.put("ObjectId",item.ObjectId);
        cv.put("isSelected",item.isSelected);
        long result = db.update("Item",cv,"id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Cursor getItems(){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=0 order by Priority desc, id asc",null);
        return cursor;
    }
    public Cursor getItemsByFolder(int id){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=0 and FolderId=? order by Priority desc, id asc",new String[]{""+id});
        return cursor;
    }
    public Cursor getItemsByFolder0(){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=0 and FolderId=0 order by Priority desc, id asc", null);
        return cursor;
    }
    public Cursor getItemsByFolderIdForAdding(int id){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=0 and FolderId=0 or FolderId=? order by Priority desc, id asc",new String[]{""+id});
        return cursor;
    }
    public int getItemFolderItemsCount(int id){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where FolderId=? order by Priority desc, id asc", new String[]{""+id});
        return cursor.getCount();
    }
    public Boolean deleteItem(int id){
        SQLiteDatabase db = open();
        long result = db.delete("Item","id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    //*********************************************************************************************
    //Passport table
    public Cursor getPassportById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Passport where Id=?", new String[]{""+objectId});
        return cursor;
    }
    public Boolean deletePassport(int id){
        SQLiteDatabase db = open();
        long result = db.delete("Passport","id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Boolean updatePassport(int id, Passport passport){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("SeriaNomer",passport.SeriaNomer);
        cv.put("DivisionCode",passport.DivisionCode);
        cv.put("GiveDate",passport.GiveDate);
        cv.put("ByWhom",passport.ByWhom);
        cv.put("FIO",passport.FIO);
        cv.put("BirthDate",passport.BirthDate);
        cv.put("Gender",passport.Gender);
        cv.put("BirthPlace",passport.BirthPlace);
        cv.put("ResidencePlace",passport.ResidencePlace);
        cv.put("FacePhoto",passport.FacePhoto);
        cv.put("PhotoPage1",passport.PhotoPage1);
        cv.put("PhotoPage2",passport.PhotoPage2);
        long result = db.update("Passport",cv,"id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Boolean insertPassport(Passport passport){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("SeriaNomer",passport.SeriaNomer);
        cv.put("DivisionCode",passport.DivisionCode);
        cv.put("GiveDate",passport.GiveDate);
        cv.put("ByWhom",passport.ByWhom);
        cv.put("FIO",passport.FIO);
        cv.put("BirthDate",passport.BirthDate);
        cv.put("Gender",passport.Gender);
        cv.put("BirthPlace",passport.BirthPlace);
        cv.put("ResidencePlace",passport.ResidencePlace);
        cv.put("FacePhoto",passport.FacePhoto);
        cv.put("PhotoPage1",passport.PhotoPage1);
        cv.put("PhotoPage2",passport.PhotoPage2);
        long result = db.insert("Passport",null,cv);
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    @SuppressLint("Range")
    public String selectLastId(){
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("SELECT * FROM SQLITE_SEQUENCE where name=?",new String[]{"Passport"});
        cur.moveToFirst();
        String id = cur.getString(cur.getColumnIndex("seq"));
        return id;
    }

    //*********************************************************************************************
    //User table

    public Cursor getUserById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from User where Id=?", new String[]{""+objectId});
        return cursor;
    }
    public Boolean deleteUser(int id){
        SQLiteDatabase db = open();
        long result = db.delete("User","id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Boolean updateUser(int id, User user){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Login",user.login);
        cv.put("Password",user.password);
        long result = db.update("User",cv,"id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Boolean insertUser(User user){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Login",user.login);
        cv.put("Password",user.password);
        long result = db.insert("User",null,cv);
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
}

