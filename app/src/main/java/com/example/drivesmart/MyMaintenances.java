package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class MyMaintenances extends AppCompatActivity {

    private RecyclerView rvMaintenances;
    private MaintenanceAdapter adapter;
    private List<Maintenance> maintenanceList;
    private DatabaseReference database;
    private Button btnDeleteSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_maintenances);

        rvMaintenances = findViewById(R.id.rvMaintenances);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected); // כפתור מחיקה חדש
        ImageButton btnBack = findViewById(R.id.btnBack);
        FloatingActionButton btnAdd = findViewById(R.id.btnAddMaintenance);

        rvMaintenances.setLayoutManager(new LinearLayoutManager(this));
        maintenanceList = new ArrayList<>();

        adapter = new MaintenanceAdapter(maintenanceList, item -> {
            Intent intent = new Intent(this, EditMaintenanceActivity.class);
            intent.putExtra("MAINTENANCE_ID", item.id);
            intent.putExtra("TITLE", item.title);
            intent.putExtra("DESCRIPTION", item.description);
            intent.putExtra("DUE_DATE", item.dueDate);
            startActivity(intent);
        });

        rvMaintenances.setAdapter(adapter);
        database = FirebaseDatabase.getInstance("https://drivesmart-dd12a-default-rtdb.firebaseio.com/").getReference("maintenances");

        fetchMaintenances();

        // לחיצה על כפתור מחיקת המסומנים
        btnDeleteSelected.setOnClickListener(v -> deleteSelectedItems());

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddMaintenance.class)));
    }

    private void fetchMaintenances() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                maintenanceList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Maintenance m = ds.getValue(Maintenance.class);
                    if (m != null) {
                        m.id = ds.getKey();
                        maintenanceList.add(m);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void deleteSelectedItems() {
        List<Maintenance> toDelete = adapter.getSelectedItems();
        if (toDelete.isEmpty()) {
            Toast.makeText(this, "לא נבחרו טיפולים למחיקה", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Maintenance m : toDelete) {
            if (m.id != null) {
                database.child(m.id).removeValue(); // מחיקה מה-Firebase
            }
        }
        Toast.makeText(this, "הטיפולים שנבחרו נמחקו", Toast.LENGTH_SHORT).show();
        toDelete.clear(); // ניקוי הרשימה לאחר המחיקה
    }
}