package com.example.mydocsapp.services;

import static com.example.mydocsapp.services.AppService.NULL_UUID;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mydocsapp.models.CreditCard;
import com.example.mydocsapp.models.Inn;
import com.example.mydocsapp.models.Item;
import com.example.mydocsapp.models.Passport;
import com.example.mydocsapp.models.Photo;
import com.example.mydocsapp.models.Polis;
import com.example.mydocsapp.models.Snils;
import com.example.mydocsapp.models.Template;
import com.example.mydocsapp.models.TemplateDocument;
import com.example.mydocsapp.models.TemplateDocumentData;
import com.example.mydocsapp.models.TemplateObject;
import com.example.mydocsapp.models.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        super.onOpen(db);
    }

    public void create_db() throws IOException {
        File file = new File(DB_PATH);
        if (!file.exists()) {
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

    public SQLiteDatabase open() throws SQLException {
        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }

    private String getCurrentTime() {
        long datetimeMillis = new Date().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDateTime = dateFormat.format(new Date(datetimeMillis));
        return formattedDateTime;
    }

    private void deletePhotoFile(String image) {
        if(image==null)
            return;
        File file = new File(image);
        File directory = new File(file.getAbsolutePath());
        deleteDirectory(directory);
    }
    public void deleteDirectory(File directory){
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }
    //*********************************************************************************************
    //Item table
    public UUID insertItem(Item item, boolean isNew) {
        UUID id = isNew?UUID.randomUUID():item.Id;
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", id.toString());
        cv.put("Title", item.Title);
        cv.put("Type", item.Type);
        cv.put("Image", item.Image);
        cv.put("Priority", item.Priority);
        cv.put("IsHidden", item.IsHidden);
        cv.put("FolderId", item.FolderId.toString());
        cv.put("DateCreation", item.DateCreation);
        cv.put("UserId", UserId);
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("Item", null, cv);
        if (result == -1)
            return NULL_UUID;
        else
            return id;
    }

    private List<Item> getItemsFromCursor(Cursor cur) {
        List<Item> items = new ArrayList<>();
        Item item;
        while (cur.moveToNext()) {
            item = new Item(UUID.fromString(cur.getString(0)),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4),
                    cur.getInt(5),
                    0,
                    cur.getString(6),
                    UUID.fromString(cur.getString(7)),
                    cur.getInt(8),
                    cur.getString(9));
            items.add(item);
        }
        return items;
    }

    public Boolean updateItemFolder(UUID id, UUID i) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("FolderId", i.toString());
        long result = db.update("Item", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean updateItem(UUID id, Item item, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        if (isDelete)
            item.setValuesNull();
        else
            item.UpdateTime = getCurrentTime();
        cv.put("Title", item.Title);
        cv.put("Type", item.Type);
        cv.put("Image", item.Image);
        cv.put("Priority", item.Priority);
        cv.put("IsHidden", item.IsHidden);
        cv.put("FolderId", item.FolderId.toString());
        cv.put("DateCreation", item.DateCreation);
        cv.put("UserId", UserId);
        cv.put("UpdateTime", item.UpdateTime);
        long result = db.update("Item", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    @SuppressLint("Range")
    public int selectLastItemId() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("SELECT rowid from Item order by ROWID DESC limit 1", null);
        int id;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("rowid"));
            return id;
        } else
            return 0;
    }

    public List<Item> getItemsByFolder(UUID id, int hideMode) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where UpdateTime IS NOT NULL and FolderId=? and UserId=? order by Priority desc, rowid asc", new String[]{id+"", UserId + ""});
        return getItemsFromCursor(cursor);
    }

    public List<Item> getItemsByUser() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where UserId=?", new String[]{ this.UserId + ""});
        return getItemsFromCursor(cursor);
    }
    public List<Item> getAllItemsByUser(String type) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where UserId=? and Type=?", new String[]{ this.UserId + "", type});
        return getItemsFromCursor(cursor);
    }
    public List<Item> getItemsByFolder0() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where UpdateTime IS NOT NULL and IsHidden=0 and FolderId=? and UserId=? order by Priority desc, rowid asc", new String[]{NULL_UUID.toString(), this.UserId + ""});
        return getItemsFromCursor(cursor);
    }

    public Item getItemById(UUID id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where Id=? and UserId=? order by Priority desc, id asc", new String[]{id.toString(), this.UserId + ""});
        return cursor.getCount()==0?null: getItemsFromCursor(cursor).get(0);
    }

    public List<Item> getHiddenItemsByFolder0() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where UpdateTime IS NOT NULL and IsHidden=1 and FolderId=? and UserId=? order by Priority desc, rowid asc", new String[]{NULL_UUID.toString(), this.UserId + ""});
        return getItemsFromCursor(cursor);
    }

    public List<Item> getItemsByFolderIdForAdding(UUID id, int hideMode) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where UpdateTime IS NOT NULL and IsHidden=? and UserId=? and FolderId=? or FolderId=? order by Priority desc, rowid asc", new String[]{ hideMode + "", UserId + "", NULL_UUID.toString(), "" + id.toString()});
        return getItemsFromCursor(cursor);
    }

    public int getItemFolderItemsCount(UUID id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where UpdateTime IS NOT NULL and FolderId=? and UserId=? order by Priority desc, rowid asc", new String[]{"" + id.toString(), UserId + ""});
        return cursor.getCount();
    }

    public Boolean deleteItem(UUID id) {
        Item item = getItemById(id);
        if(item==null)
            return true;
        deletePhotoFile(item.Image);
        updateItem(item.Id, item, true);
        deletePassport(id);
        deleteCreditCard(id);
        deleteINN(id);
        deleteSnils(id);
        deletePolis(id);
        deletePhotos(id);
        deleteTemplateDocument(id);
        return true;
    }

    public void deleteCreditCard(UUID id) {
        CreditCard object = getCreditCardById(id);
        if (object != null) {
            deletePhotoFile(object.PhotoPage1);
            updateCreditCard(object.Id, object, true);
        }
    }
    public void deleteCreditCardConfirm(UUID id) {
        SQLiteDatabase db = open();
        db.delete("CreditCard", "Id=?", new String[]{"" + id.toString()});
    }

    private void deleteINN(UUID id) {
        Inn object = getInnById(id);
        if (object != null) {
            deletePhotoFile(object.PhotoPage1);
            updateInn(object.Id, object, true);
        }
    }

    public Boolean deleteItemConfirm(UUID id) {
        SQLiteDatabase db = open();
        db.delete("Item", "Id=?", new String[]{"" + id.toString()});
        db.delete("Passport", "Id=?", new String[]{"" + id.toString()});
        db.delete("CreditCard", "Id=?", new String[]{"" + id.toString()});
        db.delete("INN", "Id=?", new String[]{"" + id.toString()});
        db.delete("SNILS", "Id=?", new String[]{"" + id.toString()});
        db.delete("Polis", "Id=?", new String[]{"" + id.toString()});
        deleteTemplateDocumentConfirm(id);
        db.delete("Photo", "CollectionId=?", new String[]{"" + id.toString()});
        db.delete("Item", "FolderId=?", new String[]{"" + id.toString()});
        return true;
    }

    //SNILS table
    private List<Snils> getSnilsFromCursor(Cursor cur) {
        List<Snils> snilsList = new ArrayList<>();
        Snils snils;
        while (cur.moveToNext()) {
            snils = new Snils(UUID.fromString(cur.getString(0)),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getString(4),
                    cur.getString(5),
                    cur.getString(6),
                    cur.getString(7),
                    cur.getString(8));
            snilsList.add(snils);
        }
        return snilsList;
    }

    public void deleteSnils(UUID id) {
        Snils object = getSnilsById(id);
        if (object != null) {
            deletePhotoFile(object.PhotoPage1);
            updateSnils(object.Id, object, true);
        }
    }

    public void deleteSnilsConfirm(UUID id) {
        SQLiteDatabase db = open();
        db.delete("SNILS", "Id=?", new String[]{"" + id.toString()});
    }
    public Snils getSnilsById(UUID objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("SELECT * FROM SNILS WHERE Id=?", new String[]{objectId.toString()});
        return cursor.getCount()==0? null:getSnilsFromCursor(cursor).get(0);
    }

    public boolean updateSnils(UUID id, Snils snils, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        if (isDelete)
            snils.setValuesNull();
        else
            snils.UpdateTime = getCurrentTime();
        cv.put("Number", snils.Number);
        cv.put("FIO", snils.FIO);
        cv.put("Gender", String.valueOf(snils.Gender));
        cv.put("BirthDate", snils.BirthDate);
        cv.put("BirthPlace", snils.BirthPlace);
        cv.put("RegistrationDate", snils.RegistrationDate);
        cv.put("PhotoPage1", snils.PhotoPage1);
        cv.put("UpdateTime", snils.UpdateTime);
        int result = db.update("SNILS", cv, "Id=?", new String[]{id.toString()});
        return result > 0;
    }

    public UUID insertSnils(Snils snils) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", snils.Id.toString());
        cv.put("Number", snils.Number);
        cv.put("FIO", snils.FIO);
        cv.put("Gender", String.valueOf(snils.Gender));
        cv.put("BirthDate", snils.BirthDate);
        cv.put("BirthPlace", snils.BirthPlace);
        cv.put("RegistrationDate", snils.RegistrationDate);
        cv.put("PhotoPage1", snils.PhotoPage1);
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("SNILS", null, cv);
        return result > 0 ? snils.Id : NULL_UUID;
    }
    // Inn table
    private List<Inn> getInnsFromCursor(Cursor cur) {
        List<Inn> inns = new ArrayList<>();
        Inn inn;
        while (cur.moveToNext()) {
            inn = new Inn(UUID.fromString(cur.getString(0)),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getString(4),
                    cur.getString(5),
                    cur.getString(6),
                    cur.getString(7),
                    cur.getString(8));
            inns.add(inn);
        }
        return inns;
    }

    public void deleteInn(UUID id) {
        Inn object = getInnById(id);
        if (object != null) {
            deletePhotoFile(object.PhotoPage1);
            object.BirthDate = null;
            object.PhotoPage1 = null;
            object.RegistrationDate = null;
            object.FIO = null;
            object.Number = null;
            object.UpdateTime = null;
            updateInn(object.Id, object, true);
        }
    }
    public void deleteInnConfirm(UUID id) {
        SQLiteDatabase db = open();
        db.delete("INN", "Id=?", new String[]{"" + id.toString()});
    }

    public Inn getInnById(UUID objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from INN where Id=?", new String[]{"" + objectId.toString()});
        return cursor.getCount()==0? null: getInnsFromCursor(cursor).get(0);
    }

    public Boolean updateInn(UUID id, Inn inn, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        if (isDelete)
            inn.setValuesNull();
        else
            inn.UpdateTime = getCurrentTime();
        cv.put("Number", inn.Number);
        cv.put("FIO", inn.FIO);
        cv.put("Gender", String.valueOf(inn.Gender));
        cv.put("BirthDate", inn.BirthDate);
        cv.put("BirthPlace", inn.BirthPlace);
        cv.put("RegistrationDate", inn.RegistrationDate);
        cv.put("PhotoPage1", inn.PhotoPage1);
        cv.put("UpdateTime", inn.UpdateTime);
        long result = db.update("INN", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public UUID insertInn(Inn inn) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", inn.Id.toString());
        cv.put("Number", inn.Number);
        cv.put("FIO", inn.FIO);
        cv.put("Gender", String.valueOf(inn.Gender));
        cv.put("BirthDate", inn.BirthDate);
        cv.put("BirthPlace", inn.BirthPlace);
        cv.put("RegistrationDate", inn.RegistrationDate);
        cv.put("PhotoPage1", inn.PhotoPage1);
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("INN", null, cv);
        if (result <= 0)
            return NULL_UUID;
        else {
            return inn.Id;
        }
    }

    //*********************************************************************************************
    //Passport table
    private List<Passport> getPassportsFromCursor(Cursor cur) {
        List<Passport> passports = new ArrayList<>();
        Passport passport;
        while (cur.moveToNext()) {
            passport = new Passport(UUID.fromString(cur.getString(0)),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getString(4),
                    cur.getString(5),
                    cur.getString(6),
                    cur.getString(7),
                    cur.getString(8),
                    cur.getString(9),
                    cur.getString(10),
                    cur.getString(11),
                    cur.getString(12),
                    cur.getString(13));
            passports.add(passport);
        }
        return passports;
    }

    public List<Passport> getPassportsByUser() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Passport where UserId=?", new String[]{this.UserId + ""});
        return getPassportsFromCursor(cursor);
    }
    public void deletePassport(UUID id) {
        Passport object = getPassportById(id);
        if (object != null) {
            deletePhotoFile(object.FacePhoto);
            deletePhotoFile(object.PhotoPage1);
            deletePhotoFile(object.PhotoPage2);
            updatePassport(object.Id, object, true);
        }
    }
    public void deletePassportConfirm(UUID id) {
        SQLiteDatabase db = open();
        db.delete("Passport", "Id=?", new String[]{"" + id.toString()});
    }

    public Passport getPassportById(UUID objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Passport where Id=?", new String[]{"" + objectId.toString()});
        return cursor.getCount()==0? null:getPassportsFromCursor(cursor).get(0);
    }

    public Boolean updatePassport(UUID id, Passport passport, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        if (isDelete)
            passport.setValuesNull();
        else
            passport.UpdateTime = getCurrentTime();
        cv.put("SeriaNomer", passport.SeriaNomer);
        cv.put("DivisionCode", passport.DivisionCode);
        cv.put("GiveDate", passport.GiveDate);
        cv.put("ByWhom", passport.ByWhom);
        cv.put("FIO", passport.FIO);
        cv.put("BirthDate", passport.BirthDate);
        cv.put("Gender", passport.Gender);
        cv.put("BirthPlace", passport.BirthPlace);
        cv.put("ResidencePlace", passport.ResidencePlace);
        cv.put("FacePhoto", passport.FacePhoto);
        cv.put("PhotoPage1", passport.PhotoPage1);
        cv.put("PhotoPage2", passport.PhotoPage2);
        cv.put("UpdateTime", passport.UpdateTime);
        long result = db.update("Passport", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public UUID insertPassport(Passport passport) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", passport.Id.toString());
        cv.put("SeriaNomer", passport.SeriaNomer);
        cv.put("DivisionCode", passport.DivisionCode);
        cv.put("GiveDate", passport.GiveDate);
        cv.put("ByWhom", passport.ByWhom);
        cv.put("FIO", passport.FIO);
        cv.put("BirthDate", passport.BirthDate);
        cv.put("Gender", passport.Gender);
        cv.put("BirthPlace", passport.BirthPlace);
        cv.put("ResidencePlace", passport.ResidencePlace);
        cv.put("FacePhoto", passport.FacePhoto);
        cv.put("PhotoPage1", passport.PhotoPage1);
        cv.put("PhotoPage2", passport.PhotoPage2);
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("Passport", null, cv);
        if (result <= 0)
            return NULL_UUID;
        else {
            return passport.Id;
        }
    }

    //*********************************************************************************************
    //User table

    public User getUserById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from User where Id=?", new String[]{"" + objectId});
        return getUserFromCursor(cursor);
    }

    public String getUserUpdateTime() {
        User user = getUserById(UserId);
        return user.UpdateTime;
    }
    public Boolean setUserUpdateTime() {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("UpdateTime", getCurrentTime());
        long result = db.update("User", cv, "id=?", new String[]{"" + UserId});
        if (result <= 0)
            return false;
        else
            return true;
    }
    public User getUserFromCursor(Cursor cur) {
        User user;
        if (cur.getCount() == 0)
            return null;
        cur.moveToFirst();
        user = new User(cur.getInt(0),
                cur.getString(1),
                cur.getString(2),
                cur.getString(3),
                cur.getString(4),
                cur.getString(5),
                cur.getString(6));
        return user;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("select count(*) from User where Email=?", new String[]{email});
        if (cur != null) {
            cur.moveToFirst();
            if (cur.getInt(0) == 0) {
                return null;
            }
        }
        cur = db.rawQuery("select * from User where Email=?", new String[]{email});
        return getUserFromCursor(cur);
    }

    public Boolean updateUser(int id, User user) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Email", user.Email);
        cv.put("Login", user.Login);
        cv.put("Password", user.Password);
        cv.put("Photo", user.Photo);
        cv.put("AccessCode", user.AccessCode);
        long result = db.update("User", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean updateUserDate(int id, String datetimeMillis) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("UpdateTime", datetimeMillis);
        long result = db.update("User", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertUser(User user) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", user.Id);
        cv.put("Email", user.Email);
        cv.put("Login", user.Login);
        cv.put("Password", user.Password);
        cv.put("Photo", user.Photo);
        cv.put("AccessCode", user.AccessCode);
        cv.put("UpdateTime", user.UpdateTime);
        long result = db.insert("User", null, cv);
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public int selectLastUserId() {
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("SELECT rowid from User order by ROWID DESC limit 1", null);
        cur.moveToFirst();
        int id;
        if (cur != null && cur.getCount() > 0)
            id = cur.getInt(0);
        else
            id = 0;
        return id;
    }

    //*********************************************************************************************
    //Photo table
    private List<Photo> getPhotosFromCursor(Cursor cur) {
        List<Photo> photos = new ArrayList<>();
        Photo photo;
        while (cur.moveToNext()) {
            photo = new Photo(
                    UUID.fromString(cur.getString(0)),
                    cur.getString(1),
                    UUID.fromString(cur.getString(2)),
                    cur.getString(3));
            photos.add(photo);
        }
        return photos;
    }

    public List<Photo> getPhotos(UUID id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Photo where CollectionId=?", new String[]{"" + id.toString()});
        return getPhotosFromCursor(cursor);
    }
    public Photo getPhoto(UUID id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Photo where Id=?", new String[]{"" + id.toString()});
        return cursor.getCount()==0?null:getPhotosFromCursor(cursor).get(0);
    }

    public Boolean deletePhoto(UUID id) {
        Photo photo = getPhoto(id);
        if(photo==null)
            return false;
        deletePhotoFile(photo.Image);
        photo.setValuesNull();
        updatePhoto(photo.Id, photo, true);
        return true;
    }
    public Boolean deletePhotoConfirm(UUID id) {
        SQLiteDatabase db = open();
        long result = db.delete("Photo", "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean deletePhotos(UUID id) {
        List<Photo> photos = getPhotos(id);
        for (Photo photo :
                photos) {
            deletePhotoFile(photo.Image);
            photo.setValuesNull();
            updatePhoto(photo.Id, photo, true);
        }
        return true;
    }

    public Boolean updatePhoto(UUID id, Photo photo, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        photo.UpdateTime = isDelete?null:getCurrentTime();
        cv.put("Image", photo.Image);
        cv.put("CollectionId", photo.CollectionId.toString());
        cv.put("UpdateTime", photo.UpdateTime);
        long result = db.update("Photo", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertPhoto(Photo photo, boolean isNew) {
        UUID id = isNew?UUID.randomUUID():photo.Id;
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", id.toString());
        cv.put("Image", photo.Image);
        cv.put("CollectionId", photo.CollectionId.toString());
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("Photo", null, cv);
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    //*********************************************************************************************
    //CreditCard table

    private List<CreditCard> getCreditCardsFromCursor(Cursor cur) {
        List<CreditCard> creditCards = new ArrayList<>();
        CreditCard creditCard;
        while (cur.moveToNext()) {
            creditCard = new CreditCard(
                    UUID.fromString(cur.getString(0)),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4),
                    cur.getString(5),
                    cur.getString(6));
            creditCards.add(creditCard);
        }
        return creditCards;
    }

    public CreditCard getCreditCardById(UUID objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from CreditCard where Id=?", new String[]{"" + objectId.toString()});
        List<CreditCard> cards = getCreditCardsFromCursor(cursor);
        return cards.size() == 0 ? null : cards.get(0);
    }

    public Boolean updateCreditCard(UUID id, CreditCard creditCard, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        if (isDelete)
            creditCard.setValuesNull();
        else
            creditCard.UpdateTime = getCurrentTime();
        cv.put("Number", creditCard.Number);
        cv.put("FIO", creditCard.FIO);
        cv.put("ExpiryDate", creditCard.ExpiryDate);
        cv.put("CVV", creditCard.CVV);
        cv.put("PhotoPage1", creditCard.PhotoPage1);
        cv.put("UpdateTime", creditCard.UpdateTime);
        long result = db.update("CreditCard", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public UUID insertCreditCard(CreditCard creditCard) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", creditCard.Id.toString());
        cv.put("Number", creditCard.Number);
        cv.put("FIO", creditCard.FIO);
        cv.put("ExpiryDate", creditCard.ExpiryDate);
        cv.put("CVV", creditCard.CVV);
        cv.put("PhotoPage1", creditCard.PhotoPage1);
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("CreditCard", null, cv);
        if (result <= 0)
            return NULL_UUID;
        else {
            return creditCard.Id;
        }
    }

    //Polis
    private List<Polis> getPolisFromCursor(Cursor cur) {
        List<Polis> polisList = new ArrayList<>();
        Polis polis;
        while (cur.moveToNext()) {
            polis = new Polis(
                    UUID.fromString(cur.getString(0)),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getString(4),
                    cur.getString(5),
                    cur.getString(6),
                    cur.getString(7),
                    cur.getString(8));
            polisList.add(polis);
        }
        return polisList;
    }

    public void deletePolis(UUID id) {
        Polis object = getPolisById(id);
        if (object != null) {
            deletePhotoFile(object.PhotoPage1);
            deletePhotoFile(object.PhotoPage2);
            object.setValuesNull();
            updatePolis(object.Id, object, true);
        }
    }
    public void deletePolisConfirm(UUID id) {
        SQLiteDatabase db = open();
        db.delete("Polis", "Id=?", new String[]{"" + id.toString()});
    }

    public Polis getPolisById(UUID objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Polis where Id=?", new String[]{"" + objectId.toString()});
        List<Polis> polisList = getPolisFromCursor(cursor);
        return polisList.size() == 0 ? null : polisList.get(0);
    }

    public Boolean updatePolis(UUID id, Polis polis, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        if (isDelete)
            polis.setValuesNull();
        else
            polis.UpdateTime = getCurrentTime();
        cv.put("Number", polis.Number);
        cv.put("FIO", polis.FIO);
        cv.put("Gender", String.valueOf(polis.Gender));
        cv.put("BirthDate", polis.BirthDate);
        cv.put("PhotoPage1", polis.PhotoPage1);
        cv.put("PhotoPage2", polis.PhotoPage2);
        cv.put("ValidUntil", polis.ValidUntil);
        cv.put("UpdateTime", polis.UpdateTime);
        long result = db.update("Polis", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertPolis(Polis polis) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", polis.Id.toString());
        cv.put("Number", polis.Number);
        cv.put("FIO", polis.FIO);
        cv.put("Gender", String.valueOf(polis.Gender));
        cv.put("BirthDate", polis.BirthDate);
        cv.put("PhotoPage1", polis.PhotoPage1);
        cv.put("PhotoPage2", polis.PhotoPage2);
        cv.put("ValidUntil", polis.ValidUntil);
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("Polis", null, cv);
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    //Template
    private List<Template> getTemplatesFromCursor(Cursor cur) {
        List<Template> templates = new ArrayList<>();
        Template template;
        while (cur.moveToNext()) {
            template = new Template(
                    UUID.fromString(cur.getString(0)),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getString(5),
                    cur.getInt(4));
            templates.add(template);
        }
        return templates;
    }
    public List<Template> getAllTemplatesByUser() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Template where UserId=?", new String[]{ this.UserId + ""});
        return getTemplatesFromCursor(cursor);
    }
    public List<Template> getTemplateDownload(int userId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Template where UpdateTime IS NOT NULL and UserId=? and Status=?", new String[]{"" + userId, "Downloaded"});
        List<Template> templates = getTemplatesFromCursor(cursor);
        return templates;
    }

    public List<Template> getTemplatePublished() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Template where UpdateTime IS NOT NULL and UserId!=? and Status=?", new String[]{"" + UserId, "Published"});
        List<Template> templates = getTemplatesFromCursor(cursor);
        return templates;
    }

    public Template getTemplateById(UUID objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Template where Id=?", new String[]{"" + objectId.toString()});
        List<Template> templates = getTemplatesFromCursor(cursor);
        return templates.size() == 0 ? null : templates.get(0);
    }

    public List<Template> getTemplateByUserId(int userId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Template where UpdateTime IS NOT NULL and UserId=? and Status!=?", new String[]{"" + userId, "Downloaded"});
        List<Template> templates = getTemplatesFromCursor(cursor);
        return templates;
    }

    @SuppressLint("Range")
    public UUID selectLastTemplateId() {
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("SELECT Id from Template order by ROWID DESC limit 1", null);
        cur.moveToFirst();
        UUID id;
        if (cur != null && cur.getCount() > 0)
            id = UUID.fromString(cur.getString(0));
        else
            id = null;
        return id;
    }

    public Boolean updateTemplate(UUID id, Template template, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        if(isDelete)
            template.UpdateTime = null;
        else
            template.UpdateTime = getCurrentTime();
        cv.put("Name", template.Name);
        cv.put("Status", template.Status);
        cv.put("Date", template.Date);
        cv.put("UserId", template.UserId);
        cv.put("UpdateTime", template.UpdateTime);
        long result = db.update("Template", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public UUID insertTemplate(Template template, boolean isNew) {
        UUID id = isNew?UUID.randomUUID():template.Id;
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", id.toString());
        cv.put("Name", template.Name);
        cv.put("Status", template.Status);
        cv.put("Date", template.Date);
        cv.put("UserId", template.UserId);
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("Template", null, cv);
        if (result <= 0)
            return NULL_UUID;
        else {
            return id;
        }
    }

    public Boolean deleteTemplateConfirm(UUID id) {
        SQLiteDatabase db = open();
        long result = db.delete("Template", "id=?", new String[]{"" + id});
        db.delete("TemplateObject", "TemplateId=?", new String[]{"" + id});
        db.delete("TemplateDocument", "TemplateId=?", new String[]{"" + id});
        db.execSQL("DELETE FROM Item WHERE Id NOT IN (SELECT id FROM TemplateDocument)");
        db.execSQL("DELETE FROM TemplateDocumentData WHERE TemplateObjectId NOT IN (SELECT id FROM TemplateObject)");
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }
    public Boolean deleteTemplate(UUID id) {
        Template template = getTemplateById(id);
        if(template==null)
            return true;
        updateTemplate(id,template,true);
        List<TemplateDocument> templateDocuments = getTemplateDocumentByTemplate(id);
        for (TemplateDocument templateDocument:
             templateDocuments) {
            deleteItem(templateDocument.Id);
        }
        for (TemplateDocument templateDocument:
             templateDocuments) {
            deleteTemplateObjectByDoc(templateDocument.Id);
        }
        return true;
    }

    public Boolean deleteTemplateObjectByDoc(UUID id) {
        List<TemplateObject> templateObjects = getTemplateObjectsByTemplateId(id);
        for (TemplateObject templateObj :
                templateObjects) {
            templateObj.setValuesNull();
            updateTemplateObject(templateObj.Id, templateObj, true);
        }
        return true;
    }
    public Boolean deleteTemplateObject(UUID id) {
        open().delete("TemplateObject", "Id=?", new String[]{"" + id});
        return true;
    }
    public Boolean updateTemplateObject(UUID id, TemplateObject templateObject, boolean isDelete) {
        if(isDelete)
            templateObject.setValuesNull();
        else
            templateObject.UpdateTime = getCurrentTime();
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Position", templateObject.Position);
        cv.put("Type", templateObject.Type);
        cv.put("Title", templateObject.Title);
        cv.put("TemplateId", templateObject.TemplateId.toString());
        cv.put("UpdateTime", templateObject.UpdateTime);
        long result = db.update("TemplateObject", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }
    public Boolean deleteTemplateObjectConfirm(UUID id) {
        SQLiteDatabase db = open();
        long result = db.delete("TemplateObject", "id=?", new String[]{"" + id.toString()});
        db.delete("TemplateDocumentData", "TemplateObjectId=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }
    //TemplateDocument
    private List<TemplateDocument> getTemplateDocumentsFromCursor(Cursor cur) {
        List<TemplateDocument> templateDocuments = new ArrayList<>();
        TemplateDocument templateDocument;
        while (cur.moveToNext()) {
            templateDocument = new TemplateDocument(
                    UUID.fromString(cur.getString(0)),
                    UUID.fromString(cur.getString(1)),
                    cur.getString(2));
            templateDocuments.add(templateDocument);
        }
        return templateDocuments;
    }


    @SuppressLint("Range")
    public UUID selectLastTemplateDocumentId() {
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("SELECT Id from TemplateDocument order by ROWID DESC limit 1", null);
        cur.moveToFirst();
        UUID id;
        if (cur != null && cur.getCount() > 0)
            id = UUID.fromString(cur.getString(0));
        else
            id = null;
        return id;
    }

    public TemplateDocument getTemplateDocumentById(UUID id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateDocument where Id=?", new String[]{"" + id.toString()});
        List<TemplateDocument> documents = getTemplateDocumentsFromCursor(cursor);
        return documents.size() == 0 ? null : documents.get(0);
    }
    public List<TemplateDocument> getTemplateDocumentByTemplate(UUID id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateDocument where TemplateId=?", new String[]{"" + id.toString()});
        List<TemplateDocument> documents = getTemplateDocumentsFromCursor(cursor);
        return documents;
    }

    public Boolean deleteTemplateDocument(UUID id) {
        SQLiteDatabase db = open();
        TemplateDocument templateDocument = getTemplateDocumentById(id);
        if(templateDocument==null)
            return false;
        templateDocument.setValuesNull();
        ContentValues cv = new ContentValues();
        cv.put("UpdateTime", templateDocument.UpdateTime);
        db.update("TemplateDocument", cv, "id=?", new String[]{"" + id.toString()});

        cv = new ContentValues();
        cv.put("Value", (String) null);
        cv.put("UpdateTime", (String) null);
        db.update("TemplateDocumentData", cv, "TemplateDocumentId=?", new String[]{"" + id.toString()});
        return true;
    }

    public Boolean deleteTemplateDocumentConfirm(UUID id) {
        SQLiteDatabase db = open();
        long result = db.delete("TemplateDocument", "id=?", new String[]{"" + id.toString()});
        db.delete("TemplateDocumentData", "TemplateDocumentId=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public UUID insertTemplateDocument(TemplateDocument templateDocument) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", templateDocument.Id.toString());
        cv.put("TemplateId", templateDocument.TemplateId.toString());
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("TemplateDocument", null, cv);
        if (result <= 0)
            return NULL_UUID;
        else {
            return templateDocument.Id;
        }
    }
    public UUID updateTemplateDocument(UUID id, TemplateDocument templateDocument, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        if(isDelete)
            templateDocument.setValuesNull();
        else
            templateDocument.UpdateTime  = getCurrentTime();
        cv.put("Id", templateDocument.Id.toString());
        cv.put("TemplateId", templateDocument.TemplateId.toString());
        cv.put("UpdateTime", templateDocument.UpdateTime);
        long result = db.update("TemplateDocument", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return NULL_UUID;
        else {
            return templateDocument.Id;
        }
    }

    //TemplateDocumentData
    private List<TemplateDocumentData> getTemplateDocumentDataFromCursor(Cursor cur) {
        List<TemplateDocumentData> templateDocumentDataList = new ArrayList<>();
        TemplateDocumentData templateDocumentData;
        while (cur.moveToNext()) {
            templateDocumentData = new TemplateDocumentData(
                    UUID.fromString(cur.getString(0)),
                    cur.getString(1),
                    UUID.fromString(cur.getString(2)),
                    UUID.fromString(cur.getString(3)),
                    cur.getString(4));
            templateDocumentDataList.add(templateDocumentData);
        }
        return templateDocumentDataList;
    }

    public UUID selectLastTemplateDocumentDataId() {
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("SELECT Id from TemplateDocumentData order by ROWID DESC limit 1", null);
        cur.moveToFirst();
        UUID id;
        if (cur != null && cur.getCount() > 0)
            id = UUID.fromString(cur.getString(0));
        else
            id = null;
        return id;
    }

    public TemplateDocumentData getTemplateDocumentData(UUID objectId, UUID docId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateDocumentData where TemplateObjectId=? and TemplateDocumentId=?", new String[]{"" + objectId.toString(), "" + docId.toString()});
        List<TemplateDocumentData> templateDocumentDataList = getTemplateDocumentDataFromCursor(cursor);
        return templateDocumentDataList.size() == 0 ? null : templateDocumentDataList.get(0);
    }
    public List<TemplateDocumentData> getTemplateDocumentDataByDoc(UUID docId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateDocumentData where TemplateDocumentId=?", new String[]{"" + docId.toString()});
        List<TemplateDocumentData> templateDocumentDataList = getTemplateDocumentDataFromCursor(cursor);
        return templateDocumentDataList;
    }
    public TemplateDocumentData getTemplateDocumentDataById(UUID id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateDocumentData where Id=?", new String[]{"" + id.toString()});
        List<TemplateDocumentData> templateDocumentDataList = getTemplateDocumentDataFromCursor(cursor);
        return templateDocumentDataList.size()==0?null:templateDocumentDataList.get(0);
    }

    public Boolean deleteTemplateDocumentDataConfirm(UUID id) {
        SQLiteDatabase db = open();
        TemplateDocumentData templateDocumentData = getTemplateDocumentDataById(id);
        if(templateDocumentData!=null)
            if(getTemplateObjectsById(templateDocumentData.TemplateObjectId).Type.equals("Photo"))
                deletePhotoFile(templateDocumentData.Value);
        long result = db.delete("TemplateDocumentData", "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }
    public Boolean updateTemplateDocumentData(UUID id, TemplateDocumentData templateDocumentData, boolean isDelete) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        if(isDelete)
            templateDocumentData.setValuesNull();
        else
            templateDocumentData.UpdateTime = getCurrentTime();
        cv.put("Value", templateDocumentData.Value);
        cv.put("TemplateObjectId", templateDocumentData.TemplateObjectId.toString());
        cv.put("TemplateDocumentId", templateDocumentData.TemplateDocumentId.toString());
        cv.put("UpdateTime", templateDocumentData.UpdateTime);
        long result = db.update("TemplateDocumentData", cv, "id=?", new String[]{"" + id.toString()});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }


    public UUID insertTemplateDocumentData(TemplateDocumentData templateDocumentData, boolean isNew) {
        UUID id = UUID.randomUUID();
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", id.toString());
        cv.put("Value", templateDocumentData.Value);
        cv.put("TemplateObjectId", templateDocumentData.TemplateObjectId.toString());
        cv.put("TemplateDocumentId", templateDocumentData.TemplateDocumentId.toString());
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("TemplateDocumentData", null, cv);
        if (result <= 0)
            return NULL_UUID;
        else {
            return id;
        }
    }

    //TemplateObject
    private List<TemplateObject> getTemplateObjectsFromCursor(Cursor cur) {
        List<TemplateObject> templateObjects = new ArrayList<>();
        TemplateObject templateObject;
        while (cur.moveToNext()) {
            templateObject = new TemplateObject(
                    UUID.fromString(cur.getString(0)),
                    cur.getInt(1),
                    cur.getString(2),
                    cur.getString(3),
                    UUID.fromString(cur.getString(4)),
                    cur.getString(5));
            templateObjects.add(templateObject);
        }
        return templateObjects;
    }

    public List<TemplateObject> getTemplateObjectsByTemplateId(UUID templateId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateObject where TemplateId=?", new String[]{"" + templateId.toString()});
        return getTemplateObjectsFromCursor(cursor);
    }
    public TemplateObject getTemplateObjectsById(UUID Id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateObject where Id=?", new String[]{"" + Id.toString()});
        return cursor.getCount()==0?null:getTemplateObjectsFromCursor(cursor).get(0);
    }

    public UUID insertTemplateObject(TemplateObject templateObject, boolean isNew) {
        UUID id = isNew ? UUID.randomUUID():templateObject.TemplateId;
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", id.toString());
        cv.put("Position", templateObject.Position);
        cv.put("Type", templateObject.Type);
        cv.put("Title", templateObject.Title);
        cv.put("TemplateId", templateObject.TemplateId.toString());
        cv.put("UpdateTime", getCurrentTime());
        long result = db.insert("TemplateObject", null, cv);
        if (result <= 0)
            return NULL_UUID;
        else {
            return id;
        }
    }

}
