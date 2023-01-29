package com.example.mydocsapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {
    public int Id;
    public String Title;
    public String Type;
    public String Image ;
    public int Priority;
    public int isHiden;
    public int isSelected;
    public String DateCreation;
    public int FolderId;
    public int UserId;

    public Item(int Id_,String Title_,String Type_,String Image_,int Priority_,int isHiden_,int isSelected_,String DateCreation_,int FolderId_,int UserId_)
    {
        this.Id = Id_;
        this.Title = Title_;
        this.Type = Type_;
        this.Image = Image_;
        this.Priority = Priority_;
        this.isHiden = isHiden_;
        this.isSelected = isSelected_;
        this.DateCreation = DateCreation_;
        this.FolderId = FolderId_;
        this.UserId = UserId_;
    }

    protected Item(Parcel in) {
        Id = in.readInt();
        Title = in.readString();
        Type = in.readString();
        Image = in.readString();
        Priority = in.readInt();
        isHiden = in.readInt();
        isSelected = in.readInt();
        DateCreation = in.readString();
        FolderId = in.readInt();
        UserId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(Id);
        parcel.writeString(Title);
        parcel.writeString(Type);
        parcel.writeString(Image);
        parcel.writeInt(Priority);
        parcel.writeInt(isHiden);
        parcel.writeInt(isSelected);
        parcel.writeString(DateCreation);
        parcel.writeInt(FolderId);
        parcel.writeInt(UserId);
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

}
