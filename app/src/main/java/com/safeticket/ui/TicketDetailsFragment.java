package com.safeticket.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.safeticket.R;

public class TicketDetailsFragment extends Fragment {

    // Keys for arguments
    public static final String ARG_ID = "id"; // New
    public static final String ARG_NAME = "name";
    public static final String ARG_PRICE = "price";
    public static final String ARG_LOCATION = "location";
    public static final String ARG_COUNTRY = "country";
    public static final String ARG_SELLER = "seller";
    public static final String ARG_PHONE = "phone";
    public static final String ARG_IMAGE = "image";

    private String ticketId; // To store the ID locally

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve data from Bundle
        if (getArguments() != null) {
            ticketId = getArguments().getString(ARG_ID); // Get ID
            String name = getArguments().getString(ARG_NAME);
            String price = getArguments().getString(ARG_PRICE);
            String location = getArguments().getString(ARG_LOCATION);
            String country = getArguments().getString(ARG_COUNTRY);
            String seller = getArguments().getString(ARG_SELLER);
            String phone = getArguments().getString(ARG_PHONE);
            int imageRes = getArguments().getInt(ARG_IMAGE);

            // Set data to views
            ((TextView) view.findViewById(R.id.tvDetailEventName)).setText(name);
            ((TextView) view.findViewById(R.id.tvDetailPrice)).setText(price);
            ((TextView) view.findViewById(R.id.tvDetailLocation)).setText(location + ", " + country);
            ((TextView) view.findViewById(R.id.tvDetailSeller)).setText("Seller: " + seller);
            ((TextView) view.findViewById(R.id.tvDetailPhone)).setText("Phone: " + phone);
            ((ImageView) view.findViewById(R.id.ivDetailImage)).setImageResource(imageRes);
        }

        // --- Handle DELETE ---
        Button btnDelete = view.findViewById(R.id.btnDeleteTicket);
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Ticket")
                    .setMessage("Are you sure you want to delete this ticket?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Send Delete Request
                        Bundle result = new Bundle();
                        result.putString("action", "delete");
                        result.putString("ticketId", ticketId);
                        getParentFragmentManager().setFragmentResult("ticket_action_request", result);
                        getParentFragmentManager().popBackStack(); // Close fragment
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // --- Handle EDIT (Price) ---
        Button btnEdit = view.findViewById(R.id.btnEditTicket);
        btnEdit.setOnClickListener(v -> {
            // Create a simple dialog with an input field
            EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setHint("Enter new price");

            new AlertDialog.Builder(getContext())
                    .setTitle("Update Price")
                    .setView(input)
                    .setPositiveButton("Update", (dialog, which) -> {
                        String newPriceStr = input.getText().toString();
                        if (!newPriceStr.isEmpty()) {
                            // Send Update Request
                            Bundle result = new Bundle();
                            result.putString("action", "update_price");
                            result.putString("ticketId", ticketId);
                            result.putDouble("newPrice", Double.parseDouble(newPriceStr));
                            getParentFragmentManager().setFragmentResult("ticket_action_request", result);
                            getParentFragmentManager().popBackStack(); // Close fragment
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}