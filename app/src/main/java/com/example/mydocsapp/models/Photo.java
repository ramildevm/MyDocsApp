package com.example.mydocsapp.models;
public class Photo {
    public int Id;
    public String Path;
    public int CollectionId;
    public Photo(int id, String path, int collectionId) {
        Id = id;
        Path = path;
        CollectionId = collectionId;
    }
}
