package com.example.drivesmart;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
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
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateInputs(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        etDueDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveMaintenance());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void validateInputs() {
        String title = etTitle.getText().toString().trim();
        btnSave.setEnabled(title.length() >= 2 && title.length() <= 40);
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
            dueDateCalendar.set(y, m, d);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etDueDate.setText(sdf.format(dueDateCalendar.getTime()));
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMinDate(now.getTimeInMillis());
        picker.show();
    }

    private void saveMaintenance() {
        loader.setVisibility(ProgressBar.VISIBLE);
        btnSave.setEnabled(false);

        etTitle.postDelayed(() -> {
            loader.setVisibility(ProgressBar.GONE);
            Toast.makeText(this, "הטיפול נשמר בהצלחה", Toast.LENGTH_SHORT).show();
            finish();
        }, 2000);
    }
}
