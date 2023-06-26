package com.example.mydocsapp.models;

import com.example.mydocsapp.interfaces.DatabaseObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class TemplateDocumentData implements DatabaseObject {
    public UUID Id;
    public String Value;
    public UUID TemplateObjectId;
    public UUID TemplateDocumentId;
    public String UpdateTime;

    public TemplateDocumentData(UUID id, String value, UUID templateObjectId, UUID templateDocumentId, String updateTime) {
        Id = id;
        Value = value;
        TemplateObjectId = templateObjectId;
        TemplateDocumentId = templateDocumentId;
        UpdateTime = updateTime;
    }

    @Override
    public void setValuesNull() {
        Value = UpdateTime = null;
    }
}
