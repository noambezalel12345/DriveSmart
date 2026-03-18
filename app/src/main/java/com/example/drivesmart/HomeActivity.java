package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.drivesmart.model.Maintenance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MaintenanceAdapter adapter;
    private List<Maintenance> maintenanceList;
    private Button btnDeleteSelected;
    private TextView tvNoMaintenance, tvAddMaintenanceHint;
    private ImageView imgEmpty;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        recyclerView = findViewById(R.id.recyclerView);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        tvNoMaintenance = findViewById(R.id.tvNoMaintenance);
        tvAddMaintenanceHint = findViewById(R.id.tvAddMaintenance);
        imgEmpty = findViewById(R.id.imgEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        maintenanceList = new ArrayList<>();
        adapter = new MaintenanceAdapter(maintenanceList,
                item -> {
                    Intent intent = new Intent(HomeActivity.this, EditMaintenanceActivity.class);
                    intent.putExtra("TITLE", item.title);
                    intent.putExtra("DESCRIPTION", item.description);
                    intent.putExtra("DUE_DATE", item.dueDate);
                    intent.putExtra("IS_RECURRING", item.isRecurring);
                    startActivity(intent);
                },
                count -> {
                    btnDeleteSelected.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                    btnDeleteSelected.setText("מחק פריטים שסומנו (" + count + ")");
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddMaintenanceActivity.class)));
        btnDeleteSelected.setOnClickListener(v -> askDeleteMode());

        loadMaintenanceFromFirestore();
    }

    private void loadMaintenanceFromFirestore() {
        if (userId == null) return;
        db.collection("users").document(userId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                maintenanceList.clear();
                List<Map<String, Object>> rawList = (List<Map<String, Object>>) snapshot.get("maintenances");
                if (rawList != null && !rawList.isEmpty()) {
                    for (Map<String, Object> data : rawList) {
                        maintenanceList.add(new Maintenance(
                                (String) data.get("title"),
                                (String) data.get("description"),
                                (String) data.get("dueDate"),
                                data.get("isRecurring") != null && (boolean) data.get("isRecurring")
                        ));
                    }
                    tvNoMaintenance.setVisibility(View.GONE);
                    imgEmpty.setVisibility(View.GONE);
                    tvAddMaintenanceHint.setVisibility(View.GONE);
                } else {
                    tvNoMaintenance.setVisibility(View.VISIBLE);
                    imgEmpty.setVisibility(View.VISIBLE);
                    tvAddMaintenanceHint.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void askDeleteMode() {
        List<Maintenance> selected = adapter.getSelectedItems();
        boolean hasRecurring = false;
        for (Maintenance m : selected) {
            if (m.isRecurring) {
                hasRecurring = true;
                break;
            }
        }

        if (hasRecurring) {
            new AlertDialog.Builder(this)
                    .setTitle("מחיקת פריטים")
                    .setMessage("חלק מהפריטים הם מחזוריים. האם למחוק אותם לגמרי או לחדש לשנה הבאה?")
                    .setPositiveButton("חדש מחזוריים", (dialog, which) -> processSelection(true))
                    .setNegativeButton("מחק הכל", (dialog, which) -> processSelection(false))
                    .setNeutralButton("ביטול", null).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("מחיקה")
                    .setMessage("האם למחוק את הפריטים שנבחרו?")
                    .setPositiveButton("מחק", (dialog, which) -> processSelection(false))
                    .setNegativeButton("ביטול", null).show();
        }
    }

    private void processSelection(boolean renewRecurring) {
        List<Maintenance> selected = adapter.getSelectedItems();
        List<Maintenance> toRemove = new ArrayList<>();
        List<Maintenance> toAdd = new ArrayList<>();

        for (Maintenance m : selected) {
            toRemove.add(m);
            if (renewRecurring && m.isRecurring) {
                toAdd.add(new Maintenance(m.title, m.description, calculateNextYear(m.dueDate), true));
            }
        }

        db.collection("users").document(userId).update("maintenances", FieldValue.arrayRemove(toRemove.toArray()))
                .addOnSuccessListener(aVoid -> {
                    if (!toAdd.isEmpty()) {
                        db.collection("users").document(userId).update("maintenances", FieldValue.arrayUnion(toAdd.toArray()));
                    }
                    adapter.clearSelections();
                    Toast.makeText(this, "הפעולה הושלמה", Toast.LENGTH_SHORT).show();
                });
    }

    private String calculateNextYear(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateStr));
            cal.add(Calendar.YEAR, 1);
            return sdf.format(cal.getTime());
        } catch (Exception e) { return dateStr; }
    }
}