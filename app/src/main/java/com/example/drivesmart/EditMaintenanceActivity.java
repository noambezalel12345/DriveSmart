package com.example.drivesmart;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditMaintenanceActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate;
    private Button btnSave, btnDelete;
    private ProgressBar loader;
    private String maintenanceId;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_maintenance);

        // אתחול רכיבים - וודא שה-IDs תואמים ל-XML שלך
        etTitle = findViewById(R.id.etEditTitle);
        etDescription = findViewById(R.id.etEditDescription);
        etDueDate = findViewById(R.id.etEditDueDate);
        btnSave = findViewById(R.id.btnSaveEdit);
        btnDelete = findViewById(R.id.btnDeleteEdit);
        loader = findViewById(R.id.editLoader);

        // --- זהו שלב 3: קליטת הנתונים שנשלחו מהדף הקודם ---
        maintenanceId = getIntent().getStringExtra("MAINTENANCE_ID");
        String title = getIntent().getStringExtra("TITLE");
        String description = getIntent().getStringExtra("DESCRIPTION");
        String dueDate = getIntent().getStringExtra("DUE_DATE");

        if (maintenanceId != null) {
            etTitle.setText(title);
            etDescription.setText(description);
            etDueDate.setText(dueDate);

            // חיבור ל-Firebase לטיפול הספציפי
            dbRef = FirebaseDatabase.getInstance("https://drivesmart-dd12a-default-rtdb.firebaseio.com/")
                    .getReference("maintenances")
                    .child(maintenanceId);
        } else {
            Toast.makeText(this, "שגיאה: לא נמצא מזהה טיפול", Toast.LENGTH_SHORT).show();
            finish();
        }

        // הגדרת כפתור שמירה
        btnSave.setOnClickListener(v -> saveChanges());

        // הגדרת כפתור מחיקה (ביטול טיפול)
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת טיפול")
                .setMessage("האם אתה בטוח שברצונך למחוק את הטיפול הזה?")
                .setPositiveButton("מחק", (dialog, which) -> {
                    loader.setVisibility(View.VISIBLE);
                    dbRef.removeValue().addOnCompleteListener(task -> {
                        loader.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "הטיפול נמחק", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void saveChanges() {
        String newTitle = etTitle.getText().toString().trim();
        if (newTitle.isEmpty()) return;

        loader.setVisibility(View.VISIBLE);
        Maintenance updated = new Maintenance(newTitle,
                etDescription.getText().toString().trim(),
                etDueDate.getText().toString().trim());

        dbRef.setValue(updated).addOnCompleteListener(task -> {
            loader.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "השינויים נשמרו", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}