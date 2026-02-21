package com.safeticket.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.safeticket.R;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private TextInputEditText etFullName, etPhone, etIDNumber;
    private Button btnSave;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false); // Return the inflated view
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance(); // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's ID

        // Initialize views
        etFullName = view.findViewById(R.id.etEditName);
        etPhone = view.findViewById(R.id.etEditPhone);
        etIDNumber = view.findViewById(R.id.etEditIDNumber);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        loadCurrentUserData(); // Load the current user's data


        btnSave.setOnClickListener(v -> saveProfileChanges()); // Handle the save button click
    }

    private void loadCurrentUserData() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) { // Check if the document exists
                // Load the current user's data
                etFullName.setText(documentSnapshot.getString("fullName"));
                etPhone.setText(documentSnapshot.getString("phoneNumber"));
                etIDNumber.setText(documentSnapshot.getString("idNumber"));
            }
        });
    }

    private void saveProfileChanges() { // Save the user's profile changes

        String fullName = etFullName.getText().toString().trim(); // Get the user's full name from the EditText. Trim any leading or trailing spaces.
        String phone = etPhone.getText().toString().trim();
        String idNumber = etIDNumber.getText().toString().trim();

        // Check details
        if (fullName.isEmpty() || phone.isEmpty() || idNumber.isEmpty()) {
            Toast.makeText(getContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idNumber.length() != 9) {
            Toast.makeText(getContext(), "מספר תעודת הזהות חייבל הכיל 9 ספרות", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = fullName;
        String lastName = "";
        if (fullName.contains(" ")) { // Check if the full name contains a space
            firstName = fullName.substring(0, fullName.indexOf(" ")); // Get the first name
            lastName = fullName.substring(fullName.indexOf(" ") + 1); // Get the last name
        }

        Map<String, Object> updates = new HashMap<>(); // Create a map to hold the updates
        // Add the updates
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("fullName", fullName);
        updates.put("phoneNumber", phone);
        updates.put("idNumber", idNumber);

        updates.put("isVerified", false); // Set the verification status to false


        db.collection("users").document(currentUserId)
                .update(updates) // Update the user's data
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "הפרופיל עודכן ואינו מאומת יותר", Toast.LENGTH_LONG).show();

                    getParentFragmentManager().setFragmentResult("edit_profile_request", new Bundle());

                    getParentFragmentManager().popBackStack(); // Navigate back to the previous fragment
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "שגיאה בעדכון הפרופיל", Toast.LENGTH_SHORT).show());
    }
}