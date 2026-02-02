package com.example.drivesmart;

public class Maintenance {
    public String title;
    public String description;
    public String dueDate;

    // חובה Constructor ריק עבור Firebase
    public Maintenance() {}

    public Maintenance(String title, String description, String dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    // כאן צריכות להופיע גם פונקציות ה-equals וה-hashCode שנתתי לך קודם
}