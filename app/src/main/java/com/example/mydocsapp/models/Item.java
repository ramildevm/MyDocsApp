package com.example.mydocsapp.models;
public class Item {
    public int Id;
    public String Title;
    public String Type;
    public byte[] Image;
    public int Priority;
    public int isHiden;
    public int FolderId;
    public int ObjectId;
    public int isSelected;

    public Item(int id,String title, String type, byte[] image, int priority, int isHiden, int folderId, int objectId, int isSelected) {
        Id = id;
        Title = title;
        Type = type;
        Image = image;
        Priority = priority;
        this.isHiden = isHiden;
        FolderId = folderId;
        ObjectId = objectId;
        this.isSelected = isSelected;
    }
}
