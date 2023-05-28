package com.example.mydocsapp.services;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mydocsapp.api.User;
import com.example.mydocsapp.models.CreditCard;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.Photo;

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
    private int UserId;

    public DBHelper(Context context, int userId) {
        super(context, DB_NAME, null, SCHEMA);
        this.myContext = context;
        this.UserId = userId;
        context.getFilesDir().mkdir();
        DB_PATH = context.getFilesDir().getPath() + DB_NAME;
    }
    @Override
    public void onCreate(SQLiteDatabase db) { }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) { }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        super.onOpen(db);
    }

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
        cv.put("DateCreation",item.DateCreation);
        cv.put("UserId",UserId);
        cv.put("isSelected",item.isSelected);
        long result = db.insert("Item",null,cv);
        if (result ==-1)
            return false;
        else
            return true;
    }

    public Boolean updateItemFolder(int id, int i) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("FolderId",i);
        cv.put("isSelected",0);
        long result = db.update("Item",cv,"id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
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
        cv.put("DateCreation",item.DateCreation);
        cv.put("UserId",UserId);
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
            Cursor cursor = db.rawQuery("select * from Item where isHiden=0 and UserId=? order by Priority desc, id asc",new String[]{this.UserId+""});
        return cursor;
    }

    public Cursor getItemsByFolder(int id, int hideMode){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=? and FolderId=? and UserId=? order by Priority desc, id asc",new String[]{hideMode+"",""+id,UserId+""});
        return cursor;
    }
    public Cursor getItemsByFolder0(){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=0 and FolderId=0 and UserId=? order by Priority desc, id asc", new String[]{this.UserId+""});
        return cursor;
    }
    public Cursor getHiddenItemsByFolder0(){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=1 and FolderId=0 and UserId=? order by Priority desc, id asc", new String[]{this.UserId+""});
        return cursor;
    }
    public Cursor getItemsByFolderIdForAdding(int id, int hideMode){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=? and UserId=? and FolderId=0 or FolderId=? order by Priority desc, id asc",new String[]{hideMode+"",UserId+"",""+id});
        return cursor;
    }
    public int getItemFolderItemsCount(int id){
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where FolderId=? and UserId=? order by Priority desc, id asc", new String[]{""+id, UserId +""});
        return cursor.getCount();
    }
    public Boolean deleteItem(int id){
        SQLiteDatabase db = open();
        db.delete("Item","Id=?", new String[]{""+id});
        db.delete("Passport","Id=?", new String[]{""+id});
        db.delete("CreditCard","Id=?", new String[]{""+id});
        db.delete("INN","Id=?", new String[]{""+id});
        db.delete("SNILS","Id=?", new String[]{""+id});
        db.delete("Polis","Id=?", new String[]{""+id});
        db.delete("Photo","CollectionId=?", new String[]{""+id});
            return true;
//        }
    }
    public Boolean deleteFolder(int id, int deleteAll){
        SQLiteDatabase db = open();
        db.delete("Item","Id=?", new String[]{""+id});
        db.delete("Item","FolderId=?", new String[]{""+id});
        return true;
//        }
    }

    @SuppressLint("Range")
    public int selectLastItemId(){
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("SELECT rowid from Item order by ROWID DESC limit 1",null);
        cur.moveToFirst();
        int id;
        if(cur!=null && cur.getCount()>0)
            id = cur.getInt(0);
        else
            id = 0;
        return id;
    }
    //*********************************************************************************************
    //Passport table
    public Cursor getPassportById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Passport where Id=?", new String[]{""+objectId});
        return cursor;
    }
    public Cursor getPassports() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Passport", null);
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
        cv.put("Id",passport.Id);
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

    //*********************************************************************************************
    //User table

    public Cursor getUserById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from User where Id=?", new String[]{""+objectId});
        return cursor;
    }
    public Cursor getUserByLogin(String login) {
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("select count(*) from User where Login=?", new String[]{login});
        if (cur != null) {
            cur.moveToFirst();                    // Always one row returned.
            if (cur.getInt(0) == 0) {               // Zero count means empty table.
                return null;
            }
        }
        cur = db.rawQuery("select * from User where Login=?", new String[]{login});
        return cur;
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
        cv.put("Login",user.Login);
        cv.put("Password",user.Password);
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
        cv.put("Login",user.Login);
        cv.put("Password",user.Password);
        cv.put("PremiumStatus",user.PremiumStatus);
        cv.put("Syncing",user.Syncing);
        cv.put("Photo",user.Photo);
        long result = db.insert("User",null,cv);
        if (result <=0)
            return false;
        else {
            return true;
        }
    }

    public int selectLastUserId() {
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("SELECT rowid from User order by ROWID DESC limit 1",null);
        cur.moveToFirst();
        int id;
        if(cur!=null && cur.getCount()>0)
            id = cur.getInt(0);
        else
            id = 0;
        return id;
    }
    //*********************************************************************************************
    //Photo table

    public Cursor getPhotos(int id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Photo where CollectionId=?", new String[]{""+id});
        return cursor;
    }
    public Boolean deletePhoto(int id){
        SQLiteDatabase db = open();
        long result = db.delete("Photo","id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Boolean updatePhoto(int id, Photo photo){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Path",photo.Path);
        cv.put("CollectionId",photo.Path);
        long result = db.update("Photo",cv,"id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Boolean insertPhoto(Photo photo){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Path",photo.Path);
        cv.put("CollectionId",photo.CollectionId);
        long result = db.insert("Photo",null,cv);
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    //*********************************************************************************************
    //CreditCard table
    public Cursor getCreditCardById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from CreditCard where Id=?", new String[]{""+objectId});
        return cursor;
    }
    public Boolean deleteCreditCard(int id){
        SQLiteDatabase db = open();
        long result = db.delete("CreditCard","id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Boolean updateCreditCard(int id, CreditCard creditCard){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Number",creditCard.Number);
        cv.put("FIO",creditCard.FIO);
        cv.put("ExpiryDate",creditCard.ExpiryDate);
        cv.put("CVV",creditCard.CVV);
        cv.put("PhotoPage1",creditCard.PhotoPage1);
        long result = db.update("CreditCard",cv,"id=?", new String[]{""+id});
        if (result <=0)
            return false;
        else {
            return true;
        }
    }
    public Boolean insertCreditCard(CreditCard creditCard){
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id",creditCard.Id);
        cv.put("Number",creditCard.Number);
        cv.put("FIO",creditCard.FIO);
        cv.put("ExpiryDate",creditCard.ExpiryDate);
        cv.put("CVV",creditCard.CVV);
        cv.put("PhotoPage1",creditCard.PhotoPage1);
        long result = db.insert("CreditCard",null,cv);
        if (result <=0)
            return false;
        else {
            return true;
        }
    }

}

