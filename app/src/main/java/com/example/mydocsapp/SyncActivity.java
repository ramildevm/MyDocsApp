package com.example.mydocsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mydocsapp.api.MainApiService;
import com.example.mydocsapp.api.ResponseCallback;
import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.databinding.ActivitySyncBinding;
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
import com.example.mydocsapp.services.AppService;
import com.example.mydocsapp.services.CryptoService;
import com.example.mydocsapp.services.DBHelper;
import com.example.mydocsapp.services.ValidationService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SyncActivity extends AppCompatActivity {
    ActivitySyncBinding binding;
    private DBHelper db;
    int progress = 0;
    List<Item> deleteItemList = new ArrayList<>();
    List<Template> deleteTemplateList = new ArrayList<>();
    DelayedDialogTask delayedDialogTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyncBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = new DBHelper(this, AppService.getUserId(this));
        delayedDialogTask = new DelayedDialogTask(this);

        binding.menubarBack.setOnClickListener(v -> goBackSettingsClick(v));
        binding.uploadBtn.setOnClickListener(v -> uploadBtnClick());
        binding.downloadBtn.setOnClickListener(v -> downloadBtnClick());

    }
    private void uploadBtnClick() {
        setLoadingWindowParams(true);
        MainApiService mainApiService = new MainApiService(this);
        List<Item> OldItems = db.getItemsByUser();
        List<Item> Items = new ArrayList<>();
        for (Item item :
                OldItems) {
            if(!ValidationService.isUpdateTimeNewerThanUser(item.UpdateTime,db.getUserUpdateTime()))
                continue;
            item.Image = item.Type.equals("Collection")? null : ImageService.getPhoto(item.Image);
            Items.add(item);
        }
        mainApiService.UpdateItems(Items, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                Log.e("Synced", "Yes");
                setProgressValue(20);
                updatePassports(mainApiService);
            }
            @Override
            public void onConflict() { Log.e("Synced", "Items" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Items" + "No2" + errorMessage);
            }
        });
        Log.e("Synced", "updated" + progress);
    }

    private void updateTemplateDocuments(MainApiService mainApiService) {
        List<TemplateDocument> TemplateDocumentList = new ArrayList<>();
        for (Item item :
                db.getAllItemsByUser("Template")) {
            TemplateDocument _TemplateDocument = db.getTemplateDocumentById(item.Id);
            if(!ValidationService.isUpdateTimeNewerThanUser(_TemplateDocument.UpdateTime,db.getUserUpdateTime()))
                continue;
            TemplateDocumentList.add(_TemplateDocument);
        }
        mainApiService.UpdateTemplateDocuments(TemplateDocumentList, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(90);
                updateTemplateDocumentData(mainApiService);
            }
            @Override
            public void onConflict() { Log.e("Synced", "TempDoc" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","TempDoc" + "No2" + errorMessage);
            }
        });
    }
    private void updateTemplateDocumentData(MainApiService mainApiService) {
        List<TemplateDocument> TemplateDocumentList = new ArrayList<>();
        for (Item item :
                db.getAllItemsByUser("Template")) {
            TemplateDocument _TemplateDocument = db.getTemplateDocumentById(item.Id);
            TemplateDocumentList.add(_TemplateDocument);
        }
        List<TemplateDocumentData> templateDocumentData = new ArrayList<>();
        for (TemplateDocument tempDoc :
                TemplateDocumentList) {
            for (TemplateDocumentData tempData :
                    db.getTemplateDocumentDataByDoc(tempDoc.Id)) {
                if(!ValidationService.isUpdateTimeNewerThanUser(tempData.UpdateTime,db.getUserUpdateTime()))
                    continue;
                if(db.getTemplateObjectsById(tempData.TemplateObjectId).Type.equals("Photo")) {
                    tempData.Value = ImageService.getPhoto(tempData.Value);
                }
                templateDocumentData.add(tempData);
            }
        }
        mainApiService.UpdateTemplateDocumentData(templateDocumentData, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(100);
            }
            @Override
            public void onConflict() { Log.e("Synced", "Temp d" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Temp d" + "No2" + errorMessage);
            }
        });
    }

    private void updatePolis(MainApiService mainApiService) {
        List<Polis> PolisList = new ArrayList<>();
        for (Item item:
                db.getAllItemsByUser("Polis")) {
            Polis _Polis = db.getPolisById(item.Id);
            if(!ValidationService.isUpdateTimeNewerThanUser(_Polis.UpdateTime,db.getUserUpdateTime()))
                continue;
            _Polis.PhotoPage1 = ImageService.getPhoto(_Polis.PhotoPage1);
            _Polis.BirthDate = ValidationService.dateToIso(_Polis.BirthDate);
            PolisList.add(_Polis);
        }
        mainApiService.UpdatePolis(PolisList, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(55);
                updateTemplates(mainApiService);
            }
            @Override
            public void onConflict() { Log.e("Synced", "Polis" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Polis" + "No2" + errorMessage);
            }
        });
    }
    private void updateTemplates(MainApiService mainApiService) {
        List<Template> TemplateList = new ArrayList<>();
        for (Template template :
                db.getAllTemplatesByUser()) {
            if(!ValidationService.isUpdateTimeNewerThanUser(template.UpdateTime,db.getUserUpdateTime()))
                continue;
            TemplateList.add(template);
        }
        mainApiService.UpdateTemplates(TemplateList, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(70);
                updateTemplateObjects(mainApiService);
            }
            @Override
            public void onConflict() { Log.e("Synced", "Temp" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Temp" + "No2" + errorMessage);
            }
        });
    }
    private void updateTemplateObjects(MainApiService mainApiService) {
        List<Template> TemplateList = db.getAllTemplatesByUser();
        List<TemplateObject> templateObjects = new ArrayList<>();
        for (Template template :
                TemplateList) {
            templateObjects.addAll(db.getTemplateObjectsByTemplateId(template.Id));
        }
        List<TemplateObject> templateObjectsOld = new ArrayList<>();
        templateObjectsOld.addAll(templateObjects);
        for (TemplateObject templateObj :
                templateObjectsOld) {
            if(!ValidationService.isUpdateTimeNewerThanUser(templateObj.UpdateTime,db.getUserUpdateTime()))
                templateObjects.remove(templateObj);
        }
        mainApiService.UpdateTemplateObjects(templateObjects, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(80);
                updateTemplateDocuments(mainApiService);
            }
            @Override
            public void onConflict() {
            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void updateCreditCards(MainApiService mainApiService) {
        List<CreditCard> CreditCardList = new ArrayList<>();
        for (Item item :
                db.getAllItemsByUser("CreditCard")) {
            CreditCard _CreditCard = db.getCreditCardById(item.Id);
            if(!ValidationService.isUpdateTimeNewerThanUser(_CreditCard.UpdateTime,db.getUserUpdateTime()))
                continue;
            _CreditCard.PhotoPage1 = ImageService.getPhoto(_CreditCard.PhotoPage1);
            CreditCardList.add(_CreditCard);
        }
        mainApiService.UpdateCreditCards(CreditCardList, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(50);
                updatePolis(mainApiService);
            }

            @Override
            public void onConflict() { Log.e("Synced", "Credit" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Credit" + "No2" + errorMessage);
            }
        });
    }

    private void updateInns(MainApiService mainApiService) {
        List<Inn> InnList = new ArrayList<>();
        for (Item item :
                db.getAllItemsByUser("INN")) {
            Inn _Inn = db.getInnById(item.Id);
            if(!ValidationService.isUpdateTimeNewerThanUser(_Inn.UpdateTime,db.getUserUpdateTime()))
                continue;
            _Inn.PhotoPage1 = ImageService.getPhoto(_Inn.PhotoPage1);
            _Inn.BirthDate = ValidationService.dateToIso(_Inn.BirthDate);
            InnList.add(_Inn);
        }
        mainApiService.UpdateInns(InnList, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(45);
                updateCreditCards(mainApiService);
            }

            @Override
            public void onConflict() { Log.e("Synced", "Inn" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Inn" + "No2" + errorMessage);
            }
        });
    }

    private void setLoadingWindowParams(boolean flag) {
        if (flag) {
            binding.parentPanel.setEnabled(false);
            binding.progressBarPanel.setVisibility(View.VISIBLE);
            binding.progressTxt.setText(getString(R.string.loading) + progress + "%");
        } else {
            setProgressValue(0);
            binding.parentPanel.setEnabled(true);
            binding.progressBarPanel.setVisibility(View.INVISIBLE);
        }
    }

    private void setProgressValue(int i) {
        progress = i;
        binding.progressTxt.setText(getString(R.string.loading) + progress + "%");
        if(progress==100){
            for (Item item :
                    deleteItemList) {
                db.deleteItemConfirm(item.Id);
            }
            for (Template item :
                    deleteTemplateList) {
                db.deleteTemplateConfirm(item.Id);
            }
            setLoadingWindowParams(false);
        }
        else
            delayedDialogTask.execute();
    }

    private void updatePassports(MainApiService mainApiService) {
        List<Passport> passports = new ArrayList<>();
        for (Item item :
                db.getAllItemsByUser("Passport")) {
            Passport passport = db.getPassportById(item.Id);
            if(!ValidationService.isUpdateTimeNewerThanUser(passport.UpdateTime,db.getUserUpdateTime()))
                continue;
            passport.FacePhoto = ImageService.getPhoto(passport.FacePhoto);
            passport.PhotoPage1 = ImageService.getPhoto(passport.PhotoPage1);
            passport.PhotoPage2 = ImageService.getPhoto(passport.PhotoPage2);

            passport.BirthDate = ValidationService.dateToIso(passport.BirthDate);
            passport.GiveDate = ValidationService.dateToIso(passport.GiveDate);
            passports.add(passport);
        }
        mainApiService.UpdatePassports(passports, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(25);
                updateSnils(mainApiService);
            }
            @Override
            public void onConflict() { Log.e("Synced", "Passport" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Passport" + "No2" + errorMessage);
            }
        });
    }

    private void updatePhotos(MainApiService mainApiService) {
        List<Photo> photos = new ArrayList<>();
        for (Item item :
                db.getAllItemsByUser("Collection")) {
            List<Photo> photoList = db.getPhotos(item.Id);
            for (Photo photo :
                    photoList) {
                photo.Image = ImageService.getPhoto(photo.Image);
                if(!ValidationService.isUpdateTimeNewerThanUser(photo.UpdateTime,db.getUserUpdateTime()))
                    continue;
                photos.add(photo);
            }
        }
        mainApiService.UpdatePhotos(photos, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(40);
                updateInns(mainApiService);
            }
            @Override
            public void onConflict() {
            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void updateSnils(MainApiService mainApiService) {
        List<Snils> SnilsList = new ArrayList<>();
        for (Item item :
                db.getAllItemsByUser("SNILS")) {
            Snils Snils = db.getSnilsById(item.Id);
            if(!ValidationService.isUpdateTimeNewerThanUser(Snils.UpdateTime,db.getUserUpdateTime()))
                continue;
            Snils.PhotoPage1 = ImageService.getPhoto(Snils.PhotoPage1);
            Snils.BirthDate = ValidationService.dateToIso(Snils.BirthDate);
            Snils.RegistrationDate = ValidationService.dateToIso(Snils.RegistrationDate);
            SnilsList.add(Snils);
        }
        mainApiService.UpdateSnils(SnilsList, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(30);
                updatePhotos(mainApiService);
            }
            @Override
            public void onConflict() { Log.e("Synced", "Snils" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Snils" + "No2" + errorMessage);
            }
        });
    }

    private void downloadBtnClick() {
        deleteItemList = new ArrayList<>();
        deleteTemplateList = new ArrayList<>();
        setLoadingWindowParams(true);
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetItems(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<Item>> typeToken = new TypeToken<List<Item>>() {
                };
                List<Item> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (Item item :
                        items) {
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + AppService.getUserId(SyncActivity.this) + "/Item" + item.Id;
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    item.Image = item.Type.equals("Collection")?"null_path":ImageService.getPhotoFile(item.Image, imgPath + "/ItemImage");
                    if (item.UpdateTime == null)
                        deleteItemList.add(item);
                    else if (db.getItemById(item.Id) == null)
                        db.insertItem(item, false);
                    else
                        db.updateItem(item.Id, item, false);
                }
                setProgressValue(20);
                getPassports();
            }

            @Override
            public void onConflict() {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });

    }
    private void getPassports() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetPassports(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<Passport>> typeToken = new TypeToken<List<Passport>>() {
                };
                List<Passport> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (Passport item :
                        items) {
                    item.GiveDate = ValidationService.reverseDateToIso(item.GiveDate);
                    item.BirthDate = ValidationService.reverseDateToIso(item.BirthDate);
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + AppService.getUserId(SyncActivity.this) + "/Item" + item.Id + "/Passport";
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    item.FacePhoto = ImageService.getPhotoFile(item.FacePhoto, imgPath + "/FacePhoto");
                    item.PhotoPage1 = ImageService.getPhotoFile(item.PhotoPage1, imgPath + "/PhotoPage1");
                    item.PhotoPage2 = ImageService.getPhotoFile(item.PhotoPage2, imgPath + "/PhotoPage2");
                    if (item.UpdateTime == null)
                        db.deletePassportConfirm(item.Id);
                    else if (db.getPassportById(item.Id) == null)
                        db.insertPassport(item);
                    else
                        db.updatePassport(item.Id, item, false);
                }
                setProgressValue(25);
                getSnils();
            }
            @Override
            public void onConflict() {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void getSnils() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetSnils(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<Snils>> typeToken = new TypeToken<List<Snils>>() {
                };
                List<Snils> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (Snils item :
                        items) {
                    item.BirthDate = ValidationService.reverseDateToIso(item.BirthDate);
                    item.RegistrationDate = ValidationService.reverseDateToIso(item.RegistrationDate);
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + AppService.getUserId(SyncActivity.this) + "/Item" + item.Id + "/Snils";
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    item.PhotoPage1 = ImageService.getPhotoFile(item.PhotoPage1, imgPath + "/PhotoPage1");
                    if (item.UpdateTime == null)
                        db.deleteSnilsConfirm(item.Id);
                    else if (db.getSnilsById(item.Id) == null)
                        db.insertSnils(item);
                    else
                        db.updateSnils(item.Id, item, false);
                }
                setProgressValue(30);
                getPhotos();
            }
            @Override
            public void onConflict() { Log.e("Synced", "Snils" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Snils" + "No2" + errorMessage);
            }
        });
    }

    private void getPhotos() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetPhotos(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<Photo>> typeToken = new TypeToken<List<Photo>>() {
                };
                List<Photo> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (Photo item :
                        items) {
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + AppService.getUserId(SyncActivity.this) + "/Item" + item.Id + "/Image";
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    item.Image = ImageService.getPhotoFile(item.Image, imgPath + "/Image" + item.Id);
                    if (item.UpdateTime == null)
                        db.deletePhotoConfirm(item.Id);
                    else if (db.getPhoto(item.Id) == null)
                        db.insertPhoto(item, false);
                    else
                        db.updatePhoto(item.Id, item, false);
                }
                setProgressValue(40);
                getInns();
            }
            @Override
            public void onConflict() { Log.e("Synced", "Photo" + "No1"); }
            @Override
            public void onError(String errorMessage) {
                Log.e("Synced","Photo" + "No2" + errorMessage);
            }
        });
    }

    private void getInns() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetInns(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<Inn>> typeToken = new TypeToken<List<Inn>>() {
                };
                List<Inn> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (Inn item :
                        items) {
                    item.BirthDate = ValidationService.reverseDateToIso(item.BirthDate);
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + AppService.getUserId(SyncActivity.this) + "/Item" + item.Id + "/Inn";
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    item.PhotoPage1 = ImageService.getPhotoFile(item.PhotoPage1, imgPath + "/PhotoPage1");
                    if (item.UpdateTime == null)
                        db.deleteInnConfirm(item.Id);
                    else if (db.getInnById(item.Id) == null)
                        db.insertInn(item);
                    else
                        db.updateInn(item.Id, item, false);
                }
                setProgressValue(45);
                getPolis();
            }
            @Override
            public void onConflict() {
            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void getPolis() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetPolis(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<Polis>> typeToken = new TypeToken<List<Polis>>() {
                };
                List<Polis> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (Polis item :
                        items) {
                    item.BirthDate = ValidationService.reverseDateToIso(item.BirthDate);
                    item.ValidUntil = ValidationService.reverseDateToIso(item.ValidUntil);
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + AppService.getUserId(SyncActivity.this) + "/Item" + item.Id + "/Inn";
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    item.PhotoPage1 = ImageService.getPhotoFile(item.PhotoPage1, imgPath + "/PhotoPage1");
                    item.PhotoPage2 = ImageService.getPhotoFile(item.PhotoPage2, imgPath + "/PhotoPage2");
                    if (item.UpdateTime == null)
                        db.deletePolisConfirm(item.Id);
                    else if (db.getPolisById(item.Id) == null)
                        db.insertPolis(item);
                    else
                        db.updatePolis(item.Id, item, false);
                }
                setProgressValue(50);
                getCreditCards();
            }
            @Override
            public void onConflict() {
            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void getCreditCards() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetCreditCards(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<CreditCard>> typeToken = new TypeToken<List<CreditCard>>() {
                };
                List<CreditCard> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (CreditCard item :
                        items) {
                    File rootDir = getApplicationContext().getFilesDir();
                    String imgPath = rootDir.getAbsolutePath() + "/" + MainContentActivity.APPLICATION_NAME + "/" + AppService.getUserId(SyncActivity.this) + "/Item" + item.Id + "/Inn";
                    File dir = new File(imgPath);
                    if (!dir.exists())
                        dir.mkdirs();
                    item.PhotoPage1 = ImageService.getPhotoFile(item.PhotoPage1, imgPath + "/PhotoPage1");
                    if (item.UpdateTime == null)
                        db.deleteCreditCardConfirm(item.Id);
                    else if (db.getCreditCardById(item.Id) == null)
                        db.insertCreditCard(item);
                    else
                        db.updateCreditCard(item.Id, item, false);
                }
                setProgressValue(55);
                getTemplates();
            }
            @Override
            public void onConflict() {
            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void getTemplateDocuments() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetTemplateDocuments(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<TemplateDocument>> typeToken = new TypeToken<List<TemplateDocument>>() {
                };
                List<TemplateDocument> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (TemplateDocument item :
                        items) {
                    if (item.UpdateTime == null)
                        db.deleteTemplateDocument(item.Id);
                    else if (db.getTemplateDocumentById(item.Id) == null)
                        db.insertTemplateDocument(item);
                    else
                        db.updateTemplateDocument(item.Id, item, false);
                }
                setProgressValue(80);
                getTemplateDocumentData();
            }
            @Override
            public void onConflict() {
            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void getTemplateDocumentData() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetTemplateDocumentData(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<TemplateDocumentData>> typeToken = new TypeToken<List<TemplateDocumentData>>() {
                };
                Log.e("Synced trace",decryptedData);
                List<TemplateDocumentData> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (TemplateDocumentData item :
                        items) {
                    if (item.UpdateTime == null)
                        db.deleteTemplateDocumentDataConfirm(item.Id);
                    else if (db.getTemplateDocumentDataById(item.Id) == null)
                        db.insertTemplateDocumentData(item, false);
                    else
                        db.updateTemplateDocumentData(item.Id, item, false);
                }
                setProgressValue(90);
                getTemplateObjects();
            }
            @Override
            public void onConflict() {
            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void getTemplates() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetTemplates(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<Template>> typeToken = new TypeToken<List<Template>>() {
                };
                List<Template> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (Template item :
                        items) {
                    if (item.UpdateTime == null)
                        db.deleteTemplate(item.Id);
                    else if (db.getTemplateById(item.Id) == null)
                        db.insertTemplate(item, false);
                    else
                        db.updateTemplate(item.Id, item, false);
                }
                setProgressValue(70);
                getTemplateDocuments();
            }
            @Override
            public void onConflict() {
            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void getTemplateObjects() {
        MainApiService mainApiService = new MainApiService(this);
        mainApiService.GetTemplateObjects(AppService.getUserId(this), db.getUserUpdateTime(), new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<TemplateObject>> typeToken = new TypeToken<List<TemplateObject>>() {
                };
                List<TemplateObject> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (TemplateObject item :
                        items) {
                    if (item.UpdateTime == null)
                        db.deleteTemplateObject(item.Id);
                    else if (db.getTemplateObjectsById(item.Id) == null)
                        db.insertTemplateObject(item, false);
                    else
                        db.updateTemplateObject(item.Id, item, false);
                }
                setProgressValue(100);
                db.setUserUpdateTime();
            }
            @Override
            public void onConflict() {
            }
            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    public void goBackSettingsClick(View view) {
        onBackPressed();
    }

    public class DelayedDialogTask  extends AsyncTask<Void, Void, Void> {
        private final Handler handler;
        private final Runnable showDialogRunnable;

        public DelayedDialogTask (Context context) {
            handler = new Handler();
            showDialogRunnable = () -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Warning")
                        .setMessage("Error on the server. Please try again later.\n" + "In case of repeated error, please contact us: mydocsapp.publicrelations@gmail.com")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle button click if needed
                                dialog.dismiss();
                                setProgressValue(100);
                            }
                        })
                        .show();
            };
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            handler.postDelayed(showDialogRunnable, 60000); // 1 minute in milliseconds
        }
    }
}