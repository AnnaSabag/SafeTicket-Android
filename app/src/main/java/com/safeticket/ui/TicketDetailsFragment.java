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

public class TicketDetailsFragment extends Fragment {

    // Keys for passing data
    public static final String ARG_NAME = "arg_name";
    public static final String ARG_PRICE = "arg_price";
    public static final String ARG_LOCATION = "arg_location";
    public static final String ARG_COUNTRY = "arg_country";
    public static final String ARG_SELLER = "arg_seller";
    public static final String ARG_PHONE = "arg_phone";
    public static final String ARG_IMAGE = "arg_image";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if we received data
        if (getArguments() != null) {
            String name = getArguments().getString(ARG_NAME);
            String price = getArguments().getString(ARG_PRICE);
            String location = getArguments().getString(ARG_LOCATION); // Get location
            String country = getArguments().getString(ARG_COUNTRY);   // Get country
            String seller = getArguments().getString(ARG_SELLER);
            String phone = getArguments().getString(ARG_PHONE);
            int imageResId = getArguments().getInt(ARG_IMAGE);

            // Set data to views
            ((TextView) view.findViewById(R.id.tvDetailTitle)).setText(name);
            ((TextView) view.findViewById(R.id.tvDetailPrice)).setText(price);
            ((TextView) view.findViewById(R.id.tvDetailLocation)).setText(location); // Set location
            ((TextView) view.findViewById(R.id.tvDetailCountry)).setText(country);   // Set country
            ((TextView) view.findViewById(R.id.tvSellerName)).setText("Seller: " + seller);
            ((TextView) view.findViewById(R.id.tvSellerPhone)).setText("Phone: " + phone);

            // Set the image to the ImageView
            if (imageResId != 0) {
                ((ImageView) view.findViewById(R.id.ivDetailImage)).setImageResource(imageResId);
            }
        }
    }
}