package com.example.mydocsapp.api;

import android.content.Context;
import android.util.Log;

import com.example.mydocsapp.models.CreditCard;
import com.example.mydocsapp.models.EncryptedResponse;
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
import com.example.mydocsapp.services.CryptoService;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainApiService {

    private final ApiService apiService;
    private Context context;
    private Callback<EncryptedResponse> encryptedResponseCallback;
    private  ResponseCallback callback;

    public MainApiService(Context _context) {
        context = _context;
        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("http://192.168.43.16:5000/")
                //.baseUrl("http://192.168.0.102:5000/")
                .baseUrl("http://ramildevm-001-site1.htempurl.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
        encryptedResponseCallback = new Callback<EncryptedResponse>() {
            @Override
            public void onResponse(Call<EncryptedResponse> call, Response<EncryptedResponse> response) {
                if (response.isSuccessful()) {
                    String encryptedData = response.body().getEncryptedData();
                    callback.onSuccess(encryptedData);
                }
                else if(response.code()==409){
                    callback.onConflict();
                }
                else {
                    callback.onError("Uploading failed. Please try again." + response.message());
                }
            }

            @Override
            public void onFailure(Call<EncryptedResponse> call, Throwable t) {
                callback.onError("An error occurred while Uploading. "+ t.getMessage() +"\n" +t.getLocalizedMessage());
            }
        };
    }
    public void CreateUser(User user, ResponseCallback _callback){
        String json = new Gson().toJson(user);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);

        Call<EncryptedResponse> call = apiService.postEncryptedUser(encryptedResponse);
        call.enqueue(new Callback<EncryptedResponse>() {
            @Override
            public void onResponse(Call<EncryptedResponse> call, Response<EncryptedResponse> response) {
                if (response.isSuccessful()) {
                    String encryptedData = response.body().getEncryptedData();
                    _callback.onSuccess(encryptedData);
                }
                else if(response.code()==409){
                    _callback.onConflict();
                }
                else {
                    _callback.onError("User addition failed. Please try again." + response.message());
                }
            }

            @Override
            public void onFailure(Call<EncryptedResponse> call, Throwable t) {
                _callback.onError("An error occurred while adding the user. "+ t.getMessage() +"\n" +t.getLocalizedMessage());
            }
        });
    }
    public void GetUser(String email, String password, ResponseCallback callback){
        Call<EncryptedResponse> call = apiService.getEncryptedUserByEmailAndPassword(email,password);
        call.enqueue(new Callback<EncryptedResponse>() {
            @Override
            public void onResponse(Call<EncryptedResponse> call, Response<EncryptedResponse> response) {
                if (response.isSuccessful()) {
                    String encryptedData = response.body().getEncryptedData();
                    callback.onSuccess(encryptedData);
                }
                else if(response.code()==409){
                    callback.onConflict();
                }
                else {
                    callback.onError("User addition failed. Please try again." + response.message());
                }
            }

            @Override
            public void onFailure(Call<EncryptedResponse> call, Throwable t) {
                callback.onError("An error occurred while adding the user." + t.getMessage());
            }
        });
    }
    public void GetItems(int userId, String date, ResponseCallback callback){
        Call<EncryptedResponse> call = apiService.getEncryptedItems(userId,date);
        call.enqueue(new Callback<EncryptedResponse>() {
            @Override
            public void onResponse(Call<EncryptedResponse> call, Response<EncryptedResponse> response) {
                if (response.isSuccessful()) {
                    String encryptedData = response.body().getEncryptedData();
                    callback.onSuccess(encryptedData);
                }
                else if(response.code()==409){
                    callback.onConflict();
                }
                else {
                    callback.onError("Item addition failed. Please try again." + response.message());
                }
            }

            @Override
            public void onFailure(Call<EncryptedResponse> call, Throwable t) {
                callback.onError("An error occurred while adding the items." + t.getMessage());
            }
        });
    }
    public void UpdateItems(List<Item> items, ResponseCallback callback) {
        String json = new Gson().toJson(items);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedItems(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }

    public void UpdatePassports(List<Passport> passports, ResponseCallback callback) {
        String json = new Gson().toJson(passports);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedPassports(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }

    public void GetPassports(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedPassports(userId, date);
        executeEncryptedRequest(call, callback);
    }

    public void UpdateSnils(List<Snils> snilsList, ResponseCallback callback) {
        String json = new Gson().toJson(snilsList);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedSnils(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
    public void GetSnils(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedSnils(userId, date);
        executeEncryptedRequest(call, callback);
    }
    public void UpdateInns(List<Inn> innsList, ResponseCallback callback) {
        String json = new Gson().toJson(innsList);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedInns(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
    public void GetInns(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedInns(userId, date);
        executeEncryptedRequest(call, callback);
    }
    public void UpdateTemplates(List<Template> TemplateList, ResponseCallback callback) {
        String json = new Gson().toJson(TemplateList);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedTemplates(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
    public void GetTemplates(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedTemplates(userId, date);
        executeEncryptedRequest(call, callback);
    }
    public void UpdateTemplateObjects(List<TemplateObject> TemplateObjectList, ResponseCallback callback) {
        String json = new Gson().toJson(TemplateObjectList);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedTemplateObjects(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
    public void GetTemplateObjects(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedTemplateObjects(userId, date);
        executeEncryptedRequest(call, callback);
    }
    public void UpdateCreditCards(List<CreditCard> CreditCardList, ResponseCallback callback) {
        String json = new Gson().toJson(CreditCardList);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedCreditCards(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
    public void GetCreditCards(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedCreditCards(userId, date);
        executeEncryptedRequest(call, callback);
    }
    public void UpdatePolis(List<Polis> PolisList, ResponseCallback callback) {
        String json = new Gson().toJson(PolisList);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedPolis(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
    public void GetPolis(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedPolis(userId, date);
        executeEncryptedRequest(call, callback);
    }
    public void UpdateTemplateDocuments(List<TemplateDocument> TemplateDocumentList, ResponseCallback callback) {
        String json = new Gson().toJson(TemplateDocumentList);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedTemplateDocuments(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
    public void GetTemplateDocuments(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedTemplateDocuments(userId, date);
        executeEncryptedRequest(call, callback);
    }
    public void UpdateTemplateDocumentData(List<TemplateDocumentData> TemplateDocumentDataList, ResponseCallback callback) {
        String json = new Gson().toJson(TemplateDocumentDataList);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedTemplateDocumentData(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
    public void GetTemplateDocumentData(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedTemplateDocumentData(userId, date);
        executeEncryptedRequest(call, callback);
    }
    public void UpdatePhotos(List<Photo> photosList, ResponseCallback callback) {
        String json = new Gson().toJson(photosList);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.postEncryptedPhotos(encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
    public void GetPhotos(int userId, String date, ResponseCallback callback) {
        Call<EncryptedResponse> call = apiService.getEncryptedPhotos(userId, date);
        executeEncryptedRequest(call, callback);
    }

    private void executeEncryptedRequest(Call<EncryptedResponse> call, ResponseCallback callback) {
        call.enqueue(new Callback<EncryptedResponse>() {
            @Override
            public void onResponse(Call<EncryptedResponse> call, Response<EncryptedResponse> response) {
                if (response.isSuccessful()) {
                    String encryptedData = response.body().getEncryptedData();
                    callback.onSuccess(encryptedData);
                } else if (response.code() == 409) {
                    callback.onConflict();
                } else {
                    callback.onError("Data addition failed. Please try again." + response.message());
                }
            }

            @Override
            public void onFailure(Call<EncryptedResponse> call, Throwable t) {
                callback.onError("An error occurred while adding the data. " + t.getMessage() + "\n" + t.getLocalizedMessage());
            }
        });
    }

    public void UpdateUser(User user, ResponseCallback callback) {
        String json = new Gson().toJson(user);
        String text = CryptoService.Encrypt(json);
        EncryptedResponse encryptedResponse = new EncryptedResponse();
        encryptedResponse.setEncryptedData(text);
        Call<EncryptedResponse> call = apiService.updateUser(user.Id, encryptedResponse);
        executeEncryptedRequest(call, callback);
    }
}
