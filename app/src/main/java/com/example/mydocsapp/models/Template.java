package com.example.mydocsapp.models;
public class Template {
    public int Id;
    public String Name;
    public String Status;
    public int UserId;
    public boolean isSelected;
    public Template(int id, String name, String status, int userId) {
        Id = id;
        this.Name = name;
        this.Status = status;
        this.UserId = userId;
        this.isSelected = false;
    }
}
