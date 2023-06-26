package com.example.mydocsapp.api;

import com.example.mydocsapp.models.EncryptedResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/users/byemail")
    Call<EncryptedResponse> getEncryptedUserByEmailAndPassword(@Query("email") String email, @Query("password") String password);
    @POST("api/users")
    Call<EncryptedResponse> postEncryptedUser(@Body EncryptedResponse encryptedResponse);
    @PUT("api/users/{id}")
    Call<EncryptedResponse> updateUser(@Path("id") int id, @Body EncryptedResponse encryptedResponse);
    @GET("api/items")
    Call<EncryptedResponse> getEncryptedItems(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/items")
    Call<EncryptedResponse> postEncryptedItems(@Body EncryptedResponse encryptedResponse);
    @POST("api/passports")
    Call<EncryptedResponse> postEncryptedPassports(@Body EncryptedResponse encryptedResponse);
    @GET("api/passports")
    Call<EncryptedResponse> getEncryptedPassports(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/snils")
    Call<EncryptedResponse> postEncryptedSnils(@Body EncryptedResponse encryptedResponse);
    @GET("api/snils")
    Call<EncryptedResponse> getEncryptedSnils(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/photos")
    Call<EncryptedResponse> postEncryptedPhotos(@Body EncryptedResponse encryptedResponse);
    @GET("api/photos")
    Call<EncryptedResponse> getEncryptedPhotos(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/inns")
    Call<EncryptedResponse> postEncryptedInns(@Body EncryptedResponse encryptedResponse);
    @GET("api/inns")
    Call<EncryptedResponse> getEncryptedInns(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/polis")
    Call<EncryptedResponse> postEncryptedPolis(@Body EncryptedResponse encryptedResponse);
    @GET("api/polis")
    Call<EncryptedResponse> getEncryptedPolis(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/creditcards")
    Call<EncryptedResponse> postEncryptedCreditCards(@Body EncryptedResponse encryptedResponse);
    @GET("api/creditcards")
    Call<EncryptedResponse> getEncryptedCreditCards(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/templatedocuments")
    Call<EncryptedResponse> postEncryptedTemplateDocuments(@Body EncryptedResponse encryptedResponse);
    @GET("api/templatedocuments")
    Call<EncryptedResponse> getEncryptedTemplateDocuments(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/templatedocumentdata")
    Call<EncryptedResponse> postEncryptedTemplateDocumentData(@Body EncryptedResponse encryptedResponse);
    @GET("api/templatedocumentdata")
    Call<EncryptedResponse> getEncryptedTemplateDocumentData(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/templates")
    Call<EncryptedResponse> postEncryptedTemplates(@Body EncryptedResponse encryptedResponse);
    @GET("api/templates")
    Call<EncryptedResponse> getEncryptedTemplates(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
    @POST("api/templateobjects")
    Call<EncryptedResponse> postEncryptedTemplateObjects(@Body EncryptedResponse encryptedResponse);
    @GET("api/templateobjects")
    Call<EncryptedResponse> getEncryptedTemplateObjects(@Query("userId") int userId, @Query("updateTimeString") String updateTimeString);
}
