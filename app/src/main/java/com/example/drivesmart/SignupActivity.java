package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class SignupActivity extends AppCompatActivity {

    EditText emailInput, passwordInput, confirmPasswordInput;
    Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

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
            passwordInput.setError("Password must be 6 characters or more");
            return;
        }

        // אימות סיסמה
        if (!pass.equals(confirmPass)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        // אם הכול תקין
        Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();

        // אם תקין → כניסה לאפליקציה
        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
        startActivity(intent);

    }
}
