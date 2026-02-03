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

public class AddTicketFragment extends Fragment {

    private TextInputEditText etEventName, etPrice, etLocation, etCountry;
    private Button btnSaveTicket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_ticket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        etEventName = view.findViewById(R.id.etEventName);
        etPrice = view.findViewById(R.id.etPrice);
        etLocation = view.findViewById(R.id.etLocation);
        etCountry = view.findViewById(R.id.etCountry);
        btnSaveTicket = view.findViewById(R.id.btnSaveTicket);

        btnSaveTicket.setOnClickListener(v -> {
            String name = etEventName.getText().toString();
            String price = etPrice.getText().toString();
            String location = etLocation.getText().toString();
            String country = etCountry.getText().toString();

            if (name.isEmpty() || price.isEmpty() || location.isEmpty() || country.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a Bundle with the data
            Bundle result = new Bundle();
            result.putString("name", name);
            result.putString("price", price + " ₪");
            result.putString("location", location);
            result.putString("country", country);

            // Send the result to the main screen (Feed)
            getParentFragmentManager().setFragmentResult("add_ticket_request", result);

            // Go back
            getParentFragmentManager().popBackStack();
        });
    }
}