package com.example.drivesmart;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditMaintenanceActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate;
    private Button btnSave, btnDelete;
    private ProgressBar loader;

    private FirebaseFirestore db;
    private String userId;
    private Maintenance originalMaintenance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_maintenance);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        etTitle = findViewById(R.id.etEditTitle);
        etDescription = findViewById(R.id.etEditDescription);
        etDueDate = findViewById(R.id.etEditDueDate);
        btnSave = findViewById(R.id.btnSaveEdit);
        btnDelete = findViewById(R.id.btnDeleteEdit);
        loader = findViewById(R.id.editLoader);

        // שמירת האובייקט המקורי כדי שנוכל למצוא אותו במערך
        originalMaintenance = new Maintenance(
                getIntent().getStringExtra("TITLE"),
                getIntent().getStringExtra("DESCRIPTION"),
                getIntent().getStringExtra("DUE_DATE")
        );

        etTitle.setText(originalMaintenance.title);
        etDescription.setText(originalMaintenance.description);
        etDueDate.setText(originalMaintenance.dueDate);

        btnSave.setOnClickListener(v -> updateMaintenance());
        btnDelete.setOnClickListener(v -> deleteMaintenance());
    }

    private void deleteMaintenance() {
        loader.setVisibility(View.VISIBLE);
        DocumentReference userRef = db.collection("users").document(userId);

        // הסרת האובייקט הספציפי מהרשימה
        userRef.update("maintenances", FieldValue.arrayRemove(originalMaintenance))
                .addOnCompleteListener(task -> {
                    loader.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        finish();
                    }
                });
    }

    private void updateMaintenance() {
        loader.setVisibility(View.VISIBLE);
        DocumentReference userRef = db.collection("users").document(userId);

        Maintenance updated = new Maintenance(
                etTitle.getText().toString().trim(),
                etDescription.getText().toString().trim(),
                etDueDate.getText().toString().trim()
        );

        // ב-Firestore Array, עדכון דורש הסרת הישן והוספת החדש
        userRef.update("maintenances", FieldValue.arrayRemove(originalMaintenance))
                .addOnSuccessListener(aVoid -> {
                    userRef.update("maintenances", FieldValue.arrayUnion(updated))
                            .addOnSuccessListener(aVoid2 -> {
                                loader.setVisibility(View.GONE);
                                finish();
                            });
                });
    }
}