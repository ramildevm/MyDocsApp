package com.example.mydocsapp.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import android.database.SQLException;

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

    public static String getDbPath() {
        return DB_PATH;
    }
    public static String getDbName() {
        return DB_NAME;
    }
    public DBHelper(Context context) {
        super(context, DB_NAME, null, SCHEMA);
        this.myContext = context;
        DB_PATH = context.getFilesDir().getPath() +"/"+ DB_NAME;
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
        long result = db.update("UserSettings",cv,"id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Cursor getItems(){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item order by Priority desc",null);
        return cursor;
    }

    //*********************************************************************************************
    //UserData Contex
    public Boolean insertUserStat(int id, int lvlcount, int fails, int hardlvl){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("id",id);
        cv.put("lvlcount",lvlcount);
        cv.put("fails",fails);
        cv.put("hardlvl",hardlvl);
        long result = db.insert("UserStat",null,cv);
        if (result ==-1)
            return false;
        else
            return true;
    }
    public Boolean updateUserStat(int id, int lvlcount, int fails, int hardlvl){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("lvlcount",lvlcount);
        cv.put("fails",fails);
        cv.put("hardlvl",hardlvl);
        long result = db.update("UserStat",cv,"id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Cursor getUserStat(){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from UserStat where id=1",null);
        return cursor;
    }
    //*********************************************************************************************
    //Levels Context
    public Boolean insertLevel(int num, int fails, int scenes, String difficulty){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("num",num);
        cv.put("fails",fails);
        cv.put("scenes",scenes);
        cv.put("difficulty",difficulty);
        long result = db.insert("Levels",null,cv);
        if (result <=0)
            return false;
        else
            return true;
    }
    public Boolean updateLevel(int num, int fails){
        SQLiteDatabase db = open();
        Cursor res = db.rawQuery("select * from Levels where num=?",new String[]{""+num});
        res.moveToNext();

        ContentValues cv = new ContentValues();
        cv.put("num",num);
        cv.put("fails",fails);
        cv.put("scenes",res.getInt(2));
        cv.put("difficulty",res.getString(3));
        long result = db.update("Levels",cv,"num=?", new String[]{""+num});
        if (result ==-1)
            return false;
        else
            return true;
    }
    public Cursor getLevel(int id){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Levels where num=?",new String[]{""+id});
        return cursor;
    }
    public Cursor getMaxLevel() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Levels order by fails desc",null);
        return cursor;
    }
    //*********************************************************************************************
    //Scenes Context
    public Boolean insertScene(int id, int layout, int transition, int correctMS, int secondMS,
                               int thirdMS, String correct, String second, String third, int idML){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("id",id);
        cv.put("layout",layout);
        cv.put("transition",transition);
        cv.put("correctMS",correctMS);
        cv.put("secondMS",secondMS);
        cv.put("thirdMS",thirdMS);
        cv.put("correct",correct);
        cv.put("second",second);
        cv.put("third",third);
        cv.put("idML",idML);
        long result = db.insert("Scenes",null,cv);
        if (result ==-1)
            return false;
        else
            return true;
    }
    public Boolean updateScene(int id, int layout, int transition, int correctMS, int secondMS,
                               int thirdMS, String correct, String second, String third, int idML){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("id",id);
        cv.put("layout",layout);
        cv.put("transition",transition);
        cv.put("correctMS",correctMS);
        cv.put("secondMS",secondMS);
        cv.put("thirdMS",thirdMS);
        cv.put("correct",correct);
        cv.put("second",second);
        cv.put("third",third);
        cv.put("idML",idML);;
        long result = db.update("Scenes",cv,"id=?", new String[]{""+id});
        if (result ==-1)
            return false;
        else
            return true;
    }
    public Cursor getScenes(){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Scenes",null);
        return cursor;
    }
    public Cursor getScene(int id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Scenes where id=?", new String[]{"" + id});
        return cursor;
    }
    //*********************************************************************************************
    //LevelScene Context
    public Boolean insertLevelScene(int id, int lvlid, int sceneid, int priority){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("id",id);
        cv.put("idlvl",lvlid);
        cv.put("idscene",sceneid);
        cv.put("priority",priority);
        long result = db.insert("LevelScene",null,cv);
        if (result ==-1)
            return false;
        else
            return true;
    }
    public Cursor getLevelScene(int lvlid){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from LevelScene where idlvl=? order by priority",
                new String[]{""+lvlid});
        return cursor;
    }
}

