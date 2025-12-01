package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    EditText emailInput, passwordInput, confirmPasswordInput;
    Button signupBtn;

    private FirebaseAuth auth;  // ← Firebase Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // אתחול Firebase
        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signupBtn = findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(v -> validateData());
    }

    private void validateData() {

        String email = emailInput.getText().toString().trim();
        String pass = passwordInput.getText().toString().trim();
        String confirmPass = confirmPasswordInput.getText().toString().trim();

        // אימייל ריק
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return;
        }

        // אימייל לא תקין
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Invalid email format");
            return;
        }

        // סיסמה ריקה
        if (pass.isEmpty()) {
            passwordInput.setError("Password is required");
            return;
        }

        // סיסמה קצרה מדי
        if (pass.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return;
        }

        // אימות סיסמה
        if (!pass.equals(confirmPass)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        // אם הכול תקין → יצירת משתמש ב-Firebase
        createUser(email, pass);
    }

    private void createUser(String email, String pass) {
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show();

                        // מעבר למסך הבית
                        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        // שגיאה מ-Firebase (אימייל קיים, סיסמה חלשה וכו')
                        Toast.makeText(
                                this,
                                "Signup failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
