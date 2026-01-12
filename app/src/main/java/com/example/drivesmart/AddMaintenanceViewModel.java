package com.example.drivesmart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddMaintenanceViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isSaveEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSaved = new MutableLiveData<>(false);

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("maintenances");

    public LiveData<Boolean> getIsSaveEnabled() { return isSaveEnabled; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSaved() { return isSaved; }

    public void onTitleChanged(String title) {
        isSaveEnabled.setValue(title.length() >= 2 && title.length() <= 40);
    }

    public void saveMaintenance(String title, String description, String dueDate) {
        isLoading.setValue(true);
        isSaveEnabled.setValue(false);

        String key = dbRef.push().getKey();
        if (key == null) key = String.valueOf(System.currentTimeMillis());

        Map<String, Object> maintenance = new HashMap<>();
        maintenance.put("title", title);
        maintenance.put("description", description);
        maintenance.put("dueDate", dueDate);
        maintenance.put("timestamp", System.currentTimeMillis());

        dbRef.child(key).setValue(maintenance).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            isSaved.setValue(task.isSuccessful());
        });
    }
}
