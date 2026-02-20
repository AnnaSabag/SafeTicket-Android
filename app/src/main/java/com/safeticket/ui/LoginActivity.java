package com.safeticket.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.safeticket.MainActivity;
import com.safeticket.R;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    // Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the layout for the activity

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnLogin.setOnClickListener(new View.OnClickListener() { // Login button click listener
            @Override
            public void onClick(View v) {
                loginUser();
            } // Method to handle user login
        });

        tvRegisterLink.setOnClickListener(new View.OnClickListener() { // Register link click listener
            @Override
            public void onClick(View v) { // Method to navigate to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class); // Create an intent to navigate to RegisterActivity
                startActivity(intent); // Start the activity
            }
        });
    }

    private void loginUser() { // Method to handle user login
        // Trim to remove leading/trailing spaces, get the text from the EditText
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // Validation logic- check if email is valid and not empty
            etEmail.setError("נא להזין כתובת אימייל תקינה"); // Set error message
            etEmail.requestFocus(); // Set focus on the EditText
            return; // Exit the method
        }

        if (password.isEmpty() || password.length() < 6) { // Validation logic- check if password is valid and not empty
            etPassword.setError("הסיסמה חייבת להכיל לפחות 6 תווים");
            etPassword.requestFocus();
            return;
        }

        // --- Start Firebase Login Logic ---
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> { // Firebase Login
                    if (task.isSuccessful()) { // If login is successful
                        Toast.makeText(LoginActivity.this, "ברוכים השבים! ", Toast.LENGTH_SHORT).show();

                        // Navigate to MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class); // Create an intent to navigate to MainActivity
                        startActivity(intent); // Start the activity
                        finish(); // Finish the current activity
                    } else { // Fail - maybe wrong password or no internet
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "התחברות נכשלה"; // Get the error message
                        Toast.makeText(LoginActivity.this, "שגיאה: " + errorMessage, Toast.LENGTH_LONG).show(); // Display the error message

                    }
                });
    }
}