package com.safeticket.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.safeticket.R;
import com.safeticket.model.User;

public class ProfileFragment extends Fragment {

    // UI Components
    private TextView tvName, tvEmail, tvPhone;
    private Button btnEditProfile;

    // Data (Currently simulated)
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Views
        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);

        // Note: Make sure you added an ID to the phone TextView in your XML (e.g., android:id="@+id/tvProfilePhone")
        // If not, add it to fragment_profile.xml, or this line might crash or be null
        tvPhone = view.findViewById(R.id.tvProfilePhone);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // 2. Simulate fetching current user (In future: Get from Firebase)
        if (currentUser == null) {
            currentUser = new User("uid123", "Israel", "Israeli", "123456789", "050-1234567", "israel@test.com");
            currentUser.setVerified(true);
        }

        // 3. Update UI with initial data
        updateUI();

        // 4. Handle Verification Status UI
        updateVerificationUI(view, currentUser.isVerified());

        // --- NEW: Handle "Edit Profile" Logic ---

        // A. Listen for results coming back from EditProfileFragment
        getParentFragmentManager().setFragmentResultListener("edit_profile_request", this, (requestKey, result) -> {
            String newName = result.getString("newName");
            String newPhone = result.getString("newPhone");

            // Update the User object and the UI
            // (Note: In a real app, splitting full name back to first/last is tricky,
            // but for now we just update the display)
            currentUser.setFirstName(newName); // Simplified for this example
            currentUser.setLastName("");       // Simplified
            currentUser.setPhoneNumber(newPhone);

            // Manually update the TextViews to show the change immediately
            tvName.setText(newName);
            if (tvPhone != null) {
                tvPhone.setText("Phone: " + newPhone);
            }

            Toast.makeText(getContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
        });

        // B. Open Edit Screen when button is clicked
        btnEditProfile.setOnClickListener(v -> {
            EditProfileFragment editFragment = new EditProfileFragment();

            // Pass current data to the edit screen so the fields aren't empty
            Bundle args = new Bundle();
            args.putString("name", tvName.getText().toString());

            // Extract just the number if the text is "Phone: 050..."
            String currentPhoneText = (tvPhone != null) ? tvPhone.getText().toString().replace("Phone: ", "") : "";
            args.putString("phone", currentPhoneText);

            editFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, editFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void updateUI() {
        tvName.setText(currentUser.getFullName());
        tvEmail.setText(currentUser.getEmail());
        if (tvPhone != null) {
            tvPhone.setText("Phone: " + currentUser.getPhoneNumber());
        }
    }

    private void updateVerificationUI(View view, boolean isVerified) {
        TextView tvStatus = view.findViewById(R.id.tvVerificationStatus);
        ImageView ivIcon = view.findViewById(R.id.ivVerificationIcon);
        View layout = view.findViewById(R.id.layoutVerification);

        if (isVerified) {
            tvStatus.setText("Verified User");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            layout.setBackgroundColor(0xFFE8F5E9); // Light Green
        } else {
            tvStatus.setText("Unverified");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            ivIcon.setImageResource(android.R.drawable.ic_delete); // X icon
            layout.setBackgroundColor(0xFFFFEBEE); // Light Red
        }
    }
}