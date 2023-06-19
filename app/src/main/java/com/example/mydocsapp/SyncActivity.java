package com.example.mydocsapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mydocsapp.api.MainApiService;
import com.example.mydocsapp.api.ResponseCallback;
import com.example.mydocsapp.apputils.ImageService;
import com.example.mydocsapp.databinding.ActivitySyncBinding;
import com.example.mydocsapp.interfaces.IItemAdapterActivity;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SyncActivity extends AppCompatActivity {
    ActivitySyncBinding binding;
    private DBHelper db;
    int progress = 0;
    List<Item> deleteItemList = new ArrayList<>();
    List<Template> deleteTemplateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyncBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = new DBHelper(this, AppService.getUserId(this));
        binding.menubarBack.setOnClickListener(v -> goBackSettingsClick(v));
        binding.uploadBtn.setOnClickListener(v -> uploadBtnClick());
        binding.downloadBtn.setOnClickListener(v -> downloadBtnClick());

    }

    private void uploadBtnClick() {
        setLoadingWindowParams(true);
        MainApiService mainApiService = new MainApiService(this);
        List<Item> Items = db.getItemsByUser();
        for (Item item :
                Items) {
            item.Image = ImageService.getPhoto(item.Image);
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
            TemplateDocumentList.add(_TemplateDocument);
        }
        mainApiService.UpdateTemplateDocuments(TemplateDocumentList, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(70);
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
            templateDocumentData.addAll(db.getTemplateDocumentDataByDoc(tempDoc.Id));
        }
        mainApiService.UpdateTemplateDocumentData(templateDocumentData, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(80);
                updateTemplates(mainApiService);
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
        for (Item item :
                db.getAllItemsByUser("Polis")) {
            Polis _Polis = db.getPolisById(item.Id);
            _Polis.PhotoPage1 = ImageService.getPhoto(_Polis.PhotoPage1);
            _Polis.BirthDate = dateToIso(_Polis.BirthDate);
            PolisList.add(_Polis);
        }
        mainApiService.UpdatePolis(PolisList, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(55);
                updateTemplateDocuments(mainApiService);
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
        List<Template> TemplateList = db.getAllTemplatesByUser();
        mainApiService.UpdateTemplates(TemplateList, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(90);
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
        mainApiService.UpdateTemplateObjects(templateObjects, new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(100);
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
                db.getAllItemsByUser("Inn")) { //TODO:Chek type
            Inn _Inn = db.getInnById(item.Id);
            _Inn.PhotoPage1 = ImageService.getPhoto(_Inn.PhotoPage1);
            _Inn.BirthDate = dateToIso(_Inn.BirthDate);
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
    }

    private void updatePassports(MainApiService mainApiService) {
        List<Passport> passports = new ArrayList<>();
        for (Item item :
                db.getAllItemsByUser("Passport")) {
            Passport passport = db.getPassportById(item.Id);
            passport.FacePhoto = ImageService.getPhoto(passport.FacePhoto);
            passport.PhotoPage1 = ImageService.getPhoto(passport.PhotoPage1);
            passport.PhotoPage2 = ImageService.getPhoto(passport.PhotoPage2);

            passport.BirthDate = dateToIso(passport.BirthDate);
            passport.GiveDate = dateToIso(passport.GiveDate);
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
                db.getAllItemsByUser("Snils")) {
            Snils Snils = db.getSnilsById(item.Id);
            Snils.PhotoPage1 = ImageService.getPhoto(Snils.PhotoPage1);
            Snils.BirthDate = dateToIso(Snils.BirthDate);
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
        mainApiService.GetItems(AppService.getUserId(this), "2023-05-28 09:30:00", new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                setProgressValue(20);
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
                    item.Image = ImageService.getPhotoFile(item.Image, imgPath + "/ItemImage");
                    if (item.UpdateTime == null)
                        deleteItemList.add(item);
                    else if (db.getItemById(item.Id) == null)
                        db.insertItem(item, false);
                    else
                        db.updateItem(item.Id, item, false);
                }
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
        mainApiService.GetPassports(AppService.getUserId(this), "2023-05-28 09:30:00", new ResponseCallback() {
            @Override
            public void onSuccess(String encryptedData) {
                String decryptedData = CryptoService.Decrypt(encryptedData);
                TypeToken<List<Passport>> typeToken = new TypeToken<List<Passport>>() {
                };
                List<Passport> items = new Gson().fromJson(decryptedData, typeToken.getType());
                for (Passport item :
                        items) {
                    item.GiveDate = reverseDateToIso(item.GiveDate);
                    item.BirthDate = reverseDateToIso(item.BirthDate);
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
                setProgressValue(100);
            }
            @Override
            public void onConflict() {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private String dateToIso(String dateString) {
        if (dateString == null)
            return null;
        SimpleDateFormat inputFormatter = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = inputFormatter.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return outputFormatter.format(date);
    }

    private String reverseDateToIso(String dateString) {
        if (dateString == null)
            return null;
        SimpleDateFormat outputFormatter = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = inputFormatter.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return outputFormatter.format(date);
    }

    public void goBackSettingsClick(View view) {
        onBackPressed();
    }
}