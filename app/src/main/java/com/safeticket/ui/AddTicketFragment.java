package com.safeticket.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.safeticket.R;

public class AddTicketFragment extends Fragment {

    private TextInputEditText etEventName, etPrice, etOriginalPrice, etLocation, etCountry;
    private Spinner spCategory;
    private Button btnSaveTicket;
    private ImageView ivTicketImage;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    selectedImageUri = result;
                    ivTicketImage.setImageURI(result);
                }
            }
    );

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
        etOriginalPrice = view.findViewById(R.id.etOriginalPrice);
        spCategory = view.findViewById(R.id.spCategory);
        etLocation = view.findViewById(R.id.etLocation);
        etCountry = view.findViewById(R.id.etCountry);
        btnSaveTicket = view.findViewById(R.id.btnSaveTicket);
        ivTicketImage = view.findViewById(R.id.ivTicketImage);

        // Setup Category Spinner
        String[] categories = {"Concert", "Sport", "Theater", "Movie", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spCategory.setAdapter(adapter);

        // Handle Image Click
        ivTicketImage.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnSaveTicket.setOnClickListener(v -> {
            String name = etEventName.getText().toString();
            String priceStr = etPrice.getText().toString();
            String originalPriceStr = etOriginalPrice.getText().toString();
            String location = etLocation.getText().toString();
            String country = etCountry.getText().toString();
            String category = spCategory.getSelectedItem().toString();

            if (name.isEmpty() || priceStr.isEmpty() || originalPriceStr.isEmpty() || location.isEmpty() || country.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all text fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle result = new Bundle();
            result.putString("name", name);
            result.putString("price", priceStr);
            result.putString("originalPrice", originalPriceStr);
            result.putString("category", category);
            result.putString("location", location);
            result.putString("country", country);

            // Handle optional image safely
            if (selectedImageUri != null) {
                result.putString("uri", selectedImageUri.toString());
            } else {
                result.putString("uri", ""); // Send empty string if no image
            }

            getParentFragmentManager().setFragmentResult("add_ticket_request", result);
            getParentFragmentManager().popBackStack();
        });
    }
}