package com.example.mydocsapp.models;public class TemplateDocumentData {
    public int Id;
    public String Value;
    public int TemplateObjectId;
    public int TemplateDocumentId;

    public TemplateDocumentData(int id, String value, int templateObjectId, int templateDocumentId) {
        Id = id;
        Value = value;
        TemplateObjectId = templateObjectId;
        TemplateDocumentId = templateDocumentId;
    }
}
