package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginButton, signUpButton;

    private FirebaseAuth auth; // 🔥 Firebase Auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // אתחול Firebase
        auth = FirebaseAuth.getInstance();

        // חיבור לאלמנטים ב-XML
        emailInput = findViewById(R.id.inputEmail);
        passwordInput = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.buttonLogin);
        signUpButton = findViewById(R.id.button2);

        // כפתור התחברות
        loginButton.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // ולידציה לאימייל
            if (!isValidEmail(email)) {
                emailInput.setError("נא להזין אימייל תקין (example@mail.com)");
                return;
            }

            // ולידציה לסיסמה
            if (!isValidPassword(password)) {
                passwordInput.setError("סיסמה חייבת להכיל לפחות 6 תווים");
                return;
            }

            // 🔥 התחברות ל-Firebase
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            // התחברות הצליחה
                            Toast.makeText(this, "ברוך הבא!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // כישלון בהתחברות
                            Toast.makeText(this,
                                    "שגיאה בהתחברות: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // מעבר למסך הרשמה
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    // בדיקת אימייל תקין
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // בדיקת סיסמה תקינה
    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }
}
