package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MaintenanceAdapter adapter;
    private List<Maintenance> maintenanceList;
    private TextView tvNoMaintenance;
    private Button btnDeleteSelected;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // אתחול רכיבים
        recyclerView = findViewById(R.id.recyclerView);
        tvNoMaintenance = findViewById(R.id.tvNoMaintenance);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        ImageButton btnBack = findViewById(R.id.btnBack);

        maintenanceList = new ArrayList<>();

        // הגדרת האדפטר עם מאזין למעבר לדף עריכה
        adapter = new MaintenanceAdapter(maintenanceList, item -> {
            Intent intent = new Intent(HomeActivity.this, EditMaintenanceActivity.class);
            intent.putExtra("MAINTENANCE_ID", item.id);
            intent.putExtra("TITLE", item.title);
            intent.putExtra("DESCRIPTION", item.description);
            intent.putExtra("DUE_DATE", item.dueDate);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // חיבור ל-Firebase
        database = FirebaseDatabase.getInstance("https://drivesmart-dd12a-default-rtdb.firebaseio.com/").getReference("maintenances");

        // לחיצה על הוספת טיפול
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddMaintenance.class)));

        // לחיצה על חזרה
        btnBack.setOnClickListener(v -> finish());

        // לוגיקת מחיקה של פריטים מסומנים
        btnDeleteSelected.setOnClickListener(v -> deleteSelectedMaintenances());

        loadMaintenanceFromFirebase();
    }

    private void loadMaintenanceFromFirebase() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                maintenanceList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Maintenance m = data.getValue(Maintenance.class);
                    if (m != null) {
                        m.id = data.getKey();
                        maintenanceList.add(m);
                    }
                }
                adapter.notifyDataSetChanged();

                // עדכון תצוגת "אין טיפולים"
                boolean isEmpty = maintenanceList.isEmpty();
                tvNoMaintenance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                findViewById(R.id.imgEmpty).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                findViewById(R.id.tvAddMaintenance).setVisibility(isEmpty ? View.VISIBLE : View.GONE);

                // הצגת כפתור מחיקה רק אם יש נתונים
                btnDeleteSelected.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeActivity", "Firebase Error: " + error.getMessage());
            }
        });
    }

    private void deleteSelectedMaintenances() {
        List<Maintenance> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(this, "נא לסמן טיפולים למחיקה", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Maintenance m : selected) {
            database.child(m.id).removeValue();
        }

        selected.clear();
        Toast.makeText(this, "הטיפולים שנבחרו נמחקו בהצלחה", Toast.LENGTH_SHORT).show();
    }
}