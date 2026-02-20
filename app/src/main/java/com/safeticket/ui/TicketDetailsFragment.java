package com.safeticket.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.safeticket.R;

public class TicketDetailsFragment extends Fragment { // Declare the fragment for ticket details

    private FirebaseFirestore db; // Declare the Firebase Firestore instance, used for accessing the database
    private String sellerPhone = ""; // Declare the seller's phone number
    private String currentUserId; // Declare the current user's ID


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket_details, container, false);// Return the inflated view
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // Called when the view is created
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getUid(); // Get the current user's ID

        if (getArguments() != null) { // Check if the arguments are not null
            // Get the ticket details from the arguments
            String ticketId = getArguments().getString("ticketId");
            String name = getArguments().getString("eventName");
            double askingPrice = getArguments().getDouble("askingPrice");
            double originalPrice = getArguments().getDouble("originalPrice");
            int quantity = getArguments().getInt("quantity", 1);
            String location = getArguments().getString("location");
            String exactAddress = getArguments().getString("exactAddress");
            String sellerId = getArguments().getString("sellerId");
            String imageBase64 = getArguments().getString("ticketImage");
            String date = getArguments().getString("eventDate");
            String time = getArguments().getString("eventTime");
            String category = getArguments().getString("category");

            // Set the ticket details in the view
            ((TextView) view.findViewById(R.id.tvDetailEventName)).setText(name);
            ((TextView) view.findViewById(R.id.tvDetailPrice)).setText(askingPrice + " ₪");
            ((TextView) view.findViewById(R.id.tvDetailCategory)).setText(category);
            ((TextView) view.findViewById(R.id.tvDetailDateTime)).setText(date + " | " + time);
            ((TextView) view.findViewById(R.id.tvDetailQuantity)).setText("כמות זמינה: " + quantity);

            String fullLocation = location; // Set the full location
            if (exactAddress != null && !exactAddress.isEmpty()) fullLocation += ", " + exactAddress;
            ((TextView) view.findViewById(R.id.tvDetailLocation)).setText(fullLocation);


            if (ticketId != null && currentUserId != null && !currentUserId.equals(sellerId)) { // Check if the ticket ID is not null and the current user is not the seller
                updateViewCount(ticketId); // Update the view count
            }

            setupPriceAndImage(view, originalPrice, askingPrice, imageBase64); // Set up the price and image

            if (sellerId != null) {
                fetchSellerDetails(view, sellerId, name); // Fetch the seller details
            }
        }
    }

    private void updateViewCount(String ticketId) { // Update the view count for the ticket
        // Check if the ticket has been viewed by the current user
        db.collection("tickets").document(ticketId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) { // Check if the document exists
                java.util.List<String> viewedBy = (java.util.List<String>) doc.get("viewedBy"); // Get the list of users who have viewed the ticket
                if (viewedBy == null || !viewedBy.contains(currentUserId)) { // Check if the current user has not viewed the ticket
                    // Update the view count and add the current user to the viewedBy list
                    db.collection("tickets").document(ticketId).update(
                            "viewCount", FieldValue.increment(1),
                            "viewedBy", FieldValue.arrayUnion(currentUserId)
                    );
                }
            }
        });
    }

    private void setupPriceAndImage(View view, double originalPrice, double askingPrice, String imageBase64) { // Set up the price and image
        TextView tvOriginalPrice = view.findViewById(R.id.tvDetailOriginalPrice); // Find the original price TextView
        if (originalPrice > askingPrice) { // Check if the original price is greater than the asking price
            tvOriginalPrice.setVisibility(View.VISIBLE); // Show the original price TextView
            tvOriginalPrice.setText(originalPrice + " ₪"); // Set the original price
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Strike through the original price
        }
        if (askingPrice > 0) {
            int savingsPercent = (int) (100 - (askingPrice / originalPrice * 100)); // Calculate the savings percentage
            TextView tvSavings = view.findViewById(R.id.tvDetailSavings); // Find the savings TextView
            tvSavings.setVisibility(View.VISIBLE); // Show the savings TextView
            tvSavings.setText("חיסכון של " + savingsPercent + "%"); // Set the savings text
        }
        if (imageBase64 != null && !imageBase64.isEmpty()) { // Check if the image is not empty
            try {
                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT); // Decode the image
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); // Convert the image to a Bitmap
                ((ImageView) view.findViewById(R.id.ivDetailImage)).setImageBitmap(decodedByte); // Set the image
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void fetchSellerDetails(View view, String sellerId, String eventName) { // Fetch the seller details
        // Check if the seller ID is not null from "users" collection
        db.collection("users").document(sellerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) { // Check if the document exists
                String fullName = doc.getString("firstName") + " " + doc.getString("lastName"); // Get the seller's full name
                sellerPhone = doc.getString("phoneNumber"); // Get the seller's phone number

                // Set the seller details in the view
                ((TextView) view.findViewById(R.id.tvDetailSeller)).setText("מוכר: " + fullName);
                ((TextView) view.findViewById(R.id.tvDetailPhone)).setText(sellerPhone);
                view.findViewById(R.id.tvDetailPhone).setOnClickListener(v -> openWhatsApp(sellerPhone, eventName));

                fetchSellerSalesCount(view, sellerId); // Fetch the seller's sales count
            }
        });
    }

    private void fetchSellerSalesCount(View view, String sellerId) { // Fetch the seller's sales count
        // Check if the seller ID is not null from "tickets" collection
        db.collection("tickets")
                .whereEqualTo("sellerId", sellerId)
                .whereEqualTo("isSold", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size(); // Get the number of tickets sold by the seller
                    // Set the seller's sales count in the view
                    TextView tvTrust = view.findViewById(R.id.tvSellerTrust); // Find the seller's trust TextView
                    if (tvTrust != null) { // Check if the TextView exists
                        tvTrust.setText("מכר בהצלחה " + count + " כרטיסים ב-SafeTicket"); // Set the text
                    }
                });
    }

    private void openWhatsApp(String phone, String eventName) { // Open WhatsApp with the ticket details
        try {
            String cleanPhone = phone.replaceAll("[^0-9]", ""); // Remove all non-digit characters from the phone number
            if (cleanPhone.startsWith("0")) cleanPhone = "972" + cleanPhone.substring(1); // Add "972" if the phone number starts with "0"
            String message = Uri.encode("היי! אני מעוניין בכרטיס שלך ל-" + eventName + " שפורסם ב-SafeTicket"); // Create the WhatsApp message
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + cleanPhone + "&text=" + message)); // Create the intent to open WhatsApp
            startActivity(i); // Start the intent
        } catch (Exception e) {
            Toast.makeText(getContext(), "שגיאה ב-WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }
}