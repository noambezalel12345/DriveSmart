package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MaintenanceAdapter adapter;
    private List<Maintenance> maintenanceList;
    private Button btnDeleteSelected;
    private TextView tvNoMaintenance;
    private View imgEmpty, tvAddMaintenanceHint;
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
        imgEmpty = findViewById(R.id.imgEmpty);
        tvAddMaintenanceHint = findViewById(R.id.tvAddMaintenance);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        btnDeleteSelected.setVisibility(View.GONE);
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
                    if (count > 0) {
                        btnDeleteSelected.setVisibility(View.VISIBLE);
                        btnDeleteSelected.setText("מחק טיפולים שסומנו (" + count + ")");
                    } else {
                        btnDeleteSelected.setVisibility(View.GONE);
                    }
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddMaintenance.class)));

        // לחיצה על הכפתור האדום פותחת דיאלוג בחירה
        btnDeleteSelected.setOnClickListener(v -> showDeleteOptionsDialog());

        loadMaintenanceFromFirestore();
    }

    private void showDeleteOptionsDialog() {
        List<Maintenance> selected = adapter.getSelectedItems();
        boolean hasRecurring = false;
        for (Maintenance m : selected) {
            if (m.isRecurring) {
                hasRecurring = true;
                break;
            }
        }

        if (!hasRecurring) {
            // אם אין מחזוריים, פשוט מוחקים
            new AlertDialog.Builder(this)
                    .setTitle("מחיקת טיפולים")
                    .setMessage("האם למחוק את הטיפולים שנבחרו?")
                    .setPositiveButton("מחק", (d, w) -> handleDeletion(false))
                    .setNegativeButton("ביטול", null).show();
            return;
        }

        // דיאלוג עבור טיפולים מחזוריים
        new AlertDialog.Builder(this)
                .setTitle("סיום טיפול")
                .setMessage("חלק מהטיפולים הם מחזוריים. האם תרצה לחדש אותם לשנה הבאה או למחוק לגמרי?")
                .setPositiveButton("חדש לשנה הבאה", (d, w) -> handleDeletion(true))
                .setNegativeButton("מחק לגמרי", (d, w) -> handleDeletion(false))
                .setNeutralButton("ביטול", null).show();
    }

    private void handleDeletion(boolean shouldRenew) {
        final List<Maintenance> selected = new ArrayList<>(adapter.getSelectedItems());
        final DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> allRemotes = (List<Map<String, Object>>) documentSnapshot.get("maintenances");
                if (allRemotes == null) return;

                List<Map<String, Object>> toRemove = new ArrayList<>();
                List<Map<String, Object>> toAdd = new ArrayList<>();

                for (Maintenance sel : selected) {
                    for (Map<String, Object> rem : allRemotes) {
                        if (sel.title.equals(rem.get("title")) && sel.dueDate.equals(rem.get("dueDate"))) {
                            toRemove.add(rem);
                            if (shouldRenew && sel.isRecurring) {
                                Map<String, Object> nextM = new HashMap<>();
                                nextM.put("title", sel.title);
                                nextM.put("description", sel.description);
                                nextM.put("dueDate", calculateNextYear(sel.dueDate));
                                nextM.put("isRecurring", true);
                                toAdd.add(nextM);
                            }
                            break;
                        }
                    }
                }

                userRef.update("maintenances", FieldValue.arrayRemove(toRemove.toArray()))
                        .addOnSuccessListener(aVoid -> {
                            if (!toAdd.isEmpty()) {
                                userRef.update("maintenances", FieldValue.arrayUnion(toAdd.toArray()));
                            }
                            adapter.getSelectedItems().clear();
                            btnDeleteSelected.setVisibility(View.GONE);
                            Toast.makeText(HomeActivity.this, "הפעולה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void loadMaintenanceFromFirestore() {
        if (userId == null) return;
        db.collection("users").document(userId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                maintenanceList.clear();
                List<Map<String, Object>> rawList = (List<Map<String, Object>>) snapshot.get("maintenances");
                if (rawList != null) {
                    for (Map<String, Object> data : rawList) {
                        maintenanceList.add(new Maintenance(
                                (String) data.get("title"), (String) data.get("description"),
                                (String) data.get("dueDate"), data.get("isRecurring") != null && (boolean) data.get("isRecurring")
                        ));
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyStateUI();
            }
        });
    }

    private void updateEmptyStateUI() {
        boolean isEmpty = maintenanceList.isEmpty();
        tvNoMaintenance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (imgEmpty != null) imgEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (tvAddMaintenanceHint != null) tvAddMaintenanceHint.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private String calculateNextYear(String dateStr) {
        try {
            String pureDate = dateStr.contains(" ") ? dateStr.split(" ")[0] : dateStr;
            String timePart = dateStr.contains(" ") ? " " + dateStr.split(" ")[1] : "";
            String[] parts = pureDate.split("/");
            int year = Integer.parseInt(parts[2]) + 1;
            return String.format(Locale.getDefault(), "%s/%s/%d%s", parts[0], parts[1], year, timePart);
        } catch (Exception e) { return dateStr; }
    }
}