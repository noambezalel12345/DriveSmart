package com.example.drivesmart;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.drivesmart.model.Maintenance;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddMaintenance extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate, etDueTime;
    private SwitchMaterial swRecurring;
    private Button btnSave, btnCancel;
    private ProgressBar loader;
    private Calendar calendar = Calendar.getInstance();
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        etDueTime = findViewById(R.id.etDueTime);
        swRecurring = findViewById(R.id.swRecurring);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        loader = findViewById(R.id.progressLoader);

        // בחירת תאריך
        etDueDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDueDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // בחירת שעה
        etDueTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                etDueTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        btnSave.setOnClickListener(v -> saveMaintenanceToFirestore());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveMaintenanceToFirestore() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String date = etDueDate.getText().toString().trim();
        String time = etDueTime.getText().toString().trim();
        boolean isRecurring = swRecurring.isChecked();

        // 1. בדיקה שכל השדות מלאים
        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "נא למלא כותרת, תאריך ושעה", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. בדיקת הרשאות התראה (Android 12+) - אם אין הרשאה, הכפתור עלול "להיתקע"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "יש לאשר הרשאת התראות מדויקות בהגדרות", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        loader.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String fullDateTime = date + " " + time;
        Maintenance m = new Maintenance(title, desc, fullDateTime, isRecurring);

        db.collection("users").document(userId)
                .update("maintenances", FieldValue.arrayUnion(m))
                .addOnSuccessListener(aVoid -> {
                    // קביעת ההתראה רק אחרי שהשמירה הצליחה
                    setAlarm(title, desc, fullDateTime);
                    Toast.makeText(this, "הטיפול נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loader.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setAlarm(String title, String description, String dateTimeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateTimeStr);
            if (date == null || date.getTime() <= System.currentTimeMillis()) return;

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", "תזכורת רכב: " + title);
            intent.putExtra("message", description);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) date.getTime(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}