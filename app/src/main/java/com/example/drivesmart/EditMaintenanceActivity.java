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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
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

public class EditMaintenanceActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate, etDueTime;
    private SwitchMaterial swRecurring;
    private Button btnSave, btnDelete;
    private ProgressBar loader;
    private FirebaseFirestore db;
    private String userId;
    private Maintenance originalMaintenance;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_maintenance);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        // קישור IDs - וודא שהם תואמים ל-XML שלך
        etTitle = findViewById(R.id.etEditTitle);
        etDescription = findViewById(R.id.etEditDescription);
        etDueDate = findViewById(R.id.etEditDueDate);
        etDueTime = findViewById(R.id.etEditDueTime);
        swRecurring = findViewById(R.id.swEditRecurring);
        btnSave = findViewById(R.id.btnSaveEdit);
        btnDelete = findViewById(R.id.btnDeleteEdit);
        loader = findViewById(R.id.editLoader);

        // קבלת נתונים מה-Intent
        String fullDateTime = getIntent().getStringExtra("DUE_DATE");
        String title = getIntent().getStringExtra("TITLE");
        String description = getIntent().getStringExtra("DESCRIPTION");
        boolean isRecurring = getIntent().getBooleanExtra("IS_RECURRING", false);

        originalMaintenance = new Maintenance(title, description, fullDateTime, isRecurring);
        swRecurring.setChecked(isRecurring);

        if (fullDateTime != null && fullDateTime.contains(" ")) {
            String[] parts = fullDateTime.split(" ");
            etDueDate.setText(parts[0]);
            etDueTime.setText(parts[1]);
        }

        etTitle.setText(title);
        etDescription.setText(description);

        // בחירת תאריך עם חסימת עבר
        etDueDate.setOnClickListener(v -> {
            DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDueDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            // חסימת תאריכים שעברו (מינוס 1000 מילישניות לביטחון)
            dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dp.show();
        });

        etDueTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                etDueTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        btnSave.setOnClickListener(v -> updateMaintenance());
        btnDelete.setOnClickListener(v -> deleteMaintenance());

        if (findViewById(R.id.btnBackEdit) != null) {
            findViewById(R.id.btnBackEdit).setOnClickListener(v -> finish());
        }
    }

    private void updateMaintenance() {
        String title = etTitle.getText().toString().trim();
        String date = etDueDate.getText().toString().trim();
        String time = etDueTime.getText().toString().trim();
        String fullDateTime = date + " " + time;
        boolean isRecurringNow = swRecurring.isChecked();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loader.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        DocumentReference userRef = db.collection("users").document(userId);

        Maintenance updated = new Maintenance(title, etDescription.getText().toString().trim(), fullDateTime, isRecurringNow);

        userRef.update("maintenances", FieldValue.arrayRemove(originalMaintenance))
                .addOnSuccessListener(aVoid -> {
                    userRef.update("maintenances", FieldValue.arrayUnion(updated))
                            .addOnSuccessListener(aVoid2 -> {
                                setAlarm(title, updated.description, fullDateTime);
                                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    loader.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteMaintenance() {
        db.collection("users").document(userId).update("maintenances", FieldValue.arrayRemove(originalMaintenance))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(String title, String description, String dateTimeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateTimeStr);
            if (date == null) return;

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", "Car Maintenance: " + title);
            intent.putExtra("message", description);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) date.getTime(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}