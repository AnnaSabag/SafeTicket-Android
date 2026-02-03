package com.safeticket.ui;

// --- Imports Section ---
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // For the pop-up window
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.safeticket.R;

public class RegisterActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etFullName, etEmail, etPhone, etPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Handle Register Button Click
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Handle "Login Here" Link Click
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to LoginActivity
                finish(); // Just close this activity to go back
            }
        });
    }

    private void registerUser() {
        // Get data from inputs
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Pop-up in the end of regestration
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful")
                .setMessage("You have successfully registered! Please login with your new account.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();

        // TODO: Create user in Firebase Authentication and Firestore
        Toast.makeText(this, "Register validation passed!", Toast.LENGTH_SHORT).show();
    }
}