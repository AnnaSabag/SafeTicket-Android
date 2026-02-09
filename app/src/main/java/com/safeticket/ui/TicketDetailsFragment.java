package com.safeticket.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.safeticket.R;

public class TicketDetailsFragment extends Fragment {

    private FirebaseFirestore db;
    private String sellerPhone = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            String name = getArguments().getString("eventName");
            double askingPrice = getArguments().getDouble("askingPrice");
            double originalPrice = getArguments().getDouble("originalPrice");
            String location = getArguments().getString("location");
            String sellerId = getArguments().getString("sellerId");
            String imageBase64 = getArguments().getString("ticketImage");
            String date = getArguments().getString("eventDate");
            String time = getArguments().getString("eventTime");
            String category = getArguments().getString("category");

            // הצגת נתונים בסיסיים
            ((TextView) view.findViewById(R.id.tvDetailEventName)).setText(name);
            ((TextView) view.findViewById(R.id.tvDetailPrice)).setText(askingPrice + " ₪");
            ((TextView) view.findViewById(R.id.tvDetailLocation)).setText(location);
            ((TextView) view.findViewById(R.id.tvDetailCategory)).setText(category);
            ((TextView) view.findViewById(R.id.tvDetailDateTime)).setText(date + " | " + time);

            // טיפול בתמונה - תיקון הצגה
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ((ImageView) view.findViewById(R.id.ivDetailImage)).setImageBitmap(decodedByte);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // חישוב והצגת חיסכון
            if (originalPrice > askingPrice) {
                int savingsPercent = (int) (100 - (askingPrice / originalPrice * 100));
                TextView tvSavings = view.findViewById(R.id.tvDetailSavings);
                tvSavings.setVisibility(View.VISIBLE);
                tvSavings.setText("חיסכון של " + savingsPercent + "%");
            }

            if (sellerId != null) {
                fetchSellerDetails(view, sellerId, name);
            }
        }
    }

    private void fetchSellerDetails(View view, String sellerId, String eventName) {
        db.collection("users").document(sellerId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("firstName") + " " + doc.getString("lastName");
                        sellerPhone = doc.getString("phoneNumber");
                        boolean isVerified = Boolean.TRUE.equals(doc.getBoolean("isVerified"));

                        ((TextView) view.findViewById(R.id.tvDetailSeller)).setText("מוכר: " + fullName);
                        ((TextView) view.findViewById(R.id.tvDetailPhone)).setText(sellerPhone);

                        // הצגת V כחול אם המוכר מאומת
                        if (isVerified) {
                            view.findViewById(R.id.ivVerifiedBadge).setVisibility(View.VISIBLE);
                        }

                        view.findViewById(R.id.tvDetailPhone).setOnClickListener(v -> openWhatsApp(sellerPhone, eventName));
                    }
                });
    }

    private void openWhatsApp(String phone, String eventName) {
        try {
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            if (cleanPhone.startsWith("0")) cleanPhone = "972" + cleanPhone.substring(1);

            String message = Uri.encode("היי! אני מעוניין בכרטיס שלך ל-" + eventName + " שפורסם ב-SafeTicket");
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + cleanPhone + "&text=" + message));
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(getContext(), "WhatsApp error", Toast.LENGTH_SHORT).show();
        }
    }
}