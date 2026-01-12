package com.example.drivesmart;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    private ImageButton btnBack;
    private ProgressBar loader;
    private Calendar dueDateCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        loader = findViewById(R.id.progressLoader);

        dueDateCalendar = Calendar.getInstance();

        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSave.setEnabled(s.length() >= 2 && s.length() <= 40);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etDueDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveMaintenanceToFirebase());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
            dueDateCalendar.set(y, m, d);
            etDueDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(dueDateCalendar.getTime()));
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        picker.getDatePicker().setMinDate(now.getTimeInMillis());
        picker.show();
    }

    private void saveMaintenanceToFirebase() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Enter title");
            return;
        }

        btnSave.setEnabled(false);
        loader.setVisibility(ProgressBar.VISIBLE);

        Maintenance maintenance = new Maintenance(title, description, dueDate);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("maintenances");
        database.push().setValue(maintenance)
                .addOnCompleteListener(task -> {
                    loader.setVisibility(ProgressBar.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "הטיפול נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                    }
                });
    }
}
