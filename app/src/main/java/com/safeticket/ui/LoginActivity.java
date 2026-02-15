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

    // Firebase instances (Our logic)
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("נא להזין כתובת אימייל תקינה");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("הסיסמה חייבת להכיל לפחות 6 תווים");
            etPassword.requestFocus();
            return;
        }

        // --- Start Firebase Login Logic ---
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Success! User is authenticated
                        Toast.makeText(LoginActivity.this, "ברוכים השבים! ", Toast.LENGTH_SHORT).show();

                        // Navigate to MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Fail - maybe wrong password or no internet
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "התחברות נכשלה";
                        Toast.makeText(LoginActivity.this, "שגיאה: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}