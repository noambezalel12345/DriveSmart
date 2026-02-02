package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToSignup;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // אתחול רכיבים לפי ה-IDs ב-XML שלך
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToSignup = findViewById(R.id.btnGoToSignup);
        progressBar = findViewById(R.id.progressLoader);

        // מאזין לשינוי טקסט כדי להפעיל את כפתור ההתחברות
        TextWatcher loginWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                // הכפתור יהיה פעיל רק אם שני השדות אינם ריקים
                btnLogin.setEnabled(!email.isEmpty() && !password.isEmpty());
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(loginWatcher);
        etPassword.addTextChangedListener(loginWatcher);

        // כפתור התחברות
        btnLogin.setOnClickListener(v -> loginUser());

        // כפתור מעבר להרשמה
        btnGoToSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        btnLogin.setEnabled(true);
                        String error = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                        Toast.makeText(this, "שגיאה: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}