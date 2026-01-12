package com.example.drivesmart;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddMaintenance extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;
    private ProgressBar loader;

    private Calendar dueDateCalendar;
    private AddMaintenanceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(AddMaintenanceViewModel.class);

        // XML
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        loader = findViewById(R.id.progressLoader);

        dueDateCalendar = Calendar.getInstance();

        // Observers
        viewModel.getIsSaveEnabled().observe(this,
                enabled -> btnSave.setEnabled(Boolean.TRUE.equals(enabled)));

        viewModel.getIsLoading().observe(this,
                loading -> loader.setVisibility(loading ? ProgressBar.VISIBLE : ProgressBar.GONE));

        viewModel.getIsSaved().observe(this, saved -> {
            if (Boolean.TRUE.equals(saved)) {
                Toast.makeText(this, "הטיפול נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Input listeners
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onTitleChanged(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etDueDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> viewModel.saveMaintenance());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();

        DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
            dueDateCalendar.set(y, m, d);
            etDueDate.setText(
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(dueDateCalendar.getTime())
            );
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        picker.getDatePicker().setMinDate(now.getTimeInMillis());
        picker.show();
    }
}
