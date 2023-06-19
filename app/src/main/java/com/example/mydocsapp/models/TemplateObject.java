package com.example.mydocsapp.models;

import java.util.UUID;

public class TemplateObject {
    public UUID Id;
    public int Position;
    public String Type;
    public String Title;
    public UUID TemplateId;

    public TemplateObject(UUID id, int position, String type, String title, UUID templateId) {
        Id = id;
        Position = position;
        Type = type;
        Title = title;
        TemplateId = templateId;
    }
}
