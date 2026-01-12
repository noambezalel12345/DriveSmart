package com.example.drivesmart;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class SignupViewModel extends ViewModel {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSignupEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsSignupEnabled() {
        return isSignupEnabled;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSignupSuccess() {
        return signupSuccess;
    }

    public void validateInputs(String email,
                               String pass,
                               String confirmPass,
                               String vehicleNumber,
                               String vehicleModel,
                               String vehicleYear) {

        boolean valid =
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                        pass.length() >= 6 &&
                        pass.equals(confirmPass) &&
                        !vehicleNumber.isEmpty() &&
                        !vehicleModel.isEmpty() &&
                        !vehicleYear.isEmpty();

        isSignupEnabled.setValue(valid);
    }

    public void signup(String email, String pass) {
        isLoading.setValue(true);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);

                    if (task.isSuccessful()) {
                        signupSuccess.setValue(true);
                    } else {
                        errorMessage.setValue(
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Signup failed"
                        );
                    }
                });
    }
}
