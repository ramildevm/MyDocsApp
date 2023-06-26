package com.example.mydocsapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.mydocsapp.interfaces.DatabaseObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Item implements Parcelable, DatabaseObject {
    public UUID Id;
    public String Title;
    public String Type;
    @SerializedName("Image64")
    @Expose
    public String Image;
    public int Priority;
    public int IsHidden;
    public int isSelected;
    public String DateCreation;
    public UUID FolderId;
    public int UserId;
    public String UpdateTime;

    public Item(UUID Id_,String Title_,String Type_,String Image_,int Priority_,int IsHidden_, int isSelected_, String DateCreation_,UUID FolderId_,int UserId_, String UpdateTime_)
    {
        this.Id = Id_;
        this.Title = Title_;
        this.Type = Type_;
        this.Image = Image_;
        this.Priority = Priority_;
        this.IsHidden = IsHidden_;
        this.isSelected = isSelected_;
        this.DateCreation = DateCreation_;
        this.FolderId = FolderId_;
        this.UserId = UserId_;
        this.UpdateTime = UpdateTime_;
    }
    public void setValuesNull(){
        this.Title = "";
        this.Type = "";
        this.Image = null;
        this.Priority = 0;
        this.IsHidden = 0;
        this.isSelected = 0;
        this.UpdateTime = null;
    }
    protected Item(Parcel in) {
        Id = UUID.fromString(in.readString());
        Title = in.readString();
        Type = in.readString();
        Image = in.readString();
        Priority = in.readInt();
        IsHidden = in.readInt();
        isSelected = in.readInt();
        DateCreation = in.readString();
        FolderId = UUID.fromString(in.readString());
        UserId = in.readInt();
        UpdateTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Id.toString());
        parcel.writeString(Title);
        parcel.writeString(Type);
        parcel.writeString(Image);
        parcel.writeInt(Priority);
        parcel.writeInt(IsHidden);
        parcel.writeInt(isSelected);
        parcel.writeString(DateCreation);
        parcel.writeString(FolderId.toString());
        parcel.writeInt(UserId);
        parcel.writeString(UpdateTime);
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
