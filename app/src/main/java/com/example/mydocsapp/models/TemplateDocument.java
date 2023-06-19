package com.example.mydocsapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class TemplateDocument implements Parcelable {
    public UUID Id;
    public UUID TemplateId;
    public String UpdateTime;
    public TemplateDocument(UUID id, UUID templateId, String updateTime) {
        Id = id;
        TemplateId = templateId;
        UpdateTime = updateTime;
    }

    protected TemplateDocument(Parcel in) {
        Id = UUID.fromString(in.readString());
        TemplateId = UUID.fromString(in.readString());
        UpdateTime = in.readString();
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
        parcel.writeString(Id.toString());
        parcel.writeString(TemplateId.toString());
        parcel.writeString(UpdateTime);
    }
}