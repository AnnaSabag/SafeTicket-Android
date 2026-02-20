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
        setContentView(R.layout.activity_register); // Set the layout for the activity

        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        db = FirebaseFirestore.getInstance(); // Initialize FirebaseFirestore

        // Initialize Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etPhone);
        etIDNumber = findViewById(R.id.etIDNumber);
        etPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        btnRegister.setOnClickListener(v -> registerUser()); // Register button click listener - will call registerUser()

        tvLoginLink.setOnClickListener(v -> finish()); // Login link click listener - will finish the activity
    }

    private void registerUser() { // Method to handle user registration
        // Trim to remove leading/trailing spaces, get the text from the EditText
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String idNumber = etIDNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        //Validation Logic
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

        //Firebase Registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) { // If registration is successful
                        String userId = mAuth.getCurrentUser().getUid(); // Create unique ID in the DB for the user
                        // Split full name logic
                        String firstName = fullName;
                        String lastName = "";
                        if (fullName.contains(" ")) { // If there is a space in the name
                            firstName = fullName.substring(0, fullName.indexOf(" ")); // Extract first name
                            lastName = fullName.substring(fullName.indexOf(" ") + 1); // Extract last name
                        }

                        // Create User object using the updated constructor from your User.java
                        User newUser = new User(userId, firstName, lastName, idNumber, phone, email);

                        // Set additional default fields
                        newUser.setVerified(false);

                        saveUserToDatabase(newUser); // Save the user to the database

                    } else { // If registration fails
                        String error = task.getException() != null ? task.getException().getMessage() : "ההתחברות נכשלה"; // Get the error message
                        Toast.makeText(RegisterActivity.this, "שגיאה: " + error, Toast.LENGTH_LONG).show(); // Display the error message
                    }
                });
    }

    private void saveUserToDatabase(User user) { // Save user to the database
        db.collection("users").document(user.getUserId()) // Save the user to the "users" collection with the user ID as the document ID
                .set(user) // Set the user object as the document
                .addOnSuccessListener(aVoid -> showSuccessDialog()) // If successful, show success dialog
                .addOnFailureListener(e -> { // If failed, show error message
                    Toast.makeText(RegisterActivity.this, "שגיאה בבסיס נתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this) // Show success dialog
                .setTitle("ההרשמה הצליחה!") // Title of success
                .setMessage("נרשמת בהצלחה למערכת! כעת ניתן להתחבר ולרכוש כרטיסים או למכור לאחר אימות נתונים") // Message of success
                .setPositiveButton("אישור", (dialog, which) -> finish()) // Button to close the activity
                .setCancelable(false) // Disable canceling the dialog
                .setIcon(android.R.drawable.ic_dialog_info) // !
                .show(); // Show the dialog
    }
}