package com.safeticket.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.safeticket.R;
import java.util.Calendar;
import com.safeticket.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etID, etPhone, etBirthDate;
    private Button btnRegister;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Views
        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etID = findViewById(R.id.etRegisterID);       // New
        etPhone = findViewById(R.id.etRegisterPhone); // New
        etBirthDate = findViewById(R.id.etRegisterDate); // New
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Handle Date Picker for Birth Date
        etBirthDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Format: DD/MM/YYYY
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        etBirthDate.setText(selectedDate);
                    },
                    year, month, day);

            // Optional: Limit to past dates only (cannot be born in future)
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // Handle Register Logic
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String id = etID.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String birthDate = etBirthDate.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                    TextUtils.isEmpty(id) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(birthDate)) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                return;
            }

            if (id.length() != 9) {
                etID.setError("ID must be 9 digits");
                return;
            }

            // Success (For now, just move to Login or Main)
            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

            // In future: Save to Firebase here

            // Move to Main Activity (Simulated login)
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle "Go to Login"
        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}