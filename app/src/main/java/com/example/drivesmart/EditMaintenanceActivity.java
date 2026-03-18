package com.example.drivesmart;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.drivesmart.model.Maintenance;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EditMaintenanceActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate, etDueTime;
    private SwitchMaterial swRecurring;
    private Button btnSave, btnDelete;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private String userId;
    private Maintenance originalMaintenance;
    private Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_maintenance);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        etTitle = findViewById(R.id.etEditTitle);
        etDescription = findViewById(R.id.etEditDescription);
        etDueDate = findViewById(R.id.etEditDueDate);
        etDueTime = findViewById(R.id.etEditDueTime);
        swRecurring = findViewById(R.id.swEditRecurring);
        btnSave = findViewById(R.id.btnSaveEdit);
        btnDelete = findViewById(R.id.btnDeleteEdit);
        btnBack = findViewById(R.id.btnBackEdit);

        // קבלת נתונים מה-Intent
        String title = getIntent().getStringExtra("TITLE");
        String description = getIntent().getStringExtra("DESCRIPTION");
        String fullDateTime = getIntent().getStringExtra("DUE_DATE");
        boolean isRecurring = getIntent().getBooleanExtra("IS_RECURRING", false);

        originalMaintenance = new Maintenance(title, description, fullDateTime, isRecurring);

        etTitle.setText(title);
        etDescription.setText(description);
        swRecurring.setChecked(isRecurring);

        if (fullDateTime != null && fullDateTime.contains(" ")) {
            String[] parts = fullDateTime.split(" ");
            etDueDate.setText(parts[0]);
            etDueTime.setText(parts[1]);
        }

        // בחירת תאריך
        etDueDate.setOnClickListener(v -> {
            DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDueDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dp.getDatePicker().setMinDate(System.currentTimeMillis());
            dp.show();
        });

        // בחירת שעה
        etDueTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tp = new TimePickerDialog(this, (view, h, min) -> {
                etDueTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, min));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
            tp.show();
        });

        btnSave.setOnClickListener(v -> updateMaintenance());
        btnDelete.setOnClickListener(v -> deleteMaintenance());
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateMaintenance() {
        String title = etTitle.getText().toString().trim();
        String date = etDueDate.getText().toString().trim();
        String time = etDueTime.getText().toString().trim();
        String fullDateTime = date + " " + time;

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) return;

        DocumentReference userRef = db.collection("users").document(userId);
        Maintenance updated = new Maintenance(title, etDescription.getText().toString().trim(), fullDateTime, swRecurring.isChecked());

        userRef.update("maintenances", FieldValue.arrayRemove(originalMaintenance))
                .addOnSuccessListener(aVoid -> {
                    userRef.update("maintenances", FieldValue.arrayUnion(updated))
                            .addOnSuccessListener(aVoid2 -> finish());
                });
    }

    private void deleteMaintenance() {
        // אם הטיפול מחזורי, נציע אופציה לחדש לשנה הבאה
        if (originalMaintenance.isRecurring) {
            new AlertDialog.Builder(this)
                    .setTitle("סיום טיפול מחזורי")
                    .setMessage("האם ברצונך למחוק את הטיפול לגמרי, או לחדש אותו לשנה הבאה?")
                    .setPositiveButton("חדש לשנה הבאה", (dialog, which) -> renewForNextYear())
                    .setNegativeButton("מחק הכל", (dialog, which) -> performFullDelete())
                    .setNeutralButton("ביטול", null)
                    .show();
        } else {
            // אם לא מחזורי, פשוט מוחקים
            performFullDelete();
        }
    }

    private void performFullDelete() {
        db.collection("users").document(userId)
                .update("maintenances", FieldValue.arrayRemove(originalMaintenance))
                .addOnSuccessListener(aVoid -> finish());
    }

    private void renewForNextYear() {
        String nextYearDate = calculateNextYear(originalMaintenance.dueDate);
        Maintenance nextYearMaintenance = new Maintenance(
                originalMaintenance.title,
                originalMaintenance.description,
                nextYearDate,
                true
        );

        DocumentReference userRef = db.collection("users").document(userId);
        // מוחקים את הנוכחי ומוסיפים את החדש עם התאריך המעודכן
        userRef.update("maintenances", FieldValue.arrayRemove(originalMaintenance))
                .addOnSuccessListener(aVoid -> {
                    userRef.update("maintenances", FieldValue.arrayUnion(nextYearMaintenance))
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "הטיפול חודש לשנה הבאה!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                });
    }

    private String calculateNextYear(String currentDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(currentDateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.YEAR, 1); // מוסיף שנה אחת
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return currentDateStr; // במקרה של שגיאה מחזיר את המקורי
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(String title, String dateTimeStr) {
        // ... (הלוגיקה של ההתראה נשארת כפי שהייתה)
    }
}