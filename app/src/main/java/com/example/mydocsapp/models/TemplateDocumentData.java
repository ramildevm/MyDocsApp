package com.example.mydocsapp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class TemplateDocumentData {
    public UUID Id;
    public String Value;
    public UUID TemplateObjectId;
    public UUID TemplateDocumentId;

    public TemplateDocumentData(UUID id, String value, UUID templateObjectId, UUID templateDocumentId) {
        Id = id;
        Value = value;
        TemplateObjectId = templateObjectId;
        TemplateDocumentId = templateDocumentId;
    }
}
