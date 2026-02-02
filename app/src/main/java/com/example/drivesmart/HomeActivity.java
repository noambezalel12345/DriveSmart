package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MaintenanceAdapter adapter;
    private List<Maintenance> maintenanceList;
    private TextView tvNoMaintenance;
    private Button btnDeleteSelected;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        recyclerView = findViewById(R.id.recyclerView);
        tvNoMaintenance = findViewById(R.id.tvNoMaintenance);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        ImageButton btnBack = findViewById(R.id.btnBack);

        maintenanceList = new ArrayList<>();
        adapter = new MaintenanceAdapter(maintenanceList, item -> {
            Intent intent = new Intent(HomeActivity.this, EditMaintenanceActivity.class);
            intent.putExtra("TITLE", item.title);
            intent.putExtra("DESCRIPTION", item.description);
            intent.putExtra("DUE_DATE", item.dueDate);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddMaintenance.class)));
        btnBack.setOnClickListener(v -> finish());
        btnDeleteSelected.setOnClickListener(v -> deleteSelectedMaintenances());

        loadMaintenanceFromFirestore();
    }

    private void loadMaintenanceFromFirestore() {
        if (userId == null) return;

        db.collection("users").document(userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("FirestoreError", "Listen failed: " + e.getMessage());
                        return;
                    }

                    maintenanceList.clear();

                    if (snapshot != null && snapshot.exists()) {
                        List<Map<String, Object>> rawList = (List<Map<String, Object>>) snapshot.get("maintenances");

                        if (rawList != null) {
                            for (Map<String, Object> data : rawList) {
                                Maintenance m = new Maintenance(
                                        (String) data.get("title"),
                                        (String) data.get("description"),
                                        (String) data.get("dueDate")
                                );
                                maintenanceList.add(m);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateUI(); // מעדכן את הנראות של כל הרכיבים
                });
    }

    private void updateUI() {
        boolean isEmpty = maintenanceList.isEmpty();

        // 1. הודעת "אין טיפולים"
        tvNoMaintenance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        // 2. כפתור המחיקה האדום - מופיע רק כשיש פריטים
        btnDeleteSelected.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        // 3. הסתרת האיקס (imgEmpty) מה-XML
        if (findViewById(R.id.imgEmpty) != null) {
            findViewById(R.id.imgEmpty).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        // 4. הסתרת הטקסט המשני "Add car maintenance"
        if (findViewById(R.id.tvAddMaintenance) != null) {
            findViewById(R.id.tvAddMaintenance).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void deleteSelectedMaintenances() {
        List<Maintenance> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(this, "נא לסמן טיפולים למחיקה", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("users").document(userId);

        for (Maintenance m : selected) {
            userRef.update("maintenances", FieldValue.arrayRemove(m));
        }

        Toast.makeText(this, "הפריטים נמחקו", Toast.LENGTH_SHORT).show();
        // הערה: ה-SnapshotListener כבר יעדכן את הרשימה אוטומטית, אין צורך ב-clear ידני כאן.
    }
}