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
import com.safeticket.R;

public class EditProfileFragment extends Fragment {

    private TextInputEditText etName, etPhone;
    private Button btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etEditName);
        etPhone = view.findViewById(R.id.etEditPhone);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        // 1. Get arguments passed from ProfileFragment (to pre-fill the fields)
        if (getArguments() != null) {
            String currentName = getArguments().getString("name");
            String currentPhone = getArguments().getString("phone");
            etName.setText(currentName);
            etPhone.setText(currentPhone);
        }

        // 2. Handle Save
        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString();
            String newPhone = etPhone.getText().toString();

            if (newName.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Send result back to ProfileFragment
            Bundle result = new Bundle();
            result.putString("newName", newName);
            result.putString("newPhone", newPhone);
            getParentFragmentManager().setFragmentResult("edit_profile_request", result);

            getParentFragmentManager().popBackStack(); // Go back
        });
    }
}