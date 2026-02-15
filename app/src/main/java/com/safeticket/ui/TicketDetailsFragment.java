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

public class TicketDetailsFragment extends Fragment {

    private FirebaseFirestore db;
    private String sellerPhone = "";
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (getArguments() != null) {
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

            // הצגת נתונים
            ((TextView) view.findViewById(R.id.tvDetailEventName)).setText(name);
            ((TextView) view.findViewById(R.id.tvDetailPrice)).setText(askingPrice + " ₪");
            ((TextView) view.findViewById(R.id.tvDetailCategory)).setText(category);
            ((TextView) view.findViewById(R.id.tvDetailDateTime)).setText(date + " | " + time);
            ((TextView) view.findViewById(R.id.tvDetailQuantity)).setText("כמות זמינה: " + quantity);

            String fullLocation = location;
            if (exactAddress != null && !exactAddress.isEmpty()) fullLocation += ", " + exactAddress;
            ((TextView) view.findViewById(R.id.tvDetailLocation)).setText(fullLocation);

            // לוגיקת צפיות - מניעת כפילות
            if (ticketId != null && currentUserId != null && !currentUserId.equals(sellerId)) {
                updateViewCount(ticketId);
            }

            // שאר העיצוב (מחיר מקורי ותמונה) נשאר כפי שהיה...
            setupPriceAndImage(view, originalPrice, askingPrice, imageBase64);

            if (sellerId != null) {
                fetchSellerDetails(view, sellerId, name);
            }
        }
    }

    private void updateViewCount(String ticketId) {
        // שימוש ב-arrayUnion וב-increment של Firestore כדי להבטיח אטומיות ומניעת כפילות
        db.collection("tickets").document(ticketId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                java.util.List<String> viewedBy = (java.util.List<String>) doc.get("viewedBy");
                if (viewedBy == null || !viewedBy.contains(currentUserId)) {
                    db.collection("tickets").document(ticketId).update(
                            "viewCount", FieldValue.increment(1),
                            "viewedBy", FieldValue.arrayUnion(currentUserId)
                    );
                }
            }
        });
    }

    private void setupPriceAndImage(View view, double originalPrice, double askingPrice, String imageBase64) {
        TextView tvOriginalPrice = view.findViewById(R.id.tvDetailOriginalPrice);
        if (originalPrice > askingPrice) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setText(originalPrice + " ₪");
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            int savingsPercent = (int) (100 - (askingPrice / originalPrice * 100));
            TextView tvSavings = view.findViewById(R.id.tvDetailSavings);
            tvSavings.setVisibility(View.VISIBLE);
            tvSavings.setText("חיסכון של " + savingsPercent + "%");
        }
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ((ImageView) view.findViewById(R.id.ivDetailImage)).setImageBitmap(decodedByte);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void fetchSellerDetails(View view, String sellerId, String eventName) {
        db.collection("users").document(sellerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String fullName = doc.getString("firstName") + " " + doc.getString("lastName");
                sellerPhone = doc.getString("phoneNumber");
                ((TextView) view.findViewById(R.id.tvDetailSeller)).setText("מוכר: " + fullName);
                ((TextView) view.findViewById(R.id.tvDetailPhone)).setText(sellerPhone);
                view.findViewById(R.id.tvDetailPhone).setOnClickListener(v -> openWhatsApp(sellerPhone, eventName));

                // שליפת סטטיסטיקת מכירות של המוכר לאמינות
                fetchSellerSalesCount(view, sellerId);
            }
        });
    }

    private void fetchSellerSalesCount(View view, String sellerId) {
        db.collection("tickets")
                .whereEqualTo("sellerId", sellerId)
                .whereEqualTo("isSold", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    TextView tvTrust = view.findViewById(R.id.tvSellerTrust);
                    if (tvTrust != null) {
                        tvTrust.setText("מכר בהצלחה " + count + " כרטיסים ב-SafeTicket");
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
            Toast.makeText(getContext(), "שגיאה ב-WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }
}