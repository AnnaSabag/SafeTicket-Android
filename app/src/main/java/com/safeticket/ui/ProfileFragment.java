package com.safeticket.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.safeticket.R;
import com.safeticket.model.User;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Simulate fetching current user (In future: Get from Firebase)
        User currentUser = new User("uid123", "Israel", "Israeli", "123456789", "050-1234567", "israel@test.com");
        currentUser.setVerified(true); // Toggle this to test "Unverified" status!

        // Update UI
        ((TextView) view.findViewById(R.id.tvProfileName)).setText(currentUser.getFullName());
        ((TextView) view.findViewById(R.id.tvProfileEmail)).setText(currentUser.getEmail());

        // Handle Verification Status
        updateVerificationUI(view, currentUser.isVerified());
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