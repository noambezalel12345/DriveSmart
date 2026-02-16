package com.example.drivesmart;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog; // הוספה
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddMaintenance extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate, etDueTime; // נוסף etDueTime
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
        etDueTime = findViewById(R.id.etDueTime); // קישור לשדה החדש
        swRecurring = findViewById(R.id.swRecurring);
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

        // בחירת תאריך
        etDueDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDueDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            picker.getDatePicker().setMinDate(System.currentTimeMillis());
            picker.show();
        });

        // בחירת שעה
        etDueTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                etDueTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePicker.show();
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
        String time = etDueTime.getText().toString().trim(); // השעה שנבחרה
        boolean isRecurring = swRecurring.isChecked();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "נא למלא כותרת, תאריך ושעה להתראה", Toast.LENGTH_SHORT).show();
            return;
        }

        loader.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // שינוי: הוספת השעה למחרוזת התאריך או לאובייקט Maintenance (וודא שה-Constructor של Maintenance תואם)
        // אם ה-Constructor שלך מקבל רק כותרת, תיאור, תאריך ובווליאן, אפשר לשרשר את השעה לתאריך:
        String fullDateTime = date + " " + time;

        Maintenance m = new Maintenance(title, desc, fullDateTime, isRecurring);

        db.collection("users").document(userId)
                .update("maintenances", FieldValue.arrayUnion(m))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "הטיפול נשמר", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        loader.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}