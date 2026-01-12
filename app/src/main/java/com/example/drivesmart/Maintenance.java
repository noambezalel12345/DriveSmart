package com.example.drivesmart;

public class Maintenance {
    private String title;
    private String description;
    private String dueDate;

    // Constructor ריק ל-Firebase
    public Maintenance() {}

    public Maintenance(String title, String description, String dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDueDate() { return dueDate; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
