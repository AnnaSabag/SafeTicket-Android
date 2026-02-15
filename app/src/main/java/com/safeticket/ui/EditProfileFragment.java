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
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // אתחול רכיבים
        etFullName = view.findViewById(R.id.etEditName);
        etPhone = view.findViewById(R.id.etEditPhone);
        etIDNumber = view.findViewById(R.id.etEditIDNumber);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        // טעינת נתונים נוכחיים מה-DB כדי שהמשתמש יראה מה הוא עורך
        loadCurrentUserData();

        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadCurrentUserData() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etFullName.setText(documentSnapshot.getString("fullName"));
                etPhone.setText(documentSnapshot.getString("phoneNumber"));
                etIDNumber.setText(documentSnapshot.getString("idNumber"));
            }
        });
    }

    private void saveProfileChanges() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String idNumber = etIDNumber.getText().toString().trim();

        // ולידציה בסיסית
        if (fullName.isEmpty() || phone.isEmpty() || idNumber.isEmpty()) {
            Toast.makeText(getContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idNumber.length() != 9) {
            Toast.makeText(getContext(), "מספר תעודת הזהות חייבל הכיל 9 ספרות", Toast.LENGTH_SHORT).show();
            return;
        }

        // הפרדת שם (למקרה שהמערכת משתמשת בשם פרטי בנפרד)
        String firstName = fullName;
        String lastName = "";
        if (fullName.contains(" ")) {
            firstName = fullName.substring(0, fullName.indexOf(" "));
            lastName = fullName.substring(fullName.indexOf(" ") + 1);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("fullName", fullName);
        updates.put("phoneNumber", phone);
        updates.put("idNumber", idNumber);

        // השורה הקריטית - איפוס אימות המשתמש
        updates.put("isVerified", false);

        db.collection("users").document(currentUserId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "הפרופיל עודכן ואינו מאומת יותר", Toast.LENGTH_LONG).show();

                    // שליחת הודעה לפרופיל להתעדכן
                    getParentFragmentManager().setFragmentResult("edit_profile_request", new Bundle());

                    // חזרה למסך הפרופיל
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "שגיאה בעדכון הפרופיל", Toast.LENGTH_SHORT).show());
    }
}