package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private EditText etVehicleNumber, etVehicleModel, etVehicleYear;
    private Button btnSignup;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // חיבור ל־XML
        etEmail = findViewById(R.id.etSignupEmail);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etSignupConfirmPassword);
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        etVehicleModel = findViewById(R.id.etVehicleModel);
        etVehicleYear = findViewById(R.id.etVehicleYear);
        btnSignup = findViewById(R.id.btnSignup);

        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(v -> validateAndSignup());
    }

    private void validateAndSignup() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();
        String vehicleNumber = etVehicleNumber.getText().toString().trim();
        String vehicleModel = etVehicleModel.getText().toString().trim();
        String vehicleYear = etVehicleYear.getText().toString().trim();

        // אימות בסיסי
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }
        if (pass.length() < 6) {
            etPassword.setError("Password must be at least 6 chars");
            return;
        }
        if (!pass.equals(confirmPass)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }
        if (vehicleNumber.isEmpty()) {
            etVehicleNumber.setError("Enter vehicle number");
            return;
        }
        if (vehicleModel.isEmpty()) {
            etVehicleModel.setError("Enter vehicle model");
            return;
        }
        if (vehicleYear.isEmpty()) {
            etVehicleYear.setError("Enter vehicle year");
            return;
        }

        // יצירת משתמש ב-Firebase
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "User registered!", Toast.LENGTH_SHORT).show();
                        // כאן אפשר לשלוח את שדות הרכב ל-Firestore או Realtime DB אם רוצים
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
