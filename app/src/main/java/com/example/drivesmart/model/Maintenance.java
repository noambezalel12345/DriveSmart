package com.example.drivesmart.model;

import java.util.Objects;

public class Maintenance {
    public String title;
    public String description;
    public String dueDate;
    public boolean isRecurring; // שדה חדש לטיפול חוזר

    public Maintenance() {}

    public Maintenance(String title, String description, String dueDate, boolean isRecurring) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isRecurring = isRecurring;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Maintenance that = (Maintenance) o;
        return isRecurring == that.isRecurring &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(dueDate, that.dueDate);
    }

    // בתוך Maintenance.java
    public Maintenance(String title, String description, String dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isRecurring = false; // ברירת מחדל
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, dueDate, isRecurring);
    }
}