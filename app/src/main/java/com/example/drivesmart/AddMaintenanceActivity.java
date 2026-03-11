package com.example.drivesmart;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.drivesmart.model.Maintenance;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMaintenanceActivity extends AppCompatActivity {

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

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "שגיאה: משתמש לא מחובר!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        etDueTime = findViewById(R.id.etDueTime);
        swRecurring = findViewById(R.id.swRecurring);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        loader = findViewById(R.id.progressLoader);

        etDueDate.setOnClickListener(v -> {
            DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDueDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            // הגבלת בחירת תאריכים רק מהיום והלאה
            dp.getDatePicker().setMinDate(System.currentTimeMillis());
            dp.show();
        });

        etDueTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                etDueTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
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
        String time = etDueTime.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "נא למלא שדות חובה", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                return;
            }
        }

        loader.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Maintenance m = new Maintenance(title, desc, date + " " + time, swRecurring.isChecked());

        Map<String, Object> data = new HashMap<>();
        data.put("maintenances", FieldValue.arrayUnion(m));

        db.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    scheduleNotifications(title, desc);
                    Toast.makeText(this, "נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loader.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void scheduleNotifications(String title, String desc) {
        long time = calendar.getTimeInMillis();
        setAlarm(title, "טיפול רכב: " + desc, time);
    }

    private void setAlarm(String title, String msg, long time) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", msg);
        PendingIntent pi = PendingIntent.getBroadcast(this, (int)(time/1000), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
    }
}