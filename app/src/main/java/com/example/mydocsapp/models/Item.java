package com.example.mydocsapp.models;
public class Item {
    private int Id;
    private String Title;
    private int Image;
    private boolean hasImage;
    private String Type;
    public Item(int id, String title, int image, boolean hasImage, String type) {
        Id = id;
        Title = title;
        Image = image;
        hasImage = hasImage;
        Type = type;
    }
    public int getId() {
        return Id;
    }
    public String getTitle() {
        return Title;
    }
    public int getImage() {
        return Image;
    }
    public String getType() {
        return Type;
    }
    public boolean isHasImage() {
        return hasImage;
    }
}
