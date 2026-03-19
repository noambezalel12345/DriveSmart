package com.example.drivesmart;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
    private ImageButton btnBack;
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
        btnBack = findViewById(R.id.btnBack);

        // בחירת תאריך - חסימת עבר
        etDueDate.setOnClickListener(v -> {
            DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, d) -> {
                calendar.set(Calendar.YEAR, y);
                calendar.set(Calendar.MONTH, m);
                calendar.set(Calendar.DAY_OF_MONTH, d);
                etDueDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
                etDueTime.setText(""); // איפוס שעה כשמחליפים תאריך
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            dp.getDatePicker().setMinDate(System.currentTimeMillis()); // חסימת תאריכים שעברו
            dp.show();
        });
        // בחירת שעה - חסימת שעה שעברה היום
        etDueTime.setOnClickListener(v -> {
            if (etDueDate.getText().toString().isEmpty()) {
                Toast.makeText(this, "נא לבחור תאריך תחילה", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            TimePickerDialog tp = new TimePickerDialog(this, (view, h, min) -> {
                Calendar selectedTime = (Calendar) calendar.clone();
                selectedTime.set(Calendar.HOUR_OF_DAY, h);
                selectedTime.set(Calendar.MINUTE, min);
                selectedTime.set(Calendar.SECOND, 0);
                selectedTime.set(Calendar.MILLISECOND, 0);

                // בדיקה אם הזמן הנבחר עבר (רק אם התאריך הוא היום)
                if (selectedTime.getTimeInMillis() <= System.currentTimeMillis()) {
                    Toast.makeText(this, "לא ניתן לבחור זמן שעבר", Toast.LENGTH_SHORT).show();
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, h);
                    calendar.set(Calendar.MINUTE, min);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    etDueTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, min));
                }
            }, hour, minute, true);
            tp.show();
        });

        // כפתורי שמירה וביטול
        btnSave.setOnClickListener(v -> saveToFirestore());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveToFirestore() {
        String title = etTitle.getText().toString().trim();
        String date = etDueDate.getText().toString().trim();
        String time = etDueTime.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // וידוא אחרון שהזמן לא עבר
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "הזמן שנבחר עבר, נא לעדכן שעה", Toast.LENGTH_SHORT).show();
            return;
        }

        Maintenance m = new Maintenance(title, etDescription.getText().toString().trim(), date + " " + time, swRecurring.isChecked());
        Map<String, Object> data = new HashMap<>();
        data.put("maintenances", FieldValue.arrayUnion(m));

        db.collection("users").document(userId).set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    setAlarm(title, calendar.getTimeInMillis());
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(String title, long triggerTime) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", "DriveSmart Reminder");
        intent.putExtra("message", "הגיע הזמן לטיפול: " + title);
        intent.putExtra("triggerTime", triggerTime);

        // יצירת RequestCode ייחודי (למשל לפי השניות של התאריך)
        int requestCode = (int) (triggerTime % 100000);

        // השתמשנו ב-FLAG_CANCEL_CURRENT כדי שאם יש התראה ישנה "תקועה" במערכת, היא תתבטל מיד
        PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
        }
    }
}