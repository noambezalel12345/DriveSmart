package com.example.drivesmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button button = findViewById(R.id.button);
        EditText input = findViewById(R.id.inputText);
        TextView output = findViewById(R.id.outputText);
        Button button2 = findViewById(R.id.button2);


        button.setOnClickListener(v -> {
           output.setText(input.getText());
        });

        button2.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, signUpActivity.class);
            startActivity(intent);


        });
    }
}

