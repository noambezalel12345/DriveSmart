package com.example.drivesmart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddMaintenanceViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isSaveEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSaved = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsSaveEnabled() {
        return isSaveEnabled;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsSaved() {
        return isSaved;
    }

    public void onTitleChanged(String title) {
        isSaveEnabled.setValue(title.length() >= 2 && title.length() <= 40);
    }

    public void saveMaintenance() {
        isLoading.setValue(true);
        isSaveEnabled.setValue(false);

        // סימולציה של שמירה (בפועל כאן Firebase)
        new android.os.Handler().postDelayed(() -> {
            isLoading.setValue(false);
            isSaved.setValue(true);
        }, 2000);
    }
}
