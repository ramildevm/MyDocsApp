package com.example.mydocsapp.models;

import com.example.mydocsapp.interfaces.DatabaseObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Photo implements DatabaseObject {
    public UUID Id;
    @SerializedName("Image64")
    @Expose
    public String Image;
    public UUID CollectionId;
    public String UpdateTime;

    public Photo(UUID id, String path, UUID collectionId, String updateTime) {
        Id = id;
        Image = path;
        CollectionId = collectionId;
        UpdateTime = updateTime;
    }

    @Override
    public void setValuesNull() {
        Image = UpdateTime = null;
    }
}
