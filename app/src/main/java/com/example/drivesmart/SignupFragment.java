package com.example.drivesmart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignupFragment extends Fragment {

    private EditText etEmail, etPassword, etConfirmPassword, etVehicleNumber, etVehicleYear;
    private AutoCompleteTextView etVehicleModel;
    private Button btnSignup;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String[] CAR_MODELS = {
            "Toyota", "Hyundai", "Kia", "Mazda", "Skoda", "Mitsubishi", "Suzuki", "Tesla",
            "Chevrolet", "Ford", "Volkswagen", "Renault", "Nissan", "Seat", "Peugeot", "Citroen",
            "Audi", "BMW", "Mercedes-Benz", "Honda", "Volvo", "Subaru", "Lexus", "Jeep", "Fiat",
            "Land Rover", "Jaguar", "Porsche", "Alfa Romeo", "Cupra", "MG", "BYD", "Geely", "Chery",
            "Ferrari", "Lamborghini", "Maserati", "Bentley", "Aston Martin", "Rolls-Royce", "McLaren",
            "Bugatti", "Lotus", "Genesis", "Cadillac", "Chrysler", "Dodge", "Ram", "Buick", "GMC",
            "Lincoln", "Infiniti", "Acura", "Mini", "Smart", "Abarth", "Dacia", "Lancia", "Opel",
            "NIO", "Rivian", "Lucid", "Zeekr", "Polestar", "VinFast", "Voyah", "Seres", "Skywell"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        Arrays.sort(CAR_MODELS);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = view.findViewById(R.id.etSignupEmail);
        etPassword = view.findViewById(R.id.etSignupPassword);
        etConfirmPassword = view.findViewById(R.id.etSignupConfirmPassword);
        etVehicleNumber = view.findViewById(R.id.etVehicleNumber);
        etVehicleModel = view.findViewById(R.id.etVehicleModel);
        etVehicleYear = view.findViewById(R.id.etVehicleYear);
        btnSignup = view.findViewById(R.id.btnSignup);
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);
        progressBar = view.findViewById(R.id.progressLoader);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, CAR_MODELS);
        etVehicleModel.setAdapter(adapter);

        etVehicleYear.setOnClickListener(v -> showYearPicker());
        btnSignup.setOnClickListener(v -> signupUser());
        tvBackToLogin.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    private void showYearPicker() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        final NumberPicker picker = new NumberPicker(getContext());
        picker.setMinValue(1950);
        picker.setMaxValue(currentYear);
        picker.setValue(currentYear);
        picker.setWrapSelectorWheel(true);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setGravity(Gravity.CENTER);
        layout.addView(picker);

        new AlertDialog.Builder(getContext())
                .setTitle("Select Year")
                .setView(layout)
                .setPositiveButton("OK", (d, w) -> etVehicleYear.setText(String.valueOf(picker.getValue())))
                .show();
    }

    private void signupUser() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();
        String vNum = etVehicleNumber.getText().toString().trim();
        String vModel = etVehicleModel.getText().toString().trim();
        String vYear = etVehicleYear.getText().toString().trim();

        // בדיקת אימייל וסיסמה (לפחות 6 תווים)
        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // בדיקת אימות סיסמה
        if (!pass.equals(confirm)) {
            Toast.makeText(getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // בדיקת מספר רכב (6-8 ספרות)
        if (!vNum.matches("\\d{6,8}")) {
            Toast.makeText(getContext(), "Vehicle number must be 6-8 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // בדיקת דגם רכב
        if (vModel.isEmpty()) {
            Toast.makeText(getContext(), "Please select a vehicle model", Toast.LENGTH_SHORT).show();
            return;
        }

        // בדיקת שנת רכב
        if (vYear.isEmpty()) {
            Toast.makeText(getContext(), "Please select vehicle year", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false); // חסימת הכפתור למניעת לחיצות כפולות

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveUserData(mAuth.getCurrentUser().getUid(), vNum, vModel, vYear);
            } else {
                progressBar.setVisibility(View.GONE);
                btnSignup.setEnabled(true);
                Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData(String uid, String vNum, String vMod, String vYear) {
        Map<String, Object> user = new HashMap<>();
        user.put("vehicleNumber", vNum);
        user.put("vehicleModel", vMod);
        user.put("vehicleYear", vYear);
        user.put("maintenances", new ArrayList<>());

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), HomeActivity.class));
                        getActivity().finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSignup.setEnabled(true);
                    Toast.makeText(getContext(), "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}