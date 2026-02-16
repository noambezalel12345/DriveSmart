package com.example.drivesmart;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial; // הוספה
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditMaintenanceActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate, etDueTime;
    private SwitchMaterial swRecurring; // הוספה
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
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        etTitle = findViewById(R.id.etEditTitle);
        etDescription = findViewById(R.id.etEditDescription);
        etDueDate = findViewById(R.id.etEditDueDate);
        etDueTime = findViewById(R.id.etEditDueTime);
        swRecurring = findViewById(R.id.swEditRecurring); // קישור ל-Switch
        btnSave = findViewById(R.id.btnSaveEdit);
        btnDelete = findViewById(R.id.btnDeleteEdit);
        loader = findViewById(R.id.editLoader);

        // שליפת נתונים מה-Intent
        String fullDateTime = getIntent().getStringExtra("DUE_DATE");
        String title = getIntent().getStringExtra("TITLE");
        String description = getIntent().getStringExtra("DESCRIPTION");
        boolean isRecurring = getIntent().getBooleanExtra("IS_RECURRING", false);

        originalMaintenance = new Maintenance(title, description, fullDateTime, isRecurring);

        // הגדרת מצב ה-Switch לפי הנתון הקיים
        swRecurring.setChecked(isRecurring);

        // הפרדת תאריך ושעה לתצוגה בשדות
        if (fullDateTime != null && fullDateTime.contains(" ")) {
            String[] parts = fullDateTime.split(" ");
            etDueDate.setText(parts[0]);
            etDueTime.setText(parts[1]);
        } else {
            etDueDate.setText(fullDateTime);
        }

        etTitle.setText(title);
        etDescription.setText(description);

        // בחירת תאריך
        etDueDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                etDueDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // בחירת שעה
        etDueTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                etDueTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        btnSave.setOnClickListener(v -> updateMaintenance());
        btnDelete.setOnClickListener(v -> deleteMaintenance());
    }

    private void updateMaintenance() {
        String title = etTitle.getText().toString().trim();
        String date = etDueDate.getText().toString().trim();
        String time = etDueTime.getText().toString().trim();
        boolean isRecurringNow = swRecurring.isChecked(); // קבלת המצב החדש

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        loader.setVisibility(View.VISIBLE);
        DocumentReference userRef = db.collection("users").document(userId);

        String updatedFullDateTime = date + " " + time;

        Maintenance updated = new Maintenance(
                title,
                etDescription.getText().toString().trim(),
                updatedFullDateTime,
                isRecurringNow // שמירת המצב החדש (מופעל/מבוטל)
        );

        // עדכון ב-Firestore (הסרה והוספה)
        userRef.update("maintenances", FieldValue.arrayRemove(originalMaintenance))
                .addOnSuccessListener(aVoid -> {
                    userRef.update("maintenances", FieldValue.arrayUnion(updated))
                            .addOnSuccessListener(aVoid2 -> {
                                loader.setVisibility(View.GONE);
                                Toast.makeText(this, "הטיפול עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                loader.setVisibility(View.GONE);
                                Toast.makeText(this, "שגיאה בשמירת הטיפול המעודכן", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(this, "שגיאה בהסרת הטיפול הישן", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteMaintenance() {
        loader.setVisibility(View.VISIBLE);
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update("maintenances", FieldValue.arrayRemove(originalMaintenance))
                .addOnCompleteListener(task -> {
                    loader.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "הטיפול נמחק", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}