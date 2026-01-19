package com.example.drivesmart;

/**
 * מחלקת המודל המייצגת טיפול ברכב.
 * כוללת את כל השדות הדרושים לתצוגה ברשימה, עריכה ושמירה ב-Firebase.
 */
public class Maintenance {
    // מזהה ייחודי שנוצר על ידי Firebase (ה-Key של הצומת)
    public String id;

    // שדות המידע של הטיפול
    public String title;
    public String description;
    public String dueDate;

    /**
     * בנאי (Constructor) ריק - חובה עבור Firebase Realtime Database.
     * Firebase משתמש בזה כדי להמיר את הנתונים מהענן לאובייקט Java.
     */
    public Maintenance() {
    }

    /**
     * בנאי ליצירת טיפול חדש.
     * @param title שם הטיפול
     * @param description תיאור הטיפול
     * @param dueDate תאריך יעד
     */
    public Maintenance(String title, String description, String dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    // הערה: ניתן להשאיר Getters ו-Setters אם אתה מעדיף,
    // אך כשמשתמשים במשתנים public, Firebase יודע לגשת אליהם ישירות.
}