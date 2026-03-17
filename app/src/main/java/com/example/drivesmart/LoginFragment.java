package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToSignup;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ניפוח ה-Layout המעודכן
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();

        // אתחול רכיבים
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoToSignup = view.findViewById(R.id.btnGoToSignup);
        progressBar = view.findViewById(R.id.progressLoader);

        // מאזינים ללחיצות
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginUser());
        }

        if (btnGoToSignup != null) {
            btnGoToSignup.setOnClickListener(v -> {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).switchFragment(new SignupFragment());
                }
            });
        }

        return view;
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (isAdded()) { // בדיקה שהפרגמנט עדיין מחובר ל-Activity
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            startActivity(new Intent(getActivity(), HomeActivity.class));
                            getActivity().finish();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}