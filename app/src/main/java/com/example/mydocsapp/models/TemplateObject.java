package com.example.mydocsapp.models;public class TemplateObject {
    public int Id;
    public int Position;
    public String Type;
    public String Title;
    public int TemplateId;

    public TemplateObject(int id, int position, String type, String title, int templateId) {
        Id = id;
        Position = position;
        Type = type;
        Title = title;
        TemplateId = templateId;
    }
}
