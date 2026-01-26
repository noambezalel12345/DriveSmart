package com.example.drivesmart;

public class Maintenance {
    public String id; // שדה חובה למזהה הייחודי
    public String title;
    public String description;
    public String dueDate;

    public Maintenance() {} // קונסטרקטור ריק ל-Firebase

    public Maintenance(String title, String description, String dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }
}