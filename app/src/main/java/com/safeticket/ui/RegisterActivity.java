package com.safeticket.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.safeticket.R;
import com.safeticket.model.User;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etIDNumber;
    private Button btnRegister;
    private TextView tvLoginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etPhone);
        etIDNumber = findViewById(R.id.etIDNumber); // New ID field
        etPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String idNumber = etIDNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // --- Validation Logic ---
        if (fullName.isEmpty()) {
            etFullName.setError("נדרש למלא שם מלא");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("נדרש למלא כתובת אימייל");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("נדרש למלא מספר טלפון");
            return;
        }
        if (idNumber.length() < 9) {
            etIDNumber.setError("מספר תעודת הזהות צריכה להכיל 9 ספרות");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("הסיסמא חייבת להכיל לפחות 6 תווים");
            return;
        }

        // --- Firebase Registration ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Split full name logic
                        String firstName = fullName;
                        String lastName = "";
                        if (fullName.contains(" ")) {
                            firstName = fullName.substring(0, fullName.indexOf(" "));
                            lastName = fullName.substring(fullName.indexOf(" ") + 1);
                        }

                        // Create User object using the updated constructor from your User.java
                        User newUser = new User(userId, firstName, lastName, idNumber, phone, email);

                        // Set additional default fields
                        newUser.setVerified(false);
                        newUser.setRating(0.0);

                        saveUserToDatabase(newUser);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "ההתחברות נכשלה";
                        Toast.makeText(RegisterActivity.this, "שגיאה: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(User user) {
        db.collection("users").document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> showSuccessDialog())
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "שגיאה בבסיס נתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("ההרשמה הצליחה!")
                .setMessage("נרשמת בהצלחה למערכת! כעת ניתן להתחבר ולרכוש כרטיסים או למכור לאחר אימות נתונים")
                .setPositiveButton("אישור", (dialog, which) -> finish())
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}