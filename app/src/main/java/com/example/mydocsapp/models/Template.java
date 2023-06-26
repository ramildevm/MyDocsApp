package com.example.mydocsapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.mydocsapp.interfaces.DatabaseObject;

import java.util.UUID;

public class Template implements Parcelable, DatabaseObject {
    public UUID Id;
    public String Name;
    public String Status;
    public String Date;
    public String UpdateTime;
    public int UserId;
    public boolean isSelected;

    public Template(UUID id, String name, String status, String date, String updateTime, int userId) {
        Id = id;
        Name = name;
        Status = status;
        Date = date;
        UpdateTime = updateTime;
        UserId = userId;
        this.isSelected = false;
    }

    protected Template(Parcel in) {
        Id = UUID.fromString(in.readString());
        Name = in.readString();
        Status = in.readString();
        Date = in.readString();
        UserId = in.readInt();
        isSelected = in.readByte() != 0;
        UpdateTime = in.readString();
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
        parcel.writeString(Id.toString());
        parcel.writeString(Name);
        parcel.writeString(Status);
        parcel.writeString(Date);
        parcel.writeInt(UserId);
        parcel.writeByte((byte) (isSelected ? 1 : 0));
        parcel.writeString(UpdateTime);
    }

    @Override
    public void setValuesNull() {
        Name = Status = Date =UpdateTime = null;
    }
}
