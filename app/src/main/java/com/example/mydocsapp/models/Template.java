package com.example.mydocsapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Template implements Parcelable {
    public int Id;
    public String Name;
    public String Status;
    public String Date;
    public int UserId;
    public boolean isSelected;
    public Template(int id, String name, String status, String date, int userId) {
        Id = id;
        this.Name = name;
        this.Date = date;
        this.Status = status;
        this.UserId = userId;
        this.isSelected = false;
    }

    protected Template(Parcel in) {
        Id = in.readInt();
        Name = in.readString();
        Status = in.readString();
        Date = in.readString();
        UserId = in.readInt();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<Template> CREATOR = new Creator<Template>() {
        @Override
        public Template createFromParcel(Parcel in) {
            return new Template(in);
        }

        @Override
        public Template[] newArray(int size) {
            return new Template[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(Id);
        parcel.writeString(Name);
        parcel.writeString(Status);
        parcel.writeString(Date);
        parcel.writeInt(UserId);
        parcel.writeByte((byte) (isSelected ? 1 : 0));
    }
}
