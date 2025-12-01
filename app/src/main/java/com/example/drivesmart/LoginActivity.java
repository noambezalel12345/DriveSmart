package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginButton, signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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



            // אם תקין → כניסה לאפליקציה
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
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
