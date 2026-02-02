package com.example.drivesmart;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMaintenance extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate;
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
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "שגיאה: משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        loader = findViewById(R.id.progressLoader);

        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSave.setEnabled(s.toString().trim().length() >= 2);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etDueDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                etDueDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            picker.getDatePicker().setMinDate(System.currentTimeMillis());
            picker.show();
        });

        btnSave.setOnClickListener(v -> saveMaintenanceToFirestore());
        btnCancel.setOnClickListener(v -> finish());

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }

    private void saveMaintenanceToFirestore() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String date = etDueDate.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "נא למלא כותרת ותאריך", Toast.LENGTH_SHORT).show();
            return;
        }

        loader.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Maintenance m = new Maintenance(title, desc, date);
        Map<String, Object> data = new HashMap<>();
        data.put("maintenances", FieldValue.arrayUnion(m));

        db.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    loader.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // תזמון התראות לפני סגירת הדף
                        scheduleMaintenanceReminders(title);

                        Toast.makeText(this, "הטיפול והתזכורות נשמרו", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        btnSave.setEnabled(true);
                        Log.e("FirestoreError", "Error saving: " + task.getException().getMessage());
                    }
                });
    }

    private void scheduleMaintenanceReminders(String title) {
        long eventTime = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();

        // 1. תזכורת שבוע לפני
        long weekBefore = eventTime - (7L * 24 * 60 * 60 * 1000);
        if (weekBefore > now) {
            setAlarm(title, weekBefore, "יש לך טיפול בעוד שבוע!", 7);
        }

        // 2. תזכורת יום לפני (24 שעות)
        long dayBefore = eventTime - (24 * 60 * 60 * 1000);
        if (dayBefore > now) {
            setAlarm(title, dayBefore, "תזכורת: הטיפול שלך מחר!", 1);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(String title, long time, String message, int idOffset) {
        Intent intent = new Intent(this, com.example.drivesmart.ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        // מזהה ייחודי כדי שהתראות לא ידרסו אחת את השנייה
        int requestId = (title + time).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }
}