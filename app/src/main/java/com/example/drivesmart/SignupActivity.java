package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private EditText etVehicleNumber, etVehicleModel, etVehicleYear;
    private Button btnSignup;
    private ProgressBar progressBar;

    private SignupViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        viewModel = new ViewModelProvider(this).get(SignupViewModel.class);

        etEmail = findViewById(R.id.etSignupEmail);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etSignupConfirmPassword);
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        etVehicleModel = findViewById(R.id.etVehicleModel);
        etVehicleYear = findViewById(R.id.etVehicleYear);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressLoader); // תוודא שיש ב־XML

        // Observers
        viewModel.getIsSignupEnabled().observe(this,
                enabled -> btnSignup.setEnabled(Boolean.TRUE.equals(enabled)));

        viewModel.getIsLoading().observe(this,
                loading -> progressBar.setVisibility(loading ? ProgressBar.VISIBLE : ProgressBar.GONE));

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSignupSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "User registered!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.validateInputs(
                        etEmail.getText().toString().trim(),
                        etPassword.getText().toString().trim(),
                        etConfirmPassword.getText().toString().trim(),
                        etVehicleNumber.getText().toString().trim(),
                        etVehicleModel.getText().toString().trim(),
                        etVehicleYear.getText().toString().trim()
                );
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);
        etVehicleNumber.addTextChangedListener(watcher);
        etVehicleModel.addTextChangedListener(watcher);
        etVehicleYear.addTextChangedListener(watcher);

        btnSignup.setOnClickListener(v ->
                viewModel.signup(
                        etEmail.getText().toString().trim(),
                        etPassword.getText().toString().trim()
                )
        );
    }
}
