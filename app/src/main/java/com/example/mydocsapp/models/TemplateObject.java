package com.example.mydocsapp.models;

import com.example.mydocsapp.interfaces.DatabaseObject;

import java.util.UUID;

public class TemplateObject implements DatabaseObject {
    public UUID Id;
    public int Position;
    public String Type;
    public String Title;
    public UUID TemplateId;
    public String UpdateTime;

    public TemplateObject(UUID id, int position, String type, String title, UUID templateId, String updateTime) {
        Id = id;
        Position = position;
        Type = type;
        Title = title;
        TemplateId = templateId;
        UpdateTime = updateTime;
    }

    @Override
    public void setValuesNull() {
        Position = 0;
        Type = Title = UpdateTime = null;
    }
}
