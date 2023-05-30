package com.example.mydocsapp.models;
public class Template {
    public int Id;
    public String Name;
    public String Status;
    public String Date;
    public int UserId;
    public boolean isSelected;
    public Template(int id, String name, String status, String date, int userId) {
        Id = id;
        this.Name = name;
        this.Date = date;
        this.Status = status;
        this.UserId = userId;
        this.isSelected = false;
    }
}
