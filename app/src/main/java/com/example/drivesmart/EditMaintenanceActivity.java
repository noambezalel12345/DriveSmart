package com.example.drivesmart;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditMaintenanceActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate;
    private Button btnSave, btnCancel, btnDelete;
    private ProgressBar loader;
    private String maintenanceId;
    private DatabaseReference dbRef;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_maintenance);

        etTitle = findViewById(R.id.etEditTitle);
        etDescription = findViewById(R.id.etEditDescription);
        etDueDate = findViewById(R.id.etEditDueDate);
        btnSave = findViewById(R.id.btnSaveEdit);
        btnCancel = findViewById(R.id.btnCancelEdit);
        btnDelete = findViewById(R.id.btnDeleteEdit);
        loader = findViewById(R.id.editLoader);

        maintenanceId = getIntent().getStringExtra("MAINTENANCE_ID");

        // הגנה: אם אין ID, נחזור אחורה כדי למנוע קריסה ב-Firebase
        if (maintenanceId == null) {
            Toast.makeText(this, "Error: Maintenance ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle.setText(getIntent().getStringExtra("TITLE"));
        etDescription.setText(getIntent().getStringExtra("DESCRIPTION"));
        etDueDate.setText(getIntent().getStringExtra("DUE_DATE"));

        dbRef = FirebaseDatabase.getInstance().getReference("maintenances").child(maintenanceId);

        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateForm(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        etDueDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> showExitDialog());
        btnDelete.setOnClickListener(v -> showDeleteDialog());

        validateForm();
    }

    private void validateForm() {
        String title = etTitle.getText().toString().trim();
        btnSave.setEnabled(title.length() >= 2 && title.length() <= 40);
    }

    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            etDueDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }

    private void saveChanges() {
        loader.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Maintenance updated = new Maintenance(
                etTitle.getText().toString().trim(),
                etDescription.getText().toString().trim(),
                etDueDate.getText().toString().trim()
        );

        dbRef.setValue(updated).addOnCompleteListener(task -> {
            loader.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "הטיפול נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
            }
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage("השינויים לא נשמרו")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setMessage("האם את/ה בטוח?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbRef.removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "הטיפול נמחק", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }).show();
    }

    @Override
    public void onBackPressed() { showExitDialog(); }
}