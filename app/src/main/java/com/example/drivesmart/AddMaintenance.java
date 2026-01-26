package com.example.drivesmart;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddMaintenance extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate;
    private Button btnSave, btnCancel;
    private ProgressBar loader;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        // אתחול רכיבים לפי ה-XML שלך
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        loader = findViewById(R.id.progressLoader);

        // מאזין לשינוי טקסט - כדי להפעיל את הכפתור (שב-XML מוגדר כ-false)
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // הכפתור יהיה פעיל רק אם יש לפחות 2 תווים
                btnSave.setEnabled(s.toString().trim().length() >= 2);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // בחירת תאריך עם חסימת עבר
        etDueDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                etDueDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            picker.getDatePicker().setMinDate(System.currentTimeMillis()); // חסימת עבר
            picker.show();
        });

        // כפתור שמירה
        btnSave.setOnClickListener(v -> saveMaintenance());

        // כפתור ביטול
        btnCancel.setOnClickListener(v -> finish());

        // כפתור חזרה (ImageButton שבפינה)
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void saveMaintenance() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String date = etDueDate.getText().toString().trim();

        loader.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // חיבור ל-Firebase שלך
        DatabaseReference db = FirebaseDatabase.getInstance("https://drivesmart-dd12a-default-rtdb.firebaseio.com/").getReference("maintenances");

        Maintenance m = new Maintenance(title, desc, date);

        db.push().setValue(m).addOnCompleteListener(task -> {
            loader.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                // מעבר לדף הטיפולים שלי
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                btnSave.setEnabled(true);
                Toast.makeText(this, "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}