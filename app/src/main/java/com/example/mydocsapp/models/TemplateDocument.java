package com.example.mydocsapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class TemplateDocument implements Parcelable {
    public int Id;
    public int TemplateId;
    public TemplateDocument(int id, int templateId) {
        Id = id;
        TemplateId = templateId;
    }

    protected TemplateDocument(Parcel in) {
        Id = in.readInt();
        TemplateId = in.readInt();
    }

    public static final Creator<TemplateDocument> CREATOR = new Creator<TemplateDocument>() {
        @Override
        public TemplateDocument createFromParcel(Parcel in) {
            return new TemplateDocument(in);
        }

        @Override
        public TemplateDocument[] newArray(int size) {
            return new TemplateDocument[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(Id);
        parcel.writeInt(TemplateId);
    }
}