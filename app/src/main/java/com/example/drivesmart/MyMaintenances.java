package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_maintenances);

        rvMaintenances = findViewById(R.id.rvMaintenances);
        ImageButton btnBack = findViewById(R.id.btnBack);
        FloatingActionButton btnAdd = findViewById(R.id.btnAddMaintenance);

        rvMaintenances.setLayoutManager(new LinearLayoutManager(this));
        maintenanceList = new ArrayList<>();

        adapter = new MaintenanceAdapter(maintenanceList, item -> {
            Intent intent = new Intent(MyMaintenances.this, EditMaintenanceActivity.class);
            intent.putExtra("MAINTENANCE_ID", item.id);
            intent.putExtra("TITLE", item.title);
            intent.putExtra("DESCRIPTION", item.description);
            intent.putExtra("DUE_DATE", item.dueDate);
            startActivity(intent);
        });

        rvMaintenances.setAdapter(adapter);

        // חיבור ישיר לכתובת ה-Database שלך למניעת טעינה אינסופית
        database = FirebaseDatabase.getInstance("https://drivesmart-dd12a-default-rtdb.firebaseio.com/").getReference("maintenances");
        fetchMaintenances();

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddMaintenance.class)));
    }

    private void fetchMaintenances() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}