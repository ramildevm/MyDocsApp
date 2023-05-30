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
import com.example.mydocsapp.models.Polis;
import com.example.mydocsapp.models.Template;
import com.example.mydocsapp.models.TemplateDocument;
import com.example.mydocsapp.models.TemplateDocumentData;
import com.example.mydocsapp.models.TemplateObject;
import com.example.mydocsapp.models.UserTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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

    public SQLiteDatabase open() throws SQLException {
        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }

    //*********************************************************************************************
    //Item table
    public Boolean insertItem(Item item) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Title", item.Title);
        cv.put("Type", item.Type);
        cv.put("Image", item.Image);
        cv.put("Priority", item.Priority);
        cv.put("isHiden", item.isHiden);
        cv.put("FolderId", item.FolderId);
        cv.put("DateCreation", item.DateCreation);
        cv.put("UserId", UserId);
        cv.put("isSelected", item.isSelected);
        long result = db.insert("Item", null, cv);
        if (result == -1)
            return false;
        else
            return true;
    }

    private List<Item> getItemsFromCursor(Cursor cur) {
        List<Item> items = new ArrayList<>();
        Item item;
        while (cur.moveToNext()) {
            item = new Item(cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4),
                    cur.getInt(5),
                    cur.getInt(6),
                    cur.getString(7),
                    cur.getInt(8),
                    cur.getInt(8));
            items.add(item);
        }
        return items;
    }

    public Boolean updateItemFolder(int id, int i) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("FolderId", i);
        cv.put("isSelected", 0);
        long result = db.update("Item", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean updateItem(int id, Item item) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Title", item.Title);
        cv.put("Type", item.Type);
        cv.put("Image", item.Image);
        cv.put("Priority", item.Priority);
        cv.put("isHiden", item.isHiden);
        cv.put("FolderId", item.FolderId);
        cv.put("DateCreation", item.DateCreation);
        cv.put("UserId", UserId);
        cv.put("isSelected", item.isSelected);
        long result = db.update("Item", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public List<Item> getItemsByFolder(int id, int hideMode) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=? and FolderId=? and UserId=? order by Priority desc, id asc", new String[]{hideMode + "", "" + id, UserId + ""});
        return getItemsFromCursor(cursor);
    }

    public List<Item> getItemsByFolder0() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=0 and FolderId=0 and UserId=? order by Priority desc, id asc", new String[]{this.UserId + ""});
        return getItemsFromCursor(cursor);
    }

    public List<Item> getHiddenItemsByFolder0() {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=1 and FolderId=0 and UserId=? order by Priority desc, id asc", new String[]{this.UserId + ""});
        return getItemsFromCursor(cursor);
    }

    public List<Item> getItemsByFolderIdForAdding(int id, int hideMode) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where isHiden=? and UserId=? and FolderId=0 or FolderId=? order by Priority desc, id asc", new String[]{hideMode + "", UserId + "", "" + id});
        return getItemsFromCursor(cursor);
    }

    public int getItemFolderItemsCount(int id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Item where FolderId=? and UserId=? order by Priority desc, id asc", new String[]{"" + id, UserId + ""});
        return cursor.getCount();
    }

    public Boolean deleteItem(int id) {
        SQLiteDatabase db = open();
        db.delete("Item", "Id=?", new String[]{"" + id});
        db.delete("Passport", "Id=?", new String[]{"" + id});
        db.delete("CreditCard", "Id=?", new String[]{"" + id});
        db.delete("INN", "Id=?", new String[]{"" + id});
        db.delete("SNILS", "Id=?", new String[]{"" + id});
        db.delete("Polis", "Id=?", new String[]{"" + id});
        db.delete("Photo", "CollectionId=?", new String[]{"" + id});
        return true;
    }

    public Boolean deleteFolder(int id, int deleteAll) {
        SQLiteDatabase db = open();
        db.delete("Item", "Id=?", new String[]{"" + id});
        db.delete("Item", "FolderId=?", new String[]{"" + id});
        return true;
    }

    @SuppressLint("Range")
    public int selectLastItemId() {
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("SELECT rowid from Item order by ROWID DESC limit 1", null);
        cur.moveToFirst();
        int id;
        if (cur != null && cur.getCount() > 0)
            id = cur.getInt(0);
        else
            id = 0;
        return id;
    }

    //*********************************************************************************************
    //Passport table
    private List<Passport> getPassportsFromCursor(Cursor cur) {
        List<Passport> passports = new ArrayList<>();
        Passport passport;
        while (cur.moveToNext()) {
            passport = new Passport(
                    cur.getInt(0),
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
                    cur.getString(12));
            passports.add(passport);
        }
        return passports;
    }

    public Passport getPassportById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Passport where Id=?", new String[]{"" + objectId});
        return getPassportsFromCursor(cursor).get(0);
    }

    public Boolean updatePassport(int id, Passport passport) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
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
        long result = db.update("Passport", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertPassport(Passport passport) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", passport.Id);
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
        long result = db.insert("Passport", null, cv);
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    //*********************************************************************************************
    //User table

    public User getUserById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from User where Id=?", new String[]{"" + objectId});
        return getUserFromCursor(cursor);
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
                cur.getString(5));
        return user;
    }

    public User getUserByLogin(String login) {
        SQLiteDatabase db = open();
        Cursor cur = db.rawQuery("select count(*) from User where Login=?", new String[]{login});
        if (cur != null) {
            cur.moveToFirst();
            if (cur.getInt(0) == 0) {
                return null;
            }
        }
        cur = db.rawQuery("select * from User where Login=?", new String[]{login});
        return getUserFromCursor(cur);
    }

    public Boolean updateUser(int id, User user) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Login", user.Login);
        cv.put("Password", user.Password);
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
        cv.put("Login", user.Login);
        cv.put("Password", user.Password);
        cv.put("PremiumStatus", user.PremiumStatus);
        cv.put("Syncing", user.Syncing);
        cv.put("Photo", user.Photo);
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
                    cur.getInt(0),
                    cur.getString(1),
                    cur.getInt(2));
            photos.add(photo);
        }
        return photos;
    }

    public List<Photo> getPhotos(int id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Photo where CollectionId=?", new String[]{"" + id});
        return getPhotosFromCursor(cursor);
    }

    public Boolean deletePhoto(int id) {
        SQLiteDatabase db = open();
        long result = db.delete("Photo", "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean updatePhoto(int id, Photo photo) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Path", photo.Path);
        cv.put("CollectionId", photo.Path);
        long result = db.update("Photo", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertPhoto(Photo photo) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Path", photo.Path);
        cv.put("CollectionId", photo.CollectionId);
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
                    cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4),
                    cur.getString(5));
            creditCards.add(creditCard);
        }
        return creditCards;
    }

    public CreditCard getCreditCardById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from CreditCard where Id=?", new String[]{"" + objectId});
        List<CreditCard> cards = getCreditCardsFromCursor(cursor);
        return cards.size() == 0 ? null : cards.get(0);
    }

    public Boolean updateCreditCard(int id, CreditCard creditCard) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Number", creditCard.Number);
        cv.put("FIO", creditCard.FIO);
        cv.put("ExpiryDate", creditCard.ExpiryDate);
        cv.put("CVV", creditCard.CVV);
        cv.put("PhotoPage1", creditCard.PhotoPage1);
        long result = db.update("CreditCard", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertCreditCard(CreditCard creditCard) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", creditCard.Id);
        cv.put("Number", creditCard.Number);
        cv.put("FIO", creditCard.FIO);
        cv.put("ExpiryDate", creditCard.ExpiryDate);
        cv.put("CVV", creditCard.CVV);
        cv.put("PhotoPage1", creditCard.PhotoPage1);
        long result = db.insert("CreditCard", null, cv);
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }
    //Polis
    private List<Polis> getPolisFromCursor(Cursor cur) {
        List<Polis> polisList = new ArrayList<>();
        Polis polis;
        while (cur.moveToNext()) {
            polis = new Polis(
                    cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3).charAt(0),
                    cur.getString(4),
                    cur.getString(5),
                    cur.getString(6));
            polisList.add(polis);
        }
        return polisList;
    }

    public Polis getPolisById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Polis where Id=?", new String[]{"" + objectId});
        List<Polis> polisList = getPolisFromCursor(cursor);
        return polisList.size() == 0 ? null : polisList.get(0);
    }

    public Boolean updatePolis(int id, Polis polis) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Number", polis.Number);
        cv.put("FIO", polis.FIO);
        cv.put("Gender", String.valueOf(polis.Gender));
        cv.put("BirthDate", polis.BirthDate);
        cv.put("PhotoPage1", polis.PhotoPage1);
        cv.put("PhotoPage2", polis.PhotoPage2);
        long result = db.update("Polis", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertPolis(Polis polis) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", polis.Id);
        cv.put("Number", polis.Number);
        cv.put("FIO", polis.FIO);
        cv.put("Gender", String.valueOf(polis.Gender));
        cv.put("BirthDate", polis.BirthDate);
        cv.put("PhotoPage1", polis.PhotoPage1);
        cv.put("PhotoPage2", polis.PhotoPage2);
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
                    cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4));
            templates.add(template);
        }
        return templates;
    }

    public Template getTemplateById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from Template where Id=?", new String[]{"" + objectId});
        List<Template> templates = getTemplatesFromCursor(cursor);
        return templates.size() == 0 ? null : templates.get(0);
    }

    public Boolean updateTemplate(int id, Template template) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Name", template.Name);
        cv.put("Status", template.Status);
        cv.put("Date", template.Date);
        cv.put("UserId", template.UserId);
        long result = db.update("Template", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertTemplate(Template template) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", template.Id);
        cv.put("Name", template.Name);
        cv.put("Status", template.Status);
        cv.put("Date", template.Date);
        cv.put("UserId", template.UserId);
        long result = db.insert("Template", null, cv);
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
                    cur.getInt(0),
                    cur.getInt(1));
            templateDocuments.add(templateDocument);
        }
        return templateDocuments;
    }

    public TemplateDocument getTemplateDocumentById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateDocument where Id=?", new String[]{"" + objectId});
        List<TemplateDocument> documents = getTemplateDocumentsFromCursor(cursor);
        return documents.size() == 0 ? null : documents.get(0);
    }

    public Boolean updateTemplateDocument(int id, TemplateDocument templateDocument) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("TemplateId", templateDocument.TemplateId);
        long result = db.update("TemplateDocument", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertTemplateDocument(TemplateDocument templateDocument) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", templateDocument.Id);
        cv.put("TemplateId", templateDocument.TemplateId);
        long result = db.insert("TemplateDocument", null, cv);
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    //TemplateDocumentData
    private List<TemplateDocumentData> getTemplateDocumentDataFromCursor(Cursor cur) {
        List<TemplateDocumentData> templateDocumentDataList = new ArrayList<>();
        TemplateDocumentData templateDocumentData;
        while (cur.moveToNext()) {
            templateDocumentData = new TemplateDocumentData(
                    cur.getInt(0),
                    cur.getString(1),
                    cur.getInt(2),
                    cur.getInt(3));
            templateDocumentDataList.add(templateDocumentData);
        }
        return templateDocumentDataList;
    }

    public TemplateDocumentData getTemplateDocumentDataById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateDocumentData where Id=?", new String[]{"" + objectId});
        List<TemplateDocumentData> templateDocumentDataList = getTemplateDocumentDataFromCursor(cursor);
        return templateDocumentDataList.size() == 0 ? null : templateDocumentDataList.get(0);
    }

    public Boolean updateTemplateDocumentData(int id, TemplateDocumentData templateDocumentData) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Value", templateDocumentData.Value);
        cv.put("TemplateObjectId", templateDocumentData.TemplateObjectId);
        cv.put("TemplateDocumentId", templateDocumentData.TemplateDocumentId);
        long result = db.update("TemplateDocumentData", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertTemplateDocumentData(TemplateDocumentData templateDocumentData) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", templateDocumentData.Id);
        cv.put("Value", templateDocumentData.Value);
        cv.put("TemplateObjectId", templateDocumentData.TemplateObjectId);
        cv.put("TemplateDocumentId", templateDocumentData.TemplateDocumentId);
        long result = db.insert("TemplateDocumentData", null, cv);
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    //TemplateObject
    private List<TemplateObject> getTemplateObjectsFromCursor(Cursor cur) {
        List<TemplateObject> templateObjects = new ArrayList<>();
        TemplateObject templateObject;
        while (cur.moveToNext()) {
            templateObject = new TemplateObject(
                    cur.getInt(0),
                    cur.getInt(1),
                    cur.getString(2),
                    cur.getString(3),
                    cur.getInt(4));
            templateObjects.add(templateObject);
        }
        return templateObjects;
    }

    public TemplateObject getTemplateObjectById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from TemplateObject where Id=?", new String[]{"" + objectId});
        List<TemplateObject> templateObjects = getTemplateObjectsFromCursor(cursor);
        return templateObjects.size() == 0 ? null : templateObjects.get(0);
    }

    public Boolean updateTemplateObject(int id, TemplateObject templateObject) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Position", templateObject.Position);
        cv.put("Type", templateObject.Type);
        cv.put("Title", templateObject.Title);
        cv.put("TemplateId", templateObject.TemplateId);
        long result = db.update("TemplateObject", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertTemplateObject(TemplateObject templateObject) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", templateObject.Id);
        cv.put("Position", templateObject.Position);
        cv.put("Type", templateObject.Type);
        cv.put("Title", templateObject.Title);
        cv.put("TemplateId", templateObject.TemplateId);
        long result = db.insert("TemplateObject", null, cv);
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    //UserTemplate
    private List<UserTemplate> getUserTemplatesFromCursor(Cursor cur) {
        List<UserTemplate> userTemplates = new ArrayList<>();
        UserTemplate userTemplate;
        while (cur.moveToNext()) {
            userTemplate = new UserTemplate(
                    cur.getInt(0),
                    cur.getInt(1),
                    cur.getInt(2));
            userTemplates.add(userTemplate);
        }
        return userTemplates;
    }

    public UserTemplate getUserTemplateById(int objectId) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery("select * from UserTemplate where Id=?", new String[]{"" + objectId});
        List<UserTemplate> userTemplates = getUserTemplatesFromCursor(cursor);
        return userTemplates.size() == 0 ? null : userTemplates.get(0);
    }

    public Boolean updateUserTemplate(int id, UserTemplate userTemplate) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("UserId", userTemplate.UserId);
        cv.put("TemplateId", userTemplate.TemplateId);
        long result = db.update("UserTemplate", cv, "id=?", new String[]{"" + id});
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }

    public Boolean insertUserTemplate(UserTemplate userTemplate) {
        SQLiteDatabase db = open();
        ContentValues cv = new ContentValues();
        cv.put("Id", userTemplate.Id);
        cv.put("UserId", userTemplate.UserId);
        cv.put("TemplateId", userTemplate.TemplateId);
        long result = db.insert("UserTemplate", null, cv);
        if (result <= 0)
            return false;
        else {
            return true;
        }
    }




}

