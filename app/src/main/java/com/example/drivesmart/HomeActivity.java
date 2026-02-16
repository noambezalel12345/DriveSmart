package com.example.drivesmart;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drivesmart.model.Maintenance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        // בקשת הרשאות להתראות באנדרואיד 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

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
            new AlertDialog.Builder(this)
                    .setTitle("מחיקת טיפולים")
                    .setMessage("האם למחוק את הטיפולים שנבחרו?")
                    .setPositiveButton("מחק", (d, w) -> handleDeletion(false))
                    .setNegativeButton("ביטול", null).show();
            return;
        }

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
                                String nextDate = calculateNextYear(sel.dueDate);
                                Map<String, Object> nextM = new HashMap<>();
                                nextM.put("title", sel.title);
                                nextM.put("description", sel.description);
                                nextM.put("dueDate", nextDate);
                                nextM.put("isRecurring", true);
                                toAdd.add(nextM);

                                // קביעת התראה לשנה הבאה
                                setAlarm(sel.title, sel.description, nextDate);
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

    private void setAlarm(String title, String description, String dateTimeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateTimeStr);
            if (date == null || date.getTime() <= System.currentTimeMillis()) return;

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", "תזכורת לטיפול: " + title);
            intent.putExtra("message", description);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) date.getTime(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
        } catch (Exception e) { e.printStackTrace(); }
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