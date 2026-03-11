package com.example.drivesmart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword, etVehicleNumber, etVehicleYear;
    private AutoCompleteTextView etVehicleModel;
    private Button btnSignup;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // רשימה מורחבת הכוללת מותגי יוקרה ורכבי על
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Arrays.sort(CAR_MODELS); // מיון אלפביתי

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etSignupEmail);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etSignupConfirmPassword);
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        etVehicleModel = findViewById(R.id.etVehicleModel);
        etVehicleYear = findViewById(R.id.etVehicleYear);
        btnSignup = findViewById(R.id.btnSignup);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.progressLoader);

        // הגדרת AutoComplete
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CAR_MODELS);
        etVehicleModel.setAdapter(adapter);

        // בחירת שנה גלילה
        etVehicleYear.setOnClickListener(v -> showYearPicker());

        btnSignup.setOnClickListener(v -> signupUser());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void showYearPicker() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        final NumberPicker numberPicker = new NumberPicker(this);

        numberPicker.setMinValue(1950);
        numberPicker.setMaxValue(currentYear + 1);
        numberPicker.setValue(currentYear);
        numberPicker.setWrapSelectorWheel(true); // גלילה מעגלית

        // מרכוז הגלגלת בתוך הדיאלוג
        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.addView(numberPicker);

        new AlertDialog.Builder(this)
                .setTitle("Select Vehicle Year")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    etVehicleYear.setText(String.valueOf(numberPicker.getValue()));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void signupUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String vNumber = etVehicleNumber.getText().toString().trim();
        String vModel = etVehicleModel.getText().toString().trim();
        String vYear = etVehicleYear.getText().toString().trim();

        // 1. בדיקת ריקנות וסיסמה
        if (email.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Email is required and password must be at least 6 chars", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. בדיקת אימות סיסמה
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. בדיקת מספר רכב (6-8 ספרות)
        if (!vNumber.matches("\\d{6,8}")) {
            Toast.makeText(this, "Vehicle number must be 6-8 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. בדיקת דגם מהרשימה
        boolean isValidModel = false;
        for (String m : CAR_MODELS) {
            if (m.equalsIgnoreCase(vModel)) {
                isValidModel = true;
                vModel = m; // שומר את השם עם האותיות הגדולות הנכונות
                break;
            }
        }
        if (!isValidModel) {
            Toast.makeText(this, "Please select a valid brand from the list", Toast.LENGTH_SHORT).show();
            return;
        }

        if (vYear.isEmpty()) {
            Toast.makeText(this, "Please select vehicle year", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);

        String finalVModel = vModel;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserData(mAuth.getCurrentUser().getUid(), vNumber, finalVModel, vYear);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnSignup.setEnabled(true);
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(String userId, String vNumber, String vModel, String vYear) {
        Map<String, Object> user = new HashMap<>();
        user.put("vehicleNumber", vNumber);
        user.put("vehicleModel", vModel);
        user.put("vehicleYear", vYear);
        user.put("maintenances", new ArrayList<>());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSignup.setEnabled(true);
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}