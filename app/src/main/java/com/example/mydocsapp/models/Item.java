package com.example.mydocsapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {
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

    protected Item(Parcel in) {
        Id = in.readInt();
        Title = in.readString();
        Type = in.readString();
        Image = in.createByteArray();
        Priority = in.readInt();
        isHiden = in.readInt();
        FolderId = in.readInt();
        ObjectId = in.readInt();
        isSelected = in.readInt();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(Id);
        parcel.writeString(Title);
        parcel.writeString(Type);
        parcel.writeByteArray(Image);
        parcel.writeInt(Priority);
        parcel.writeInt(isHiden);
        parcel.writeInt(FolderId);
        parcel.writeInt(ObjectId);
        parcel.writeInt(isSelected);
    }
}
