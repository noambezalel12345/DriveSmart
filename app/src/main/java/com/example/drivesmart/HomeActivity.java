package com.example.drivesmart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MaintenanceAdapter adapter;
    private List<Maintenance> maintenanceList;
    private TextView tvNoMaintenance;
    private MaintenanceAdapter.OnItemClickListener adapterListener = new MaintenanceAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(Maintenance m) {
            Log.d("HomeActivity", "item clicked" + m.title);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        tvNoMaintenance = findViewById(R.id.tvNoMaintenance);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        maintenanceList = new ArrayList<>();
        adapter = new MaintenanceAdapter(maintenanceList, adapterListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddMaintenance.class)));

        loadMaintenanceFromFirebase();
    }



    private void loadMaintenanceFromFirebase() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("maintenances");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                maintenanceList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Maintenance m = data.getValue(Maintenance.class);
                    maintenanceList.add(m);
                }
                adapter.notifyDataSetChanged();
                tvNoMaintenance.setVisibility(maintenanceList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
